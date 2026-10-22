# portfolio — Java without the Java infrastructure

A personal portfolio site written in plain Java 21 that ships as **one static Linux executable**
in a **distroless image**, with **no application server**, **no JVM memory flags**, and
**no build tool**.

| Annoyance | What this repo does instead |
|---|---|
| Java has to run on a web server | The process owns its socket, like `node server.js`. The listener is the JDK's own `jdk.httpserver` module on virtual threads. No Tomcat, Jetty, WildFly, WAR, or servlet API anywhere. |
| Memory is set by the JVM | There is no JVM in the shipped image. The binary reads the container's cgroup limit and caps its heap at 80 % of it. The *only* memory setting is `--memory` / `limits.memory`. |
| Container should be lightweight and HA capable | ~18.6 MB binary on a ~2 MB distroless base, starts in ~12 ms, idles at ~21 MiB RSS. Readiness/liveness endpoints, SIGTERM drain, non-root, read-only rootfs, PDB + HPA + spread constraints in `deploy/`. |

## Layout

```
src/portfolio/        the whole app, 6 files, zero dependencies
  Main.java           entry point + the `healthcheck` subcommand used by container probes
  Config.java         env-only config (PORT, HOST, DRAIN_SECONDS). No memory knob on purpose.
  Site.java           the HTTP listener: routing, caching headers, graceful drain
  Pages.java          HTML + CSS rendering with text blocks; no template engine, no JS
  Content.java        your portfolio data as Java records -> edit this to change the site
  Sys.java            runtime facts for /sys (cgroup limit, heap ceiling, replica hostname)
base/Dockerfile       the base image: `toolchain` (GraalVM native-image + musl) and `runtime` (distroless static)
base/build-native     the compile policy, baked into the toolchain image
Dockerfile            the app image: FROM base toolchain -> build -> FROM base runtime (5 lines)
compose.yaml          3 replicas, 64 MiB each, ports 8080-8082
deploy/k8s.yaml       Deployment + Service + PodDisruptionBudget + HPA
Makefile              dev / native / base / image / run / up
```

## Run it

```sh
make dev                      # javac + java on the JDK; http://localhost:8080
make base && make image       # build the base targets, then the app image
make run                      # docker run ... --memory=64m --read-only
make up                       # docker compose: 3 replicas behind ports 8080-8082
kubectl apply -f deploy/k8s.yaml
```

Endpoints:

| Path | Purpose |
|---|---|
| `/` | the site |
| `/healthz` | liveness, always 200 while the process is up |
| `/readyz` | readiness: 200, or 503 the instant a SIGTERM arrives |
| `/sys` | JSON: cgroup limit, heap ceiling, hostname, pid, uptime, mode (`native`/`jvm`) |

## How each annoyance is addressed

### 1. No web server

`Site.java` calls `HttpServer.create(...)` from the JDK module `jdk.httpserver`, sets a
virtual-thread-per-request executor, and starts listening. That is the entire "deployment".
Nothing is deployed *into* anything; the process *is* the service. It handles `GET`/`HEAD`,
ETags, `304 Not Modified`, content-hashed immutable CSS, security headers, and a structured
access log, all in about 150 lines. There is no servlet, no filter chain, no container lifecycle.

The dev loop is `javac` + `java` (`make dev`). On JDK 22+ you can skip even that and run
`java src/portfolio/Main.java` directly (JEP 458, multi-file source launch).

### 2. Memory belongs to the container

The shipped artifact is a GraalVM native executable, not bytecode on a JVM. It still has a
garbage-collected heap, but the ceiling is derived from the cgroup the kernel put the process in:

```
docker run --memory=64m portfolio   ->   /sys reports heapMaxBytes ~= 0.8 * 64 MiB
docker run --memory=256m portfolio  ->   /sys reports heapMaxBytes ~= 0.8 * 256 MiB
```

`base/build-native` bakes `-R:MaximumHeapSizePercent=80` in at compile time. That is a
*policy* ("leave 20 % for stack, code, and the kernel"), not a size. Sizes come only from
`--memory` / `resources.limits.memory`. There is no `JAVA_OPTS`, no `JAVA_TOOL_OPTIONS`,
no `-Xmx`, and the runtime image has no shell to sneak one in with.

`Config.java` has no memory field on purpose. `/sys` shows the container limit and the heap
ceiling side by side so the relationship is visible, not folklore.

### 3. Lightweight and HA capable

* **Image**: static binary (`--static --libc=musl`) on `distroless/static` (~2 MB). No JVM,
  no libc, no shell, no package manager. Runs as `nonroot` with a read-only root filesystem.
* **Startup**: milliseconds, because there is no class loading or JIT warm-up. Fast startup is
  what makes horizontal scaling and rolling restarts cheap.
* **Graceful drain**: on SIGTERM the binary flips `/readyz` to 503 first, waits `DRAIN_SECONDS`
  for the load balancer to notice, then stops accepting and finishes in-flight requests.
  Built with `--install-exit-handlers` so signals reach the shutdown hook in a native binary.
* **Self-contained probes**: `/app healthcheck` does an HTTP GET against its own `/readyz`,
  so Docker and Compose health checks work in an image that has no curl.
* **Kubernetes**: 3 replicas, `maxUnavailable: 0`, startup/readiness/liveness probes,
  a PodDisruptionBudget of 2, a CPU HPA from 3 to 12, and hostname spread constraints.
* **Stateless**: every page is rendered once at startup and served from immutable bytes.
  Any replica can serve any request; the footer and `X-Served-By` tell you which one did.

## Editing the content

Everything on the page comes from `src/portfolio/Content.java`: name, headline, about
paragraphs, roles, projects, skills, links. It is typed Java, so a typo is a compile error,
and it is baked into the binary, so there is nothing to mount or configure at runtime.

## Measured

Built and measured with GraalVM for JDK 21, fully static against musl:

| Metric | Value |
|---|---|
| Binary size | 18.6 MB |
| Image size (binary + distroless static) | ~21 MB |
| Startup to first successful `/readyz` | ~12 ms |
| Resident memory, idle | ~21 MiB |
| Heap ceiling under a 64 MiB cgroup limit | 51 MiB (53,477,376 bytes) |
| Heap ceiling under a 256 MiB cgroup limit | 204 MiB (214,433,792 bytes) |
