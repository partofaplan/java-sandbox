# Java without the Java infrastructure. No Maven, no Gradle, no app server.
#
#   make dev      run from source on the JDK (fast loop, HotSpot)
#   make native   build the static binary locally (needs GraalVM native-image + musl on PATH)
#   make base     build the two base-image targets (toolchain, runtime)
#   make image    build the application image on top of the base
#   make run      run the image with a 64 MiB container limit and a read-only root fs
#   make up       three replicas via docker compose, ports 8080-8082
#   make clean

JAVA   ?= 21
BASE   ?= java-native-base
IMAGE  ?= portfolio:local
# The GraalVM muslib toolchain only exists for amd64; pin every build so the binary and runtime agree.
PLATFORM ?= linux/amd64
MAIN    = portfolio.Main
SOURCES = $(shell find src -name '*.java')

.PHONY: dev compile native base image run up down clean

compile: $(SOURCES)
	@javac -Xlint:all -Werror --release $(JAVA) -d .out $(SOURCES)

dev: compile
	@java -cp .out $(MAIN)

native: $(SOURCES)
	@SRC=$(CURDIR)/src CLASSES=$(CURDIR)/.out base/build-native $(MAIN) $(CURDIR)/out/app

base:
	docker build --platform $(PLATFORM) --target toolchain --build-arg JAVA=$(JAVA) -t $(BASE):$(JAVA)-toolchain base/
	docker build --platform $(PLATFORM) --target runtime   --build-arg JAVA=$(JAVA) -t $(BASE):$(JAVA)-runtime   base/

image:
	docker build --platform $(PLATFORM) --build-arg BASE=$(BASE) --build-arg JAVA=$(JAVA) -t $(IMAGE) .

run:
	docker run --rm --platform $(PLATFORM) -p 8080:8080 --memory=64m --read-only --cap-drop=ALL --security-opt=no-new-privileges $(IMAGE)

up:
	docker compose up --build

down:
	docker compose down

clean:
	rm -rf .out out
