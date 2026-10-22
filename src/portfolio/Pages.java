package portfolio;

import java.util.List;
import java.util.stream.Collectors;

/** HTML rendering. Plain strings and text blocks; no template engine, no reflection, no JavaScript. */
final class Pages {

    static String home(Content.Profile p, String cssPath) {
        return layout(p.name() + " · " + p.headline(), cssPath, p.name(), """
            <section class="hero" id="top">
              <p class="eyebrow">%s</p>
              <h1>%s</h1>
              <p class="lede">%s</p>
              <nav class="links">%s</nav>
            </section>

            <section id="about">
              <h2>About</h2>
              %s
            </section>

            <section id="experience">
              <h2>Experience</h2>
              <ol class="timeline">%s</ol>
            </section>

            <section id="projects">
              <h2>Projects</h2>
              <div class="cards">%s</div>
            </section>

            <section id="skills">
              <h2>Skills</h2>
              <dl class="skills">%s</dl>
            </section>
            """.formatted(
                esc(p.headline() + " · " + p.location()),
                esc(p.name()),
                esc(p.tagline()),
                p.links().stream().map(Pages::link).collect(Collectors.joining()),
                p.about().stream().map(s -> "<p>" + esc(s) + "</p>\n").collect(Collectors.joining()),
                p.roles().stream().map(Pages::role).collect(Collectors.joining()),
                p.projects().stream().map(Pages::project).collect(Collectors.joining()),
                p.skills().stream().map(Pages::skillGroup).collect(Collectors.joining())
            ));
    }

    static String notFound(String cssPath) {
        return layout("Not found", cssPath, "404", """
            <section class="hero">
              <p class="eyebrow">404</p>
              <h1>Nothing here</h1>
              <p class="lede">The page you asked for does not exist. <a href="/">Back to the front page.</a></p>
            </section>
            """);
    }

    private static String layout(String title, String cssPath, String brand, String body) {
        return """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <meta name="color-scheme" content="dark light">
              <title>%s</title>
              <link rel="stylesheet" href="%s">
              <link rel="icon" href="/favicon.svg" type="image/svg+xml">
            </head>
            <body>
              <header class="top">
                <a class="brand" href="/">%s</a>
                <nav>
                  <a href="/#about">About</a>
                  <a href="/#experience">Experience</a>
                  <a href="/#projects">Projects</a>
                  <a href="/#skills">Skills</a>
                </nav>
              </header>
              <main>
            %s
              </main>
              <footer>
                <p>
                  Served by <code>%s</code> ·
                  running as <code>%s</code> ·
                  heap ceiling <code>%d MiB</code> from a container limit of <code>%s</code> ·
                  <a href="/sys">/sys</a>
                </p>
                <p class="muted">Plain Java 21. No application server, no JVM flags, no build tool. One static binary in a distroless image.</p>
              </footer>
            </body>
            </html>
            """.formatted(
                esc(title), cssPath, esc(brand), body,
                esc(Sys.HOSTNAME), Sys.MODE,
                Runtime.getRuntime().maxMemory() >> 20,
                Sys.cgroupMemoryLimit() < 0 ? "none" : (Sys.cgroupMemoryLimit() >> 20) + " MiB");
    }

    private static String link(Content.Link l) {
        return "<a href=\"%s\" rel=\"me noopener\">%s</a>\n".formatted(esc(l.href()), esc(l.label()));
    }

    private static String role(Content.Role r) {
        return """
            <li>
              <div class="when">%s</div>
              <div>
                <h3>%s</h3>
                <p class="org">%s</p>
                <p>%s</p>
                <ul>%s</ul>
              </div>
            </li>
            """.formatted(esc(r.period()), esc(r.title()), esc(r.org()), esc(r.summary()), items(r.highlights()));
    }

    private static String project(Content.Project pr) {
        String name = pr.href() == null
            ? esc(pr.name())
            : "<a href=\"%s\">%s</a>".formatted(esc(pr.href()), esc(pr.name()));
        return """
            <article class="card">
              <h3>%s</h3>
              <p>%s</p>
              <p class="chips">%s</p>
            </article>
            """.formatted(name, esc(pr.blurb()), chips(pr.stack()));
    }

    private static String skillGroup(Content.SkillGroup g) {
        return "<dt>%s</dt>\n<dd class=\"chips\">%s</dd>\n".formatted(esc(g.name()), chips(g.skills()));
    }

    private static String items(List<String> xs) {
        return xs.stream().map(s -> "<li>" + esc(s) + "</li>").collect(Collectors.joining("\n"));
    }

    private static String chips(List<String> xs) {
        return xs.stream().map(s -> "<span>" + esc(s) + "</span>").collect(Collectors.joining(" "));
    }

    static String esc(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    static String favicon() {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
              <rect width="32" height="32" rx="7" fill="#0f172a"/>
              <path d="M9 22 L16 9 L23 22" fill="none" stroke="#7dd3fc" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 18 H20" stroke="#7dd3fc" stroke-width="3" stroke-linecap="round"/>
            </svg>
            """;
    }

    static String css() {
        return """
            :root {
              --bg: #0b1020; --bg-2: #111a2e; --fg: #e6edf7; --muted: #94a3b8;
              --accent: #7dd3fc; --accent-2: #a78bfa; --line: #1f2a44; --chip: #15213a;
              --max: 60rem;
            }
            @media (prefers-color-scheme: light) {
              :root { --bg: #f8fafc; --bg-2: #ffffff; --fg: #0f172a; --muted: #475569;
                      --accent: #0369a1; --accent-2: #6d28d9; --line: #e2e8f0; --chip: #eef2f7; }
            }
            * { box-sizing: border-box; }
            html { scroll-behavior: smooth; }
            body {
              margin: 0; background: var(--bg); color: var(--fg);
              font: 17px/1.6 system-ui, -apple-system, "Segoe UI", Roboto, Inter, sans-serif;
            }
            a { color: var(--accent); text-decoration: none; }
            a:hover { text-decoration: underline; }
            code { font: 0.9em ui-monospace, SFMono-Regular, Menlo, Consolas, monospace; color: var(--accent-2); }
            header.top {
              display: flex; justify-content: space-between; align-items: center; gap: 1rem;
              max-width: var(--max); margin: 0 auto; padding: 1.25rem 1rem;
            }
            header.top .brand { font-weight: 700; color: var(--fg); letter-spacing: 0.02em; }
            header.top nav { display: flex; gap: 1.25rem; flex-wrap: wrap; }
            header.top nav a { color: var(--muted); font-size: 0.95rem; }
            main { max-width: var(--max); margin: 0 auto; padding: 0 1rem 3rem; }
            section { padding: 2.5rem 0; border-top: 1px solid var(--line); }
            section.hero { border-top: 0; padding-top: 3.5rem; }
            .eyebrow { color: var(--accent); text-transform: uppercase; letter-spacing: 0.14em; font-size: 0.8rem; margin: 0 0 0.75rem; }
            h1 { font-size: clamp(2.4rem, 6vw, 4rem); line-height: 1.05; margin: 0 0 1rem; letter-spacing: -0.02em; }
            h2 { font-size: 1.5rem; margin: 0 0 1.25rem; letter-spacing: -0.01em; }
            h3 { font-size: 1.15rem; margin: 0 0 0.25rem; }
            .lede { font-size: 1.25rem; color: var(--muted); max-width: 44rem; margin: 0 0 1.5rem; }
            .links { display: flex; gap: 1rem; flex-wrap: wrap; }
            .links a {
              border: 1px solid var(--line); background: var(--bg-2); padding: 0.5rem 0.9rem;
              border-radius: 999px; font-size: 0.95rem;
            }
            #about p { max-width: 46rem; }
            .timeline { list-style: none; margin: 0; padding: 0; display: grid; gap: 2rem; }
            .timeline li { display: grid; grid-template-columns: 8rem 1fr; gap: 1rem; }
            .timeline .when { color: var(--muted); font-size: 0.95rem; padding-top: 0.2rem; }
            .timeline .org { color: var(--muted); margin: 0 0 0.5rem; }
            .timeline ul { margin: 0.5rem 0 0; padding-left: 1.2rem; color: var(--fg); }
            .timeline ul li { display: list-item; margin: 0.25rem 0; }
            .cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(16rem, 1fr)); gap: 1rem; }
            .card { background: var(--bg-2); border: 1px solid var(--line); border-radius: 0.9rem; padding: 1.25rem; }
            .card p { margin: 0.5rem 0; color: var(--muted); font-size: 0.98rem; }
            .chips span {
              display: inline-block; background: var(--chip); border: 1px solid var(--line);
              border-radius: 999px; padding: 0.15rem 0.6rem; font-size: 0.82rem; margin: 0.15rem 0; color: var(--fg);
            }
            dl.skills { display: grid; grid-template-columns: 8rem 1fr; gap: 0.75rem 1rem; margin: 0; }
            dl.skills dt { color: var(--muted); padding-top: 0.2rem; }
            dl.skills dd { margin: 0; }
            footer { max-width: var(--max); margin: 0 auto; padding: 1.5rem 1rem 3rem; border-top: 1px solid var(--line); font-size: 0.9rem; color: var(--muted); }
            footer p { margin: 0.25rem 0; }
            @media (max-width: 40rem) {
              .timeline li, dl.skills { grid-template-columns: 1fr; gap: 0.25rem; }
              header.top { flex-direction: column; align-items: flex-start; }
            }
            """;
    }

    private Pages() {}
}
