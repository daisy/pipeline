ifneq ($(firstword $(sort $(MAKE_VERSION) 3.82)), 3.82)
$(error "GNU Make 3.82 is required to run this script")
endif

ifeq ($(OS),Windows_NT)
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))bin/windows_amd64/eval-java.exe
else
UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -p)
ifeq ($(UNAME_S),Darwin)
ifneq ($(filter arm%,$(UNAME_P))$(filter aarch%,$(UNAME_P)),)
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))bin/darwin_arm64/eval-java
else
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))bin/darwin_amd64/eval-java
endif
else
ifeq ($(UNAME_S),Linux)
ifneq ($(filter arm%,$(UNAME_P))$(filter aarch%,$(UNAME_P)),)
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))bin/linux_arm64/eval-java
else
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))bin/linux_amd64/eval-java
endif
else
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))bin/windows_amd64/eval-java.exe
endif
endif
endif
.SHELLFLAGS :=

JAVA_VERSION := $(shell println(getJavaVersion());)

ifeq ($(JAVA_VERSION),)
# probably because java not found or exited with a UnsupportedClassVersionError
$(error "Java 8 is required to run this script")
else ifeq ($(shell println($(JAVA_VERSION) >= 8);), false)
$(error "Java 8 is required to run this script")
endif

OS := $(shell println(getOS());)
