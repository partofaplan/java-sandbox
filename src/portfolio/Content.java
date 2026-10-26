package portfolio;

import java.util.List;

/**
 * The portfolio content. Edit this file to change the site; nothing else needs touching.
 * Content is plain Java records, so it is type-checked at compile time and baked into the
 * binary at build time. There is no CMS, no database, no runtime file I/O.
 *
 * Source of truth: Zachary_Perkins_Resume.docx. The phone number is left off on purpose.
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
        "Zachary Perkins",
        "Senior Platform Engineer",
        "Omaha, NE",
        "11+ years architecting secure, highly available infrastructure for mission-critical and DoD environments.",
        List.of(
            "I'm a Platform Engineer with deep expertise in self-managed Kubernetes (RKE2/Rancher), air-gapped cloud infrastructure, "
            + "and custom Kubernetes operator development in Go.",
            "I have a track record of cutting cloud spend and deployment cycle times in half or more while raising security posture "
            + "to NIST/DoD standards, and I pair hands-on platform work with technical strategy, OKR-setting, and mentorship "
            + "to scale engineering teams without sacrificing velocity or stability.",
            "This site is itself a small statement of taste: it is a Java application with no application server, no JVM memory flags, "
            + "and a container image measured in megabytes. Scroll to the footer to see which replica served you."
        ),
        List.of(
            new Role(
                "Senior Platform Engineer",
                "Aviture",
                "Aug 2024 – Present",
                "DoD contract: IT-GEMS / Defense Manpower Data Center (DMDC).",
                List.of(
                    "Architect and operate a self-managed, cloud-agnostic Kubernetes (RKE2/Rancher) platform from the ground up in an "
                    + "air-gapped Oracle Cloud Infrastructure environment supporting a DMDC mission system.",
                    "Engineered custom Kubernetes operators in Go to automate Rancher audit-log management and lifecycle tasks in a fully "
                    + "air-gapped environment, closing a compliance gap without adding manual toil.",
                    "Cut cloud infrastructure costs 75% by automating storage cleanup and backup lifecycle management.",
                    "Redesigned the RKE2 upgrade pipeline, halving the time to roll out Kubernetes version and patch upgrades across the fleet.",
                    "Established engineering standards and delivery policies that halved average project lead time and raised sprint throughput.",
                    "Authored a 2–3 year infrastructure roadmap aligning business and mission stakeholders.",
                    "Led the migration of on-prem Java/WebLogic applications to containerized WebLogic on containerd and Podman, "
                    + "with Kubernetes operators to manage WebLogic at scale.",
                    "Drove adoption of zero-trust secrets management and Infrastructure-as-Code via Gitflow in line with NIST/DoD standards.",
                    "Designed Kubernetes and containerized-operations training for internal IT staff, and coached engineers on GenAI "
                    + "prompting and context handling using GenAI.mil.",
                    "Mentor application engineers and interns on Agile practices, 1:1 development plans, and platform tooling."
                )
            ),
            new Role(
                "DevOps Lead",
                "Chair Four Development Group LLC",
                "Feb 2024 – Aug 2024",
                "Automation and delivery across distributed on-prem and edge infrastructure.",
                List.of(
                    "Moved hardware and software configuration to a fully automated pipeline using Ansible, GitLab CI/CD, and Python.",
                    "Built an observability platform spanning edge servers, databases, and IoT devices with Prometheus, Loki, and Grafana.",
                    "Established Agile development processes across a distributed on-prem/edge infrastructure team.",
                    "Administered Red Hat Linux across isolated Dell server VLANs (RAID, DNS/DHCP, switching) and standardized "
                    + "container workloads on Podman as systemd-managed processes."
                )
            ),
            new Role(
                "Senior AWS Cloud Engineer",
                "Surest (contract via Apex Systems)",
                "Sep 2023 – Feb 2024",
                "Golden images, patching, and infrastructure as code on AWS.",
                List.of(
                    "Built a Jenkins pipeline and custom modules to produce and manage golden AMIs end-to-end with Packer.",
                    "Wrote Python/Groovy pipelines to auto-patch AMIs with the latest golden image, reducing reported vulnerabilities 25%.",
                    "Designed zero-downtime patching for priority security infrastructure: bastion hosts, Jenkins clusters, and proxies.",
                    "Built ECS development environments with Terragrunt/Terraform and Jenkins, plus Postgres RDS deployment code "
                    + "optimized for regional read/write replicas.",
                    "Guided junior DevOps engineers on IaC best practices and GitOps standards."
                )
            ),
            new Role(
                "Platform Engineer III",
                "Mindbody",
                "Jul 2022 – Aug 2023",
                "Self-managed EKS and build infrastructure for an internal developer platform.",
                List.of(
                    "Cut self-managed EKS cluster spin-up time 75% for Azure DevOps build agents through customized Docker image caching.",
                    "Wrote Python Lambda functions with Pulumi integrating the Azure DevOps API to improve agent pool monitoring.",
                    "Built EKS node observability dashboards in New Relic, Prometheus, and Grafana; upgraded Windows nodes via EC2 Image Builder.",
                    "Authored technical design specifications and helped developers optimize service infrastructure on the platform."
                )
            ),
            new Role(
                "DevOps Engineer",
                "Fusion Medical Staffing",
                "Jun 2021 – Jul 2022",
                "Moved production from Azure App Service to Kubernetes.",
                List.of(
                    "Migrated production workloads to Azure Kubernetes Service, replacing periodic-release downtime with rolling blue/green deployments.",
                    "Built AKS clusters with Terraform/Terragrunt in Azure DevOps pipelines and configured OpenMesh for canary rollouts.",
                    "Automated iOS and Android release pipelines for the Fusion Marketplace app.",
                    "Implemented Dynatrace APM and service dashboards, moving dev teams from nighttime to daytime deployment windows."
                )
            ),
            new Role(
                "Infrastructure Automation Engineer",
                "Boys Town",
                "Dec 2019 – May 2021",
                "VM, application, and database deployment automation.",
                List.of(
                    "Automated VM and application deployment workflows with vRealize Automation, Rundeck, and PowerShell.",
                    "Built Azure application deployment pipelines and automated MSSQL database deployments.",
                    "Led the upgrade of Splunk and VMware infrastructure to improve performance and reliability."
                )
            ),
            new Role(
                "DevOps / Systems Engineer",
                "Proxibid",
                "Oct 2018 – Nov 2019",
                "CI/CD and high-availability AWS infrastructure for production web applications.",
                List.of(
                    "Built CI/CD pipelines across Jenkins, Nexus, Docker, Rancher, Ansible, VMware, and AWS.",
                    "Upgraded high-availability AWS architecture and managed network security via Palo Alto firewalls and certificate management.",
                    "Developed monitoring dashboards and metrics automation feeding Datadog and Nagios, and administered Jira, Confluence, and Bitbucket."
                )
            ),
            new Role(
                "Software Engineer",
                "Northrop Grumman",
                "Aug 2015 – Oct 2018",
                "Java middleware automation and security compliance for DoD systems.",
                List.of(
                    "Automated deployment of Java applications to GlassFish and WebLogic using Ansible.",
                    "Maintained middleware patching and configuration scripts in Perl and Bash across GlassFish, WebLogic, and Apache.",
                    "Participated in security audits, producing NIST compliance documentation for DoD review.",
                    "Migrated strategic platform software to AWS and validated performance with JVM/JMeter load testing."
                )
            )
        ),
        List.of(
            new Project(
                "This site",
                "A portfolio served by a ~20 MB static Linux binary compiled from plain Java. No Tomcat, no Spring, no Maven, no -Xmx. "
                + "Memory is governed by the container cgroup; the heap sizes itself from the limit you give it.",
                List.of("Java 21", "jdk.httpserver", "virtual threads", "GraalVM native-image", "distroless", "amd64 + arm64"),
                "/sys"
            ),
            new Project(
                "Air-gapped Kubernetes operators",
                "Custom operators in Go that automate Rancher audit-log management, cluster lifecycle tasks, and WebLogic at scale "
                + "inside a fully air-gapped Oracle Cloud environment.",
                List.of("Go", "Kubernetes operators", "RKE2", "Rancher", "WebLogic", "OCI"),
                null
            ),
            new Project(
                "Golden image pipeline",
                "Jenkins and Packer pipeline that builds, patches, and rolls out golden AMIs end-to-end, cutting reported vulnerabilities 25%.",
                List.of("Packer", "Jenkins", "Groovy", "Python", "AWS"),
                null
            )
        ),
        List.of(
            new SkillGroup("Platforms & Orchestration", List.of("Kubernetes (RKE2, EKS, AKS)", "Rancher", "Docker", "Podman", "containerd", "RHEL")),
            new SkillGroup("Languages & Automation", List.of("Go", "Python", "Bash", "Groovy", "PowerShell", "JavaScript", "Kubernetes operators")),
            new SkillGroup("CI/CD & IaC", List.of("GitLab CI/CD", "Jenkins", "Ansible", "Terraform", "Terragrunt", "Packer", "GitOps/Gitflow")),
            new SkillGroup("Cloud & Infrastructure", List.of("Oracle Cloud (OCI)", "AWS", "Azure", "Air-gapped / on-prem", "Dell PowerEdge", "VMware")),
            new SkillGroup("Observability & Security", List.of("Prometheus", "Grafana", "Loki", "Dynatrace", "New Relic", "Datadog", "Zero-trust secrets", "NIST/DoD compliance")),
            new SkillGroup("Education", List.of("B.S. Cloud Computing, Western Governors University (2021)")),
            new SkillGroup("Certifications", List.of("CompTIA Security+ ce", "ITIL Foundation", "CompTIA Cloud Essentials", "Linux Essentials")),
            new SkillGroup("Clearance", List.of("DoD Secret (active)"))
        ),
        List.of(
            new Link("GitHub", "https://github.com/partofaplan"),
            new Link("LinkedIn", "https://www.linkedin.com/in/zachperkins"),
            new Link("Email", "mailto:partofaplan@gmail.com")
        )
    );

    private Content() {}
}
