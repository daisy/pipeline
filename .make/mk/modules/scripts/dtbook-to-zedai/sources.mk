modules/scripts/dtbook-to-zedai/.test modules/scripts/dtbook-to-zedai/.install modules/scripts/dtbook-to-zedai/.install-doc $(TARGET_DIR)/state/modules/scripts/dtbook-to-zedai/modified-since-release_ : \
	modules/scripts/dtbook-to-zedai/src/main/resources/META-INF/catalog.xml \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/dtbook-to-zedai.convert.xpl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/dtbook-to-zedai-meta.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/fix-dtbook.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/dtbook-to-zedai-meta.xpl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-sidebar.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-list.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/remove-tmp-attributes.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-epigraph.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-code.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-annotation.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/translate-mathml-to-zedai.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-note.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-poem.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/generate-css.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/rename-code-kbd.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-generic.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-imggroup.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/normalize-block-inline.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-deflist.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/group-deflist-contents.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/translate-elems-attrs-to-zedai.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/rename-to-span.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-table.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-div.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-linegroup.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/README.txt \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/moveout-prodnote.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/rename-annotation.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/normalize-section-block.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/add-ref-to-annotations.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/convert-linebreaks.xsl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/dtbook-to-zedai.xpl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/library.xpl \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml/dtbook2005-3-to-zedai.xpl
modules/scripts/dtbook-to-zedai/.test modules/scripts/dtbook-to-zedai/.install-doc : \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-table_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-sidebar_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/normalize-section-block_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/rename-to-span_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/normalize-block-inline_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-with-links_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-linegroup_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-deflist_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-annotation_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/add-ref-to-annotations_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-list_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/linebreaks_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-poem_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-epigraph_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/translate-elems-attrs-to-zedai_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-code_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/group-deflist-contents_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/translate-elems-attrs-to-zedai_test_failures.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/rename-annotation_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-note_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/translate-mathml-to-zedai_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/rename-code-kbd_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-imggroup_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-div_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/xspec/moveout-prodnote_test.xspec \
	modules/scripts/dtbook-to-zedai/src/test/resources/logback.xml \
	modules/scripts/dtbook-to-zedai/src/test/resources/minimal.xml \
	modules/scripts/dtbook-to-zedai/src/test/resources/image.jpg \
	modules/scripts/dtbook-to-zedai/src/test/java/XProcSpecTest.java \
	modules/scripts/dtbook-to-zedai/src/test/xprocspec/test_dtbook-to-zedai.xprocspec \
	modules/scripts/dtbook-to-zedai/src/test/xprocspec/test_dtbook-to-zedai.script.xprocspec
modules/scripts/dtbook-to-zedai/.install-doc : \
	modules/scripts/dtbook-to-zedai/doc/dev-notes.md \
	modules/scripts/dtbook-to-zedai/doc/index.md \
	modules/scripts/dtbook-to-zedai/doc/rules.md
.make/mk/modules/scripts/dtbook-to-zedai/sources.mk : \
	modules/scripts/dtbook-to-zedai/src \
	modules/scripts/dtbook-to-zedai/src/test \
	modules/scripts/dtbook-to-zedai/src/test/xspec \
	modules/scripts/dtbook-to-zedai/src/test/resources \
	modules/scripts/dtbook-to-zedai/src/test/java \
	modules/scripts/dtbook-to-zedai/src/test/xprocspec \
	modules/scripts/dtbook-to-zedai/src/main \
	modules/scripts/dtbook-to-zedai/src/main/resources \
	modules/scripts/dtbook-to-zedai/src/main/resources/META-INF \
	modules/scripts/dtbook-to-zedai/src/main/resources/xml \
	modules/scripts/dtbook-to-zedai/doc
