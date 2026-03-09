framework/persistence-mysql/.test framework/persistence-mysql/.install framework/persistence-mysql/.install-doc $(TARGET_DIR)/state/framework/persistence-mysql/modified-since-release_ : \
	framework/persistence-mysql/src/main/java/org/daisy/pipeline/persistence/impl/mysql/MySQLEntityManagerFactory.java
.make/mk/framework/persistence-mysql/sources.mk : \
	framework/persistence-mysql/src \
	framework/persistence-mysql/src/main \
	framework/persistence-mysql/src/main/java \
	framework/persistence-mysql/src/main/java/org \
	framework/persistence-mysql/src/main/java/org/daisy \
	framework/persistence-mysql/src/main/java/org/daisy/pipeline \
	framework/persistence-mysql/src/main/java/org/daisy/pipeline/persistence \
	framework/persistence-mysql/src/main/java/org/daisy/pipeline/persistence/impl \
	framework/persistence-mysql/src/main/java/org/daisy/pipeline/persistence/impl/mysql
