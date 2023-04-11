ifneq ($(firstword $(sort $(MAKE_VERSION) 3.82)), 3.82)
$(error "GNU Make 3.82 is required to run this script")
endif

ifeq ($(OS),Windows_NT)
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))eval-java.exe
else
SHELL := $(dir $(lastword $(MAKEFILE_LIST)))eval-java
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
