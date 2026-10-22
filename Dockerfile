# syntax=docker/dockerfile:1
#
# Application image. Five meaningful lines, because the policy lives in the base image.
#
#   make base image   # or: docker build -t portfolio .   (after building base/)
#   docker run --rm -p 8080:8080 --memory=64m --read-only portfolio
#
# BASE defaults to the locally built base (see base/Dockerfile and the Makefile).
# Point it at a registry once the base is published, e.g. --build-arg BASE=ghcr.io/you/java-native-base.
ARG BASE=java-native-base
ARG JAVA=21

FROM ${BASE}:${JAVA}-toolchain AS build
COPY src ./src
RUN build-native portfolio.Main /work/out/app

FROM ${BASE}:${JAVA}-runtime
COPY --from=build /work/out/app /app
HEALTHCHECK --interval=10s --timeout=2s --start-period=1s --retries=3 CMD ["/app", "healthcheck"]
ENTRYPOINT ["/app"]
