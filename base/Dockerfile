# syntax=docker/dockerfile:1
#
# java-native-base: the base image for "Java without the Java infrastructure".
#
#   docker build --target toolchain -t java-native-base:21-toolchain base/
#   docker build --target runtime   -t java-native-base:21-runtime   base/
#
# Two targets, one Dockerfile:
#   toolchain  GraalVM native-image + musl. Turns .java files into ONE static executable.
#              Used only at build time; it never ships.
#   runtime    What actually ships. No JVM, no shell, no package manager, no libc.
#              The application binary is the only thing added on top.

ARG JAVA=21

# ---------------------------------------------------------------------------
# toolchain
# ---------------------------------------------------------------------------
# The muslib toolchain is published for linux/amd64 only, so build with --platform=linux/amd64
# (the Makefile does). Runs under emulation on arm64 hosts.
FROM ghcr.io/graalvm/native-image-community:${JAVA}-muslib AS toolchain
LABEL org.opencontainers.image.title="java-native-base/toolchain" \
      org.opencontainers.image.description="javac + GraalVM native-image + musl; compiles Java to a static Linux binary"
# findutils for build-native's source scan; binutils for `strip`. Neither ships in the slim image.
RUN microdnf install -y findutils binutils && microdnf clean all
COPY build-native /usr/local/bin/build-native
RUN chmod 0755 /usr/local/bin/build-native
WORKDIR /work
ENV SRC=/work/src CLASSES=/work/classes

# ---------------------------------------------------------------------------
# runtime
# ---------------------------------------------------------------------------
# distroless/static is ~2 MB: CA certificates, tzdata, /etc/passwd with a `nonroot` user (uid 65532),
# and nothing else. Swap for `FROM scratch` if you want literally nothing (then add your own
# /etc/passwd and CA bundle). The binary is static, so either works.
FROM gcr.io/distroless/static-debian12:nonroot AS runtime
LABEL org.opencontainers.image.title="java-native-base/runtime" \
      org.opencontainers.image.description="distroless static runtime for Java native binaries; no JVM, no shell"
USER nonroot:nonroot
# Convention every app built on this base follows:
#   - listens on $PORT (default 8080)
#   - the executable is copied to /app and is the ENTRYPOINT
#   - `/app healthcheck` exits 0 when ready (no curl/wget in the image)
#   - memory: set ONLY the container limit; the heap sizes itself from it
ENV PORT=8080
EXPOSE 8080
