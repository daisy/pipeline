framework/logging-appender/.test framework/logging-appender/.install framework/logging-appender/.install-doc $(TARGET_DIR)/state/framework/logging-appender/modified-since-release_ : \
	framework/logging-appender/src/main/resources/org/daisy/pipeline/logging/appenders.xml \
	framework/logging-appender/src/main/java/org/daisy/pipeline/logging/JobProgressAppender.java \
	framework/logging-appender/src/main/java/org/daisy/pipeline/logging/JobLogFileAppender.java \
	framework/logging-appender/src/main/java/org/daisy/pipeline/logging/ThresholdFilter.java \
	framework/logging-appender/src/main/java/org/daisy/pipeline/logging/package-info.java
.make/mk/framework/logging-appender/sources.mk : \
	framework/logging-appender/src \
	framework/logging-appender/src/main \
	framework/logging-appender/src/main/resources \
	framework/logging-appender/src/main/resources/org \
	framework/logging-appender/src/main/resources/org/daisy \
	framework/logging-appender/src/main/resources/org/daisy/pipeline \
	framework/logging-appender/src/main/resources/org/daisy/pipeline/logging \
	framework/logging-appender/src/main/java \
	framework/logging-appender/src/main/java/org \
	framework/logging-appender/src/main/java/org/daisy \
	framework/logging-appender/src/main/java/org/daisy/pipeline \
	framework/logging-appender/src/main/java/org/daisy/pipeline/logging
