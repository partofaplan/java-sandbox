package portfolio;

import java.util.List;

/**
 * The portfolio content. Edit this file to change the site; nothing else needs touching.
 * Content is plain Java records, so it is type-checked at compile time and baked into the
 * binary at build time. There is no CMS, no database, no runtime file I/O.
 */
public final class Content {

    public record Link(String label, String href) {}

    public record Role(String title, String org, String period, String summary, List<String> highlights) {}

    public record Project(String name, String blurb, List<String> stack, String href) {}

    public record SkillGroup(String name, List<String> skills) {}

    public record Profile(
        String name,
        String headline,
        String location,
        String tagline,
        List<String> about,
        List<Role> roles,
        List<Project> projects,
        List<SkillGroup> skills,
        List<Link> links
    ) {}

    public static final Profile PROFILE = new Profile(
        "Zach Perkins",
        "Senior Platform Engineer",
        "Aviture · IT-GEMS",
        "I build the platforms that other engineers ship on: containers, clusters, pipelines, and the guardrails that keep DoD workloads boring in the best possible way.",
        List.of(
            "I'm a Senior Platform Engineer at Aviture, a software consulting company whose engineers are embedded across multiple contracts. "
            + "Right now I work on IT-GEMS, a Department of Defense program where Deloitte is the prime and Aviture is a subcontractor.",
            "Aviture's platform engineers operate as a team nested inside the broader IT-GEMS platform engineering organization. "
            + "That means my day is equal parts infrastructure, developer experience, and translating between a prime's program needs and what actually runs in a cluster.",
            "This site is itself a small statement of taste: it is a Java application with no application server, no JVM memory flags, "
            + "and a container image measured in megabytes. Scroll to the footer to see which replica served you."
        ),
        List.of(
            new Role(
                "Senior Platform Engineer",
                "Aviture — IT-GEMS (DoD), subcontractor to Deloitte",
                "Present",
                "Member of Aviture's platform engineering team, nested within the IT-GEMS platform engineering team.",
                List.of(
                    "Design and operate container platforms and the delivery pipelines that feed them.",
                    "Own developer experience for application teams: golden paths, base images, and paved-road templates.",
                    "Harden workloads to DoD expectations: least privilege, read-only filesystems, non-root, minimal images.",
                    "Partner with the prime's platform leads to align architecture across a multi-vendor team."
                )
            ),
            new Role(
                "Platform / DevOps Engineering",
                "Aviture",
                "Earlier roles",
                "Infrastructure automation, CI/CD, and observability for consulting engagements across industries.",
                List.of(
                    "Infrastructure as code, GitOps, and cluster lifecycle automation.",
                    "Build and release engineering across Java, JavaScript, and Python stacks.",
                    "Observability: metrics, logs, traces, and the dashboards people actually read."
                )
            )
        ),
        List.of(
            new Project(
                "This site",
                "A portfolio served by a ~20 MB static Linux binary compiled from plain Java. No Tomcat, no Spring, no Maven, no -Xmx. "
                + "Memory is governed by the container cgroup; the heap sizes itself from the limit you give it.",
                List.of("Java 21", "jdk.httpserver", "virtual threads", "GraalVM native-image", "musl", "distroless"),
                "/sys"
            ),
            new Project(
                "Paved-road base images",
                "Opinionated, minimal base images with the build policy baked in, so application Dockerfiles stay five lines long "
                + "and security reviews stay short.",
                List.of("Docker", "multi-stage builds", "SBOM", "non-root"),
                null
            ),
            new Project(
                "Platform golden paths",
                "Templates and pipelines that take a service from repo to cluster with health checks, resource limits, "
                + "and disruption budgets already wired in.",
                List.of("Kubernetes", "GitOps", "CI/CD", "Helm/Kustomize"),
                null
            )
        ),
        List.of(
            new SkillGroup("Platform", List.of("Kubernetes", "Docker / OCI", "Linux", "Networking", "GitOps")),
            new SkillGroup("Delivery", List.of("CI/CD pipelines", "Infrastructure as Code", "Release engineering", "Supply-chain security")),
            new SkillGroup("Languages", List.of("Java", "Go", "Python", "Bash", "TypeScript")),
            new SkillGroup("Context", List.of("DoD programs", "Prime/sub collaboration", "Multi-vendor teams", "Compliance-aware design"))
        ),
        List.of(
            new Link("GitHub", "https://github.com/partofaplan"),
            new Link("Email", "mailto:zach.perkins@aviture.us.com")
        )
    );

    private Content() {}
}
