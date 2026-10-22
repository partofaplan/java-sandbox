package portfolio;

/**
 * All configuration comes from the environment, 12-factor style.
 * Deliberately: there is NO memory knob here. Memory is the container's job.
 */
public record Config(String host, int port, int drainSeconds) {

    public static Config fromEnv() {
        return new Config(
            env("HOST", "0.0.0.0"),
            Integer.parseInt(env("PORT", "8080")),
            Integer.parseInt(env("DRAIN_SECONDS", "5"))
        );
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }
}
