package portfolio;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime facts, exposed at /sys so you can *see* the memory story:
 * the container cgroup limit on one line, the heap ceiling derived from it on the next.
 */
final class Sys {

    static final Instant STARTED = Instant.now();

    /** "native" when running as a GraalVM native binary, "jvm" under HotSpot (the ./dev loop). */
    static final String MODE =
        "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode")) ? "native" : "jvm";

    static final String HOSTNAME = hostname();

    static Map<String, Object> facts() {
        Runtime rt = Runtime.getRuntime();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("mode", MODE);
        m.put("hostname", HOSTNAME);
        m.put("pid", ProcessHandle.current().pid());
        m.put("startedAt", STARTED.toString());
        m.put("uptimeSeconds", Duration.between(STARTED, Instant.now()).toSeconds());
        m.put("cpus", rt.availableProcessors());
        m.put("cgroupMemoryLimitBytes", cgroupMemoryLimit());
        m.put("heapMaxBytes", rt.maxMemory());
        m.put("heapCommittedBytes", rt.totalMemory());
        m.put("heapUsedBytes", rt.totalMemory() - rt.freeMemory());
        m.put("java", System.getProperty("java.vendor.version", System.getProperty("java.vm.name")));
        m.put("javaVersion", System.getProperty("java.version"));
        m.put("arch", System.getProperty("os.arch"));
        return m;
    }

    /**
     * The memory limit the kernel enforces on this process, from the cgroup it runs in.
     * Tries the view a container sees first (cgroup mounted at its root), then the host view
     * (path from /proc/self/cgroup). -1 means "no limit visible" (bare metal, or an unlimited container).
     */
    static long cgroupMemoryLimit() {
        for (Path p : limitFiles()) {
            Long v = readLong(p);
            // cgroup v1 reports a huge page-rounded sentinel (~2^63) when unlimited; v2 reports "max".
            if (v != null && v > 0 && v < (1L << 60)) return v;
        }
        return -1L;
    }

    private static List<Path> limitFiles() {
        List<Path> out = new ArrayList<>();
        out.add(Path.of("/sys/fs/cgroup/memory.max"));                       // v2, container view
        out.add(Path.of("/sys/fs/cgroup/memory/memory.limit_in_bytes"));     // v1, container view
        try {
            for (String line : Files.readAllLines(Path.of("/proc/self/cgroup"))) {
                String[] f = line.split(":", 3);
                if (f.length != 3) continue;
                if (f[1].isEmpty()) {                                        // v2: "0::/path"
                    out.add(Path.of("/sys/fs/cgroup" + f[2] + "/memory.max"));
                    out.add(Path.of("/sys/fs/cgroup/unified" + f[2] + "/memory.max"));
                } else if (f[1].equals("memory")) {                          // v1: "4:memory:/path"
                    out.add(Path.of("/sys/fs/cgroup/memory" + f[2] + "/memory.limit_in_bytes"));
                }
            }
        } catch (IOException ignored) {
            // not Linux, or /proc not mounted; the container-view paths above still apply
        }
        return out;
    }

    private static Long readLong(Path p) {
        try {
            if (!Files.isReadable(p)) return null;
            String s = Files.readString(p).trim();
            if (s.equals("max")) return -1L;
            return Long.parseLong(s);
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }

    private static String hostname() {
        String env = System.getenv("HOSTNAME");
        if (env != null && !env.isBlank()) return env.trim();
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /** Minimal JSON for a flat map of strings, numbers, booleans and nulls. No library needed. */
    static String json(Map<String, Object> m) {
        StringBuilder sb = new StringBuilder("{\n");
        boolean first = true;
        for (var e : m.entrySet()) {
            if (!first) sb.append(",\n");
            first = false;
            sb.append("  \"").append(escape(e.getKey())).append("\": ");
            Object v = e.getValue();
            if (v == null) sb.append("null");
            else if (v instanceof Number || v instanceof Boolean) sb.append(v);
            else sb.append('"').append(escape(v.toString())).append('"');
        }
        return sb.append("\n}\n").toString();
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private Sys() {}
}
