modules/braille/libhyphen-utils/VERSION := 3.5.1-SNAPSHOT

$(TARGET_DIR)/state/modules/braille/libhyphen-utils/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

# this rule overrides the implicit rule in main.mk
# note that because the modified-since-release_ files created by main.mk, are deleted,
# this rule gets executed at least once
$(TARGET_DIR)/state/modules/braille/libhyphen-utils/modified-since-release_ : modules/braille/libhyphen-utils/pom.xml \
	$(TARGET_DIR)/state/modules/parent/modified-since-release \
	$(TARGET_DIR)/state/framework/common-utils/modified-since-release \
	$(TARGET_DIR)/state/modules/braille/braille-common/modified-since-release \
	$(TARGET_DIR)/state/framework/modules-registry/modified-since-release \
	$(TARGET_DIR)/state/utils/build-utils/modules-test-helper/modified-since-release \
	$(TARGET_DIR)/state/framework/logging-appender/modified-since-release \
	$(TARGET_DIR)/state/framework/webservice/modified-since-release \
	$(TARGET_DIR)/state/framework/calabash-adapter/modified-since-release
	mkdirs("$(dir $@)"); \
	try (OutputStream s = new FileOutputStream("$@")) { \
		ModificationType modified = isModifiedSinceLastRelease(new File("$<").getParentFile()); \
		if (modified == null) \
			for (String d : "$(filter %/modified-since-release,$^)".trim().split("\\s+")) \
				if ("major".equals(slurp(new File(d)).trim())) { \
					modified = ModificationType.PATCH; \
					break; } \
		new PrintStream(s).print("" + modified); }

.SECONDARY : modules/braille/libhyphen-utils/.test
modules/braille/libhyphen-utils/.test : | .maven-init .group-eval
	+$(EVAL) mvn.test("$(patsubst %/,%,$(dir $@))");

modules/braille/libhyphen-utils/.test : %/.test : %/pom.xml %/.compile-dependencies %/.test-dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1-SNAPSHOT/libhyphen-utils-3.5.1-SNAPSHOT.pom : modules/braille/libhyphen-utils/.install.pom | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1-SNAPSHOT/libhyphen-utils-3.5.1-SNAPSHOT% : modules/braille/libhyphen-utils/.install% | .group-eval
	+$(EVAL) if (new File("$@").exists()) touch("$@"); else exit(1);

.SECONDARY : modules/braille/libhyphen-utils/.install.pom
modules/braille/libhyphen-utils/.install.pom : | .maven-init .group-eval
	+$(EVAL) mvn.installPom("modules/braille/libhyphen-utils");

modules/braille/libhyphen-utils/.install.pom : %/.install.pom : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/braille/libhyphen-utils/.install.jar
modules/braille/libhyphen-utils/.install.jar : %/.install.jar : %/.install

.SECONDARY : modules/braille/libhyphen-utils/.install
modules/braille/libhyphen-utils/.install : | .maven-init .group-eval
	+$(EVAL) mvn.install("$(patsubst %/,%,$(dir $@))");

modules/braille/libhyphen-utils/.install : %/.install : %/pom.xml %/.compile-dependencies | %/.test-dependencies

.SECONDARY : modules/braille/libhyphen-utils/.install-doc.jar
modules/braille/libhyphen-utils/.install-doc.jar : %/.install-doc.jar : %/.install-doc

.SECONDARY : modules/braille/libhyphen-utils/.install-xprocdoc.jar
modules/braille/libhyphen-utils/.install-xprocdoc.jar : %/.install-xprocdoc.jar : %/.install-doc

.SECONDARY : modules/braille/libhyphen-utils/.install-javadoc.jar
modules/braille/libhyphen-utils/.install-javadoc.jar : %/.install-javadoc.jar : %/.install-doc

.SECONDARY : modules/braille/libhyphen-utils/.install-doc
modules/braille/libhyphen-utils/.install-doc : | .maven-init .group-eval
	+$(EVAL) mvn.installDoc("$(patsubst %/,%,$(dir $@))");

modules/braille/libhyphen-utils/.install-doc : %/.install-doc : %/pom.xml | %/.compile-dependencies %/.test-dependencies

.SECONDARY : modules/braille/libhyphen-utils/.compile-dependencies modules/braille/libhyphen-utils/.test-dependencies
modules/braille/libhyphen-utils/.compile-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5-SNAPSHOT/modules-parent-1.15.5-SNAPSHOT.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.1-SNAPSHOT/braille-common-7.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1-SNAPSHOT/common-utils-3.4.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98-SNAPSHOT/com.xmlcalabash-1.1.20-p20-98-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1-SNAPSHOT/calabash-adapter-7.1.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1-SNAPSHOT/css-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1-SNAPSHOT/file-utils-5.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1-SNAPSHOT/fileset-utils-8.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11-SNAPSHOT/zip-utils-2.1.11-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.2-SNAPSHOT/braille-css-utils-5.0.2-SNAPSHOT.jar
modules/braille/libhyphen-utils/.test-dependencies : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2-SNAPSHOT/modules-registry-5.0.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1-SNAPSHOT/common-utils-6.6.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0-SNAPSHOT/modules-test-helper-3.0.0-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2-SNAPSHOT/saxon-adapter-5.8.2-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8-SNAPSHOT/logging-appender-2.1.8-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1-SNAPSHOT/framework-core-12.0.1-SNAPSHOT.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1-SNAPSHOT/webservice-4.0.1-SNAPSHOT.jar

$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1/libhyphen-utils-3.5.1.% \
$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/libhyphen-utils/3.5.1/libhyphen-utils-3.5.1-% : modules/braille/libhyphen-utils/.release
	+//

.SECONDARY : modules/braille/libhyphen-utils/.release
modules/braille/libhyphen-utils/.release : modules/.release
	+$(EVAL) mvn.releaseModulesInDir("modules").apply("braille/libhyphen-utils");

modules/braille/libhyphen-utils/.release : \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/modules-parent/1.15.5/modules-parent-1.15.5.pom \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/common-utils/6.6.1/common-utils-6.6.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-common/7.0.1/braille-common-7.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/common-utils/3.4.1/common-utils-3.4.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98/com.xmlcalabash-1.1.20-p20-98.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/calabash-adapter/7.1.1/calabash-adapter-7.1.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/saxon-adapter/5.8.2/saxon-adapter-5.8.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules-registry/5.0.2/modules-registry-5.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/css-utils/8.0.1/css-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/file-utils/5.0.1/file-utils-5.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/fileset-utils/8.0.1/fileset-utils-8.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/zip-utils/2.1.11/zip-utils-2.1.11.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/framework-core/12.0.1/framework-core-12.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/webservice/4.0.1/webservice-4.0.1.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/modules/braille/braille-css-utils/5.0.2/braille-css-utils-5.0.2.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/build/modules-test-helper/3.0.0/modules-test-helper-3.0.0.jar \
	$(MVN_LOCAL_REPOSITORY)/org/daisy/pipeline/logging-appender/2.1.8/logging-appender-2.1.8.jar

clean : modules/braille/libhyphen-utils/.clean
.PHONY : modules/braille/libhyphen-utils/.clean
modules/braille/libhyphen-utils/.clean :
	rm("modules/braille/libhyphen-utils/target");
