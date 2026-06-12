libs/com.xmlcalabash/VERSION := 1.1.20-p20-98

$(TARGET_DIR)/state/libs/com.xmlcalabash/last-tested : $(TARGET_DIR)/state/%/last-tested : %/.test | .group-eval
	+$(EVAL) mkdirs("$(dir $@)"); touch("$@");

.SECONDARY : libs/com.xmlcalabash/.test
libs/com.xmlcalabash/.test : | .gradle-init .group-eval
	+$(EVAL) gradle.test("libs/com.xmlcalabash");

libs/com.xmlcalabash/.test : %/.test : %/build.gradle %/gradle.properties %/.dependencies

$(MVN_LOCAL_REPOSITORY)/org/daisy/libs/com.xmlcalabash/1.1.20-p20-98/com.xmlcalabash-1.1.20-p20-98.jar : libs/com.xmlcalabash/.release

.SECONDARY : libs/com.xmlcalabash/.release
libs/com.xmlcalabash/.release :
