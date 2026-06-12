modules/dtbook-to-rtf/.test modules/dtbook-to-rtf/.install modules/dtbook-to-rtf/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-rtf/modified-since-release_ : \
	modules/dtbook-to-rtf/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_encode.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_hyperlink_bookmarks.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_core.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/convert.xpl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook-to-rtf.xpl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_styles.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/number-lists.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/number-lists.scss \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_metadata.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_table.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/add_ids_to_dtbook.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_note_and_anno.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_list.xsl \
	modules/dtbook-to-rtf/src/main/resources/xml/library.xpl \
	modules/dtbook-to-rtf/src/main/resources/xml/dtbook_to_rtf_images.xsl
modules/dtbook-to-rtf/.test modules/dtbook-to-rtf/.install-doc : \
	modules/dtbook-to-rtf/src/test/xspec/unittest.xspec \
	modules/dtbook-to-rtf/src/test/resources/logback.xml \
	modules/dtbook-to-rtf/src/test/java/XProcSpecTest.java \
	modules/dtbook-to-rtf/src/test/xprocspec/test_dtbook-to-rtf.xprocspec \
	modules/dtbook-to-rtf/src/test/xprocspec/test_dtbook-to-rtf.script.xprocspec
modules/dtbook-to-rtf/.install-doc : \
	modules/dtbook-to-rtf/doc/index.md \
	modules/dtbook-to-rtf/doc/example-rtf.html
.make/mk/modules/dtbook-to-rtf/sources.mk : \
	modules/dtbook-to-rtf/src \
	modules/dtbook-to-rtf/src/test \
	modules/dtbook-to-rtf/src/test/xspec \
	modules/dtbook-to-rtf/src/test/resources \
	modules/dtbook-to-rtf/src/test/java \
	modules/dtbook-to-rtf/src/test/xprocspec \
	modules/dtbook-to-rtf/src/main \
	modules/dtbook-to-rtf/src/main/resources \
	modules/dtbook-to-rtf/src/main/resources/META-INF \
	modules/dtbook-to-rtf/src/main/resources/xml \
	modules/dtbook-to-rtf/doc
