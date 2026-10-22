package portfolio;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The whole "server". It is the JDK's own HTTP listener (module jdk.httpserver) running on
 * virtual threads, so one process handles many thousands of idle connections without a thread
 * pool to tune. Nothing here is deployed *into* anything; the process *is* the service.
 */
final class Site {

    private static final String HTML = "text/html; charset=utf-8";
    private static final String CSS = "text/css; charset=utf-8";
    private static final String SVG = "image/svg+xml";
    private static final String TEXT = "text/plain; charset=utf-8";
    private static final String JSON = "application/json";

    private final Config config;
    private final AtomicBoolean ready = new AtomicBoolean(false);
    private final CountDownLatch exited = new CountDownLatch(1);

    // Everything the site serves is rendered once at startup and held as immutable bytes.
    private final byte[] home;
    private final byte[] notFound;
    private final byte[] css;
    private final byte[] favicon;
    private final byte[] robots;
    private final String cssPath;
    private final String homeEtag;

    Site(Config config, Content.Profile profile) {
        this.config = config;
        this.css = bytes(Pages.css());
        this.cssPath = "/assets/site." + sha256(css).substring(0, 12) + ".css";
        this.favicon = bytes(Pages.favicon());
        this.robots = bytes("User-agent: *\nAllow: /\n");
        this.home = bytes(Pages.home(profile, cssPath));
        this.notFound = bytes(Pages.notFound(cssPath));
        this.homeEtag = '"' + sha256(home).substring(0, 16) + '"';
    }

    void run() throws IOException, InterruptedException {
        HttpServer server = HttpServer.create(new InetSocketAddress(config.host(), config.port()), 1024);
        ExecutorService vthreads = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(vthreads);
        server.createContext("/", this::handle);

        // SIGTERM/SIGINT -> shutdown hook -> drain -> exit. In the native binary this works because
        // the base image builds with --install-exit-handlers.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(server, vthreads), "drain"));

        server.start();
        ready.set(true);
        log("listening host=%s port=%d mode=%s pid=%d host=%s heapMax=%dMiB cgroupLimit=%s",
            config.host(), config.port(), Sys.MODE, ProcessHandle.current().pid(), Sys.HOSTNAME,
            Runtime.getRuntime().maxMemory() >> 20, human(Sys.cgroupMemoryLimit()));
        exited.await();
    }

    private void shutdown(HttpServer server, ExecutorService vthreads) {
        // 1. Fail readiness first so the load balancer / kubelet stops sending new traffic.
        ready.set(false);
        log("shutdown: readiness=false, draining for %ds", config.drainSeconds());
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(config.drainSeconds()));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        // 2. Stop accepting, give in-flight exchanges up to 2s to finish.
        server.stop(2);
        vthreads.shutdown();
        log("shutdown: complete");
        exited.countDown();
    }

    private void handle(HttpExchange ex) {
        long t0 = System.nanoTime();
        int status;
        try (ex) {
            status = route(ex);
        } catch (Exception e) {
            status = 500;
            try {
                send(ex, 500, TEXT, bytes("internal error\n"), null, "no-store");
            } catch (IOException ignored) {
                // client went away; nothing to do
            }
        }
        log("%s %s %d %.1fms", ex.getRequestMethod(), ex.getRequestURI().getPath(), status,
            (System.nanoTime() - t0) / 1_000_000.0);
    }

    private int route(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (!method.equals("GET") && !method.equals("HEAD")) {
            ex.getResponseHeaders().set("Allow", "GET, HEAD");
            return send(ex, 405, TEXT, bytes("method not allowed\n"), null, "no-store");
        }
        String path = ex.getRequestURI().getPath();
        if (path.equals(cssPath)) {
            return send(ex, 200, CSS, css, null, "public, max-age=31536000, immutable");
        }
        return switch (path) {
            case "/", "/index.html" -> send(ex, 200, HTML, home, homeEtag, "public, max-age=60");
            case "/favicon.svg"     -> send(ex, 200, SVG, favicon, null, "public, max-age=86400");
            case "/robots.txt"      -> send(ex, 200, TEXT, robots, null, "public, max-age=86400");
            case "/healthz"         -> send(ex, 200, TEXT, bytes("ok\n"), null, "no-store");
            case "/readyz"          -> ready.get()
                                        ? send(ex, 200, TEXT, bytes("ready\n"), null, "no-store")
                                        : send(ex, 503, TEXT, bytes("draining\n"), null, "no-store");
            case "/sys"             -> send(ex, 200, JSON, bytes(Sys.json(Sys.facts())), null, "no-store");
            default                 -> send(ex, 404, HTML, notFound, null, "no-store");
        };
    }

    private static int send(HttpExchange ex, int status, String type, byte[] body, String etag, String cache)
        throws IOException {
        var h = ex.getResponseHeaders();
        h.set("Content-Type", type);
        h.set("Cache-Control", cache);
        h.set("X-Content-Type-Options", "nosniff");
        h.set("X-Frame-Options", "DENY");
        h.set("Referrer-Policy", "strict-origin-when-cross-origin");
        h.set("Content-Security-Policy", "default-src 'none'; style-src 'self'; img-src 'self' data:; base-uri 'none'; form-action 'none'");
        h.set("X-Served-By", Sys.HOSTNAME);
        if (etag != null) {
            h.set("ETag", etag);
            String inm = ex.getRequestHeaders().getFirst("If-None-Match");
            if (etag.equals(inm)) {
                ex.sendResponseHeaders(304, -1);
                return 304;
            }
        }
        if (ex.getRequestMethod().equals("HEAD")) {
            h.set("Content-Length", Integer.toString(body.length));
            ex.sendResponseHeaders(status, -1);
            return status;
        }
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(body);
        }
        return status;
    }

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String human(long bytes) {
        return bytes < 0 ? "none" : (bytes >> 20) + "MiB";
    }

    private static void log(String fmt, Object... args) {
        System.out.println(java.time.Instant.now() + " " + String.format(fmt, args));
    }
}
