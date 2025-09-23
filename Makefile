include .make/enable-java-shell.mk

.PHONY : default
default : help

MVN_WORKSPACE           := .maven-workspace
MVN_CACHE               := .maven-cache

# -----------------------------------
MVN_SETTINGS            := settings.xml
MVN_PROPERTIES          := -Dorg.ops4j.pax.url.mvn.localRepository="$(CURDIR)/$(MVN_WORKSPACE)" \
                           -Dorg.ops4j.pax.url.mvn.settings="$(CURDIR)/settings.xml"
MVN_RELEASE_CACHE_REPO  := $(MVN_CACHE)

ifneq ($(MAKECMDGOALS),)
ifneq ($(MAKECMDGOALS), help)
ifneq ($(MAKECMDGOALS), dump-maven-cmd)
ifneq ($(MAKECMDGOALS), dump-gradle-cmd)
ifneq ($(MAKECMDGOALS), cache)
ifneq ($(MAKECMDGOALS), clean-website)
include .make/main.mk
assembly/BASEDIR := assembly
assembly/SOURCES : assembly/.compile-dependencies
include assembly/deps.mk
ifneq ($(OS), WINDOWS)
-include webui/.deps.mk
endif
else
.SILENT: clean-website
endif
else
.SILENT: cache
endif
else
.SILENT: dump-gradle-cmd
endif
else
.SILENT: dump-maven-cmd
endif
else
.SILENT: help
endif
else
.SILENT: help
endif

USER_HOME := $(shell println(System.getProperty("user.home").replace('\\', '/'));)

# instead of passing system properties "workspace" and "cache" we substitute them in the settings.xml file
# this is required for org.ops4j.pax.url.mvn.settings
settings.xml : settings.xml.in
	try (BufferedReader in = new BufferedReader(new FileReader("$<")); \
	     PrintStream out = new PrintStream(new FileOutputStream("$@"))) { \
		String line; \
		while ((line = in.readLine()) != null) { \
			out.println(line.replace("$${workspace}", "$(CURDIR)/$(MVN_WORKSPACE)") \
			                .replace("$${cache}", "$(CURDIR)/$(MVN_CACHE)") \
			                .replace("$${user.home}", "$(USER_HOME)")); \
		} \
	}

# -----------------------------------

.PHONY : dist-zip-linux
dist-zip-linux : pipeline2-$(assembly/VERSION)_linux.zip

.PHONY : dist-zip-mac
dist-zip-mac : pipeline2-$(assembly/VERSION)_mac.zip

.PHONY : dist-zip-win
dist-zip-win : pipeline2-$(assembly/VERSION)_windows.zip

.PHONY : dist-deb
dist-deb : pipeline2-$(assembly/VERSION)_debian.deb

.PHONY : dist-rpm
dist-rpm : pipeline2-$(assembly/VERSION)_redhat.rpm

# FIXME: $(cli/VERSION) does not always match version in assembly/pom.xml
.PHONY : dist-cli-deb
dist-cli-deb : cli-$(cli/VERSION)-linux_386.deb

make-assembly = exec(env("MAKECMDGOALS", null, \
                         "CLASSPATH", "$(addprefix $(ROOT_DIR)/,$(CLASSPATH))", \
                         "MVN", "mvn(commandLineArgs);", \
                         "MVN_LOCAL_REPOSITORY", "$(CURDIR)/$(MVN_LOCAL_REPOSITORY)"), \
                     "$(MAKE)", "-C", "assembly", $1);

ifneq ($(OS), WINDOWS)

.PHONY : dist-docker-image
dist-docker-image : assembly/.compile-dependencies | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "docker")

.PHONY : dist-webui-zip
dist-webui-zip : assembly/.compile-dependencies
	/* see webui README for instructions on how to make a signed package for distribution */ \
	$(call bash, \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" clean universal:packageBin | $(call eval-java, $(MVN_LOG)) && \
		mv webui/target/universal/*zip . \
	)

.PHONY : dist-webui-deb
dist-webui-deb : assembly/.compile-dependencies
	/* see webui README for instructions on how to make a signed package for distribution */ \
	$(call bash, \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" clean debian:packageBin | $(call eval-java, $(MVN_LOG)) && \
		mv webui/target/*deb . \
	)

.PHONY : dist-webui-rpm
dist-webui-rpm : assembly/.compile-dependencies
	/* see webui README for instructions on how to make a signed package for distribution */ \
	$(call bash, \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" clean rpm:packageBin && \
		mv webui/target/rpm/RPMS/noarch/*.rpm . \
	)

dev_launcher := assembly/target/dev-launcher/pipeline2
ifeq ($(OS), MACOSX)
ifeq ($(shell $(call bash, uname -m)),arm64)
dp2 := cli/build/bin/darwin_arm64/dp2
else
dp2 := cli/build/bin/darwin_amd64/dp2
endif
else
dp2 := cli/build/bin/linux_386/dp2
endif

.PHONY : dp2
dp2 : $(dp2)

.PHONY : run
run : $(dev_launcher)
	exec("$<", "local");

ifneq ($(OS), WINDOWS)
run-with-osgi : export JAVA_REPL_PORT =
endif
.PHONY : run-with-osgi
run-with-osgi : $(dev_launcher)
	exec("$<", "local", "osgi", "shell");

.PHONY : run-cli
run-cli :
	println("dp2 () { test -e $(dp2) || make $(dp2) && curl http://localhost:8181/ws/alive >/dev/null 2>/dev/null || make $(dev_launcher) && $(dp2) --debug false \"$$@\"; }"); \
	println("# Run this command to configure your shell: "); \
	println("# eval $$(make $@)");

.PHONY : run-webui
run-webui : webui/.compile-dependencies
	$(call bash, \
		if [ ! -d webui/dp2webui ]; then cp -r webui/dp2webui-cleandb webui/dp2webui ; fi && \
		cd webui && \
		./activator -Dmvn.settings.localRepository="file:$(CURDIR)/$(MVN_WORKSPACE)" run \
	)

.PHONY : run-docker
run-docker : dist-docker-image
	exec("docker", "run", "-e", "PIPELINE2_WS_HOST=0.0.0.0", \
	                      "-e", "PIPELINE2_WS_AUTHENTICATION=false" \
	                      "-p", "8181:8181", \
	                      "daisyorg/pipeline:latest-snapshot");

.PHONY : run-docker-detached
run-docker-detached : dist-docker-image
	exec("docker", "run", "--name", "pipeline", "--detach", \
	                      "-e", "PIPELINE2_WS_HOST=0.0.0.0", \
	                      "-e", "PIPELINE2_WS_AUTHENTICATION=false" \
	                      "-p", "8181:8181", \
	                      "daisyorg/pipeline:latest-snapshot");

endif # eq ($(OS), WINDOWS)

SCRIPTS := $(filter modules/scripts/%,$(MAVEN_MODULES)) \
           modules/scripts-utils/daisy202-utils \
           modules/scripts-utils/daisy3-utils \
           modules/scripts-utils/dtbook-utils

.PHONY : $(addprefix run-,$(SCRIPTS))
$(addprefix run-,$(SCRIPTS)) : run-% : %/.compile-dependencies %/.test-dependencies
	/* not using -f because that causes log file to be saved at the wrong location */ \
	mvn(new File("$(patsubst run-%,%,$@)"), \
	    "clean", "test", "-Prun-script-webserver");

.PHONY : check

.PHONY : check-clientlib/go
check-clientlib/go :
	exec("$(MAKE)", "-C", "clientlib/go check");

.PHONY : release
release : assembly/.release

.PHONY : $(addprefix check-,$(MODULES) $(MAVEN_AGGREGATORS))
$(addprefix check-,$(MODULES) $(MAVEN_AGGREGATORS)) : check-% : %/.last-tested

pipeline2-$(assembly/VERSION)_linux.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-linux.zip \
	| .group-eval
	+$(EVAL) cp("$<", "$@");

pipeline2-$(assembly/VERSION)_mac.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-mac.zip \
	| .group-eval
	+$(EVAL) cp("$<", "$@");

pipeline2-$(assembly/VERSION)_windows.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-win.zip \
	| .group-eval
	+$(EVAL) cp("$<", "$@");

pipeline2-$(assembly/VERSION)_minimal.zip \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-minimal.zip \
	| .group-eval
	+$(EVAL) cp("$<", "$@");

pipeline2-$(assembly/VERSION)_debian.deb \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).deb \
	| .group-eval
	+$(EVAL) cp("$<", "$@");

pipeline2-$(assembly/VERSION)_redhat.rpm \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION).rpm \
	| .group-eval
	+$(EVAL) cp("$<", "$@");

cli-$(cli/VERSION)-linux_386.deb \
	: $(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/assembly/$(assembly/VERSION)/assembly-$(assembly/VERSION)-cli.deb \
	| .group-eval
	+$(EVAL) cp("$<", "$@");

comma:= ,

$(dev_launcher) : assembly/.compile-dependencies | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "dev-launcher"$(comma) "--"$(comma) "--without-persistence"$(comma) "--with-osgi")

.SECONDARY : assembly/.install.deb
assembly/.install.deb : | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "deb")

.SECONDARY : assembly/.install.rpm
assembly/.install.rpm : | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "rpm")

.SECONDARY : assembly/.install-linux.zip
assembly/.install-linux.zip : | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "zip-linux")

.SECONDARY : assembly/.install-mac.zip
assembly/.install-mac.zip : | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "zip-mac"$(comma) "--"$(comma) "--without-persistence")

.SECONDARY : assembly/.install-win.zip
assembly/.install-win.zip : | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "zip-win"$(comma) "--"$(comma) "--without-persistence")

.SECONDARY : assembly/.install-cli.deb
assembly/.install-cli.deb : | .maven-init .group-eval
	+$(EVAL) $(call make-assembly, "deb-cli")

ifneq ($(OS), WINDOWS)

webui/.deps.mk : webui/build.sbt
	$(call bash, \
		if ! bash .make/make-webui-deps.mk.sh >$@; then \
			echo "\$$(error $@ could not be generated)" >$@; \
		fi \
	)

clean : clean-webui-deps
.PHONY : clean-webui-deps
clean-webui-deps :
	rm("webui/.deps.mk");

endif #eq ($(OS), WINDOWS)

cli/build/bin/darwin_amd64/dp2 cli/build/bin/darwin_arm64/dp2 cli/build/bin/linux_386/dp2 : cli/.install

cli/.install : $(call rwildcard,cli/cli/,*.go) $(call rwildcard,cli/cli/,*.go.in) $(call rwildcard,cli/dp2/,*.go) $(call rwildcard,clientlib/go/,*.go)

.SECONDARY : cli/.install-darwin_amd64.zip cli/.install-linux_386.zip cli/.install-windows_386.zip
cli/.install-darwin_amd64.zip cli/.install-linux_386.zip cli/.install-windows_386.zip : cli/.install

.SECONDARY : cli/.install
cli/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

.SECONDARY : libs/jstyleparser/.install-sources.jar
libs/jstyleparser/.install-sources.jar : libs/jstyleparser/.install

modules/scripts/dtbook-to-odt/.install-doc.jar : $(call rwildcard,modules/scripts/dtbook-to-odt/src/test/,*)

.SECONDARY : \
	modules/braille/liblouis-utils/.install-mac.jar \
	modules/braille/liblouis-utils/.install-linux.jar \
	modules/braille/liblouis-utils/.install-windows.jar
modules/braille/liblouis-utils/.install-mac.jar \
modules/braille/liblouis-utils/.install-linux.jar \
modules/braille/liblouis-utils/.install-windows.jar: \
	modules/braille/liblouis-utils/.install

.SECONDARY : \
	modules/braille/libhyphen-utils/.install-mac.jar \
	modules/braille/libhyphen-utils/.install-linux.jar \
	modules/braille/libhyphen-utils/.install-windows.jar
modules/braille/libhyphen-utils/.install-mac.jar \
modules/braille/libhyphen-utils/.install-linux.jar \
modules/braille/libhyphen-utils/.install-windows.jar: \
	modules/braille/libhyphen-utils/.install

# treat celia-hyphenation-tables as a released artifact
.PHONY : $(MVN_LOCAL_REPOSITORY)/fi/celia/celia-hyphenation-tables/1.5.1-SNAPSHOT/celia-hyphenation-tables-1.5.1-SNAPSHOT.jar
.PHONY : $(MVN_LOCAL_REPOSITORY)/fi/celia/celia-hyphenation-tables/1.5.1-SNAPSHOT!!!/celia-hyphenation-tables-1.5.1-SNAPSHOT!!!.jar

.maven-init : | $(MVN_WORKSPACE)
# the purpose of the test is for making "make -B" not affect this rule (to speed things up)
$(MVN_WORKSPACE) :
	if (!new File("$@").exists()) { \
		mkdirs("$(MVN_CACHE)"); \
		cp("$(MVN_CACHE)", "$@"); \
	}

.PHONY : cache
cache :
	if (!new File("$(MVN_WORKSPACE)").exists()) { \
		err.println("Caching downloaded artifacts..."); \
		rm("$(MVN_CACHE)"); \
		exec("rsync", "-mr", "--exclude", "*-SNAPSHOT", "--exclude", "maven-metadata-*.xml", "$(MVN_WORKSPACE)/", "$(MVN_CACHE)"); \
	}

clean : cache clean-workspace clean-website clean-dist clean-webui

.PHONY : clean-workspace
clean-workspace :
	rm("$(MVN_WORKSPACE)");

.PHONY : clean-dist
clean-dist :
	glob("*.{zip,deb,rpm}").forEach(x -> rm(x));

.PHONY : clean-webui
clean-webui :
	rm("webui/target"); \
	rm("webui/dp2webui"); \
	/* FIXME: run-webui should not store anything in USER_HOME */ \
	rm("$(USER_HOME)/Library/Application Support/DAISY Pipeline 2 Web UI");

.PHONY : gradle-clean
gradle-clean :
	gradle("clean");

.PHONY : checked
checked :
	for (String m : "$(MODULES)".trim().split("\\s+")) \
		touch(new File(f + "/.last-tested");

poms : website/target/maven/pom.xml
website/target/maven/pom.xml : $(addprefix website/src/_data/,modules.yml api.yml versions.yml)
	$(call bash, \
		if which bundle >/dev/null || ! [ -f $@ ]; then \
			$(MAKE) -C website target/maven/pom.xml; \
		fi \
	)

.PHONY : website
website :
	exec("$(MAKE)", "-C", "website");

.PHONY : serve-website publish-website clean-website
serve-website publish-website clean-website :
	exec(env("CLASSPATH", "$(addprefix $(ROOT_DIR)/,$(CLASSPATH))", \
	         "MVN", "$(call eval-java, mvn(commandLineArgs);) --", \
	         "MVN_LOCAL_REPOSITORY", "$(CURDIR)/$(MVN_LOCAL_REPOSITORY)"), \
	     "$(MAKE)", "-C", "website", "$(patsubst %-website,%,$@)");

# this dependency is also defined in website/Makefile, but we need to repeat it here to enable the transitive dependency below
website serve-website publish-website : | $(addprefix website/target/maven/,javadoc doc sources xprocdoc)

$(addprefix website/target/maven/,javadoc doc sources xprocdoc) : website/target/maven/.compile-dependencies
	rm("$@"); \
	exec(env("CLASSPATH", "$(addprefix $(ROOT_DIR)/,$(CLASSPATH))", \
	         "MVN", "$(call eval-java, mvn(commandLineArgs);) --", \
	         "MVN_LOCAL_REPOSITORY", "$(CURDIR)/$(MVN_LOCAL_REPOSITORY)"), \
	     "$(MAKE)", "-C", "website", "$(patsubst website/%,%,$@)");

.PHONY : dump-maven-cmd
dump-maven-cmd :
	$(call bash, \
		echo "mvn () { $(shell $(call bash, dirname "$$(which mvn)"))/mvn --settings \"$(CURDIR)/$(MVN_SETTINGS)\" $(MVN_PROPERTIES) \"\$$@\"; }" && \
		echo '# Run this command to configure your shell: ' && \
		echo '# eval $$(make $@)' \
	)

.PHONY : dump-gradle-cmd
dump-gradle-cmd :
	println("M2_HOME=$(CURDIR)/$(TARGET_DIR)/.gradle-settings $(MY_DIR)/gradlew $(MVN_PROPERTIES)");

.PHONY : help
help :
	err.println("make help:");                                                                       \
	err.println("	Print list of commands");                                                        \
	err.println("make check:");                                                                      \
	err.println("	Incrementally compile and test code");                                           \
	err.println("make dist-deb:");                                                                   \
	err.println("	Incrementally compile code and package into a DEB");                             \
	err.println("make dist-rpm:");                                                                   \
	err.println("	Incrementally compile code and package into a RPM");                             \
	err.println("make dist-zip-linux:");                                                             \
	err.println("	Incrementally compile code and package into a ZIP for Linux");                   \
	err.println("make dist-zip-mac:");                                                               \
	err.println("	Incrementally compile code and package into a ZIP for MacOS");                   \
	err.println("make dist-zip-win:");                                                               \
	err.println("	Incrementally compile code and package into a ZIP for Windows");                 \
	err.println("make dist-docker-image:");                                                          \
	err.println("	Incrementally compile code and package into a Docker image");                    \
	err.println("make dist-cli-deb:");                                                               \
	err.println("	Compile CLI and package into a DEB");                                            \
	err.println("make dist-webui-deb:");                                                             \
	err.println("	Compile Web UI and package into a DEB");                                         \
	err.println("make dist-webui-rpm:");                                                             \
	err.println("	Compile Web UI and package into a RPM");                                         \
	err.println("make run:");                                                                        \
	err.println("	Incrementally compile code and run a server locally");                           \
	err.println("make run-webui:");                                                                  \
	err.println("	Compile and run web UI locally");                                                \
	err.println("make run-cli:");                                                                    \
	err.println("	Get the command for compiling and running CLI locally");                         \
	err.println("make run-docker:");                                                                 \
	err.println("	Incrementally compile code and run a server inside a Docker container");         \
	err.println("make run-modules/scripts/[SCRIPT]:");                                               \
	err.println("	Incrementally compile code and run a server with a single script");              \
	err.println("make website:");                                                                    \
	err.println("	Build the website");                                                             \
	err.println("make dump-maven-cmd:");                                                             \
	err.println("	Get the Maven command used. To configure your shell: eval $$(make dump-maven-cmd)");
