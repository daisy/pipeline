ifneq ($(firstword $(sort $(MAKE_VERSION) 3.82)), 3.82)
$(error "GNU Make 3.82 is required to run this script")
endif

ifeq ($(OS),Windows_NT)
EVAL_JAVA := $(dir $(lastword $(MAKEFILE_LIST)))bin/windows_amd64/eval-java.exe
SHELL := $(EVAL_JAVA)
else
UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -m)
ifeq ($(UNAME_S),Darwin)
ifneq ($(filter arm%,$(UNAME_P))$(filter aarch%,$(UNAME_P)),)
EVAL_JAVA := $(dir $(lastword $(MAKEFILE_LIST)))bin/darwin_arm64/eval-java
else
EVAL_JAVA := $(dir $(lastword $(MAKEFILE_LIST)))bin/darwin_amd64/eval-java
endif
else
ifneq ($(filter arm%,$(UNAME_P))$(filter aarch%,$(UNAME_P)),)
EVAL_JAVA := $(dir $(lastword $(MAKEFILE_LIST)))bin/linux_arm64/eval-java
else
EVAL_JAVA := $(dir $(lastword $(MAKEFILE_LIST)))bin/linux_amd64/eval-java
endif
endif
ifneq ($(shell                                                                                   \
    if ! $(EVAL_JAVA) --verify 2>/dev/null; then                                                 \
        echo "$(EVAL_JAVA) can not be executed on this platform. Compiling it from source" >&2;  \
        echo "cc $(dir $(lastword $(MAKEFILE_LIST)))eval-java.c -o $(EVAL_JAVA)" >&2;            \
        cc $(dir $(lastword $(MAKEFILE_LIST)))eval-java.c -o $(EVAL_JAVA);                       \
    fi;                                                                                          \
    echo $$?                                                                                     ),0)
$(error Failed to compile eval-java.c)
else
SHELL := $(EVAL_JAVA)
endif
endif

.SHELLFLAGS :=

# Reset environment variables to avoid pass-through, when one Makefile
# invokes make in another directory.
CLASSPATH :=
IMPORTS :=
STATIC_IMPORTS :=
JAVA_REPL_PORT :=
unexport CLASSPATH IMPORTS STATIC_IMPORTS JAVA_REPL_PORT

JAVA_VERSION := $(shell println(getJavaVersion());)

ifeq ($(JAVA_VERSION),)
# probably because java not found or exited with a UnsupportedClassVersionError
$(error Java 8 is required to run this script)
else ifeq ($(shell println($(JAVA_VERSION) >= 8);), false)
$(error Java 8 is required to run this script)
endif

OS := $(shell println(getOS());)

ifneq ($(OS), WINDOWS)
export JAVA_REPL_KILL_AFTER_IDLE := 10
# The following is not enabled by default, because starting the server
# now means that any changes to environment variables after the
# include of enable-java-shell.mk will not be noticed by
# java-eval. This includes the environment variables JAVA_HOME, IMPORTS
# and STATIC_IMPORTS, and any other environment variables used by a
# recipe. Users may optionally include this line in their Makefile, at
# a position of their liking.
#export JAVA_REPL_PORT := $(shell --spawn-repl-server)
endif

# utility function for helping with the migration from bash to java shell
define \n


endef
quote-for-java = "$(subst ${\n},\n,$(subst ",\",$(subst \,\\,$(1))))"
bash = exec("bash", "-c", $(call quote-for-java,$1));
