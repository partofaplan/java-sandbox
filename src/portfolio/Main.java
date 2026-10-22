package portfolio;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Entry point. There is no application server, no servlet container, no WAR.
 * The process opens its own socket, exactly like `node server.js` would.
 *
 *   dev:  make dev                     (javac + java, no build tool)
 *   prod: a single static binary       (see Dockerfile / base/Dockerfile)
 *
 * `app healthcheck` is the container probe: the runtime image has no shell or curl,
 * so the binary checks itself.
 */
public final class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("healthcheck")) {
            System.exit(healthcheck(Config.fromEnv()) ? 0 : 1);
        }
        Config config = Config.fromEnv();
        Site site = new Site(config, Content.PROFILE);
        site.run(); // blocks until SIGTERM/SIGINT, then drains and exits
    }

    /** GET /readyz on the local port; true only on HTTP 200. Raw socket, no HttpClient, keeps the binary small. */
    static boolean healthcheck(Config config) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress("127.0.0.1", config.port()), 1000);
            s.setSoTimeout(1000);
            OutputStream out = s.getOutputStream();
            out.write("GET /readyz HTTP/1.0\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();
            InputStream in = s.getInputStream();
            byte[] buf = new byte[64];
            int n = in.read(buf);
            String statusLine = n > 0 ? new String(buf, 0, n, StandardCharsets.US_ASCII) : "";
            return statusLine.startsWith("HTTP/1.") && statusLine.length() >= 12 && statusLine.substring(9, 12).equals("200");
        } catch (Exception e) {
            return false;
        }
    }
}
