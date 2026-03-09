modules/scripts/dtbook-to-rtf/.test modules/scripts/dtbook-to-rtf/.install modules/scripts/dtbook-to-rtf/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-to-rtf/modified-since-release_ : \
	modules/scripts/dtbook-to-rtf/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_encode.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_hyperlink_bookmarks.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_core.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/convert.xpl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook-to-rtf.xpl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_styles.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/number-lists.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/number-lists.scss \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_metadata.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_table.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/add_ids_to_dtbook.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_note_and_anno.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_list.xsl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/library.xpl \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_images.xsl
modules/scripts/dtbook-to-rtf/.test modules/scripts/dtbook-to-rtf/.install-doc : \
	modules/scripts/dtbook-to-rtf/src/test/xspec/unittest.xspec \
	modules/scripts/dtbook-to-rtf/src/test/resources/logback.xml \
	modules/scripts/dtbook-to-rtf/src/test/java/XProcSpecTest.java \
	modules/scripts/dtbook-to-rtf/src/test/xprocspec/test_dtbook-to-rtf.xprocspec \
	modules/scripts/dtbook-to-rtf/src/test/xprocspec/test_dtbook-to-rtf.script.xprocspec
modules/scripts/dtbook-to-rtf/.install-doc : \
	modules/scripts/dtbook-to-rtf/doc/index.md \
	modules/scripts/dtbook-to-rtf/doc/example-rtf.html
.make/mk/modules/scripts/dtbook-to-rtf/sources.mk : \
	modules/scripts/dtbook-to-rtf/src \
	modules/scripts/dtbook-to-rtf/src/test \
	modules/scripts/dtbook-to-rtf/src/test/xspec \
	modules/scripts/dtbook-to-rtf/src/test/resources \
	modules/scripts/dtbook-to-rtf/src/test/java \
	modules/scripts/dtbook-to-rtf/src/test/xprocspec \
	modules/scripts/dtbook-to-rtf/src/main \
	modules/scripts/dtbook-to-rtf/src/main/resources \
	modules/scripts/dtbook-to-rtf/src/main/resources/META-INF \
	modules/scripts/dtbook-to-rtf/src/main/resources/xml \
	modules/scripts/dtbook-to-rtf/doc
