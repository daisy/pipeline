modules/dtbook-to-daisy3/.test modules/dtbook-to-daisy3/.install modules/dtbook-to-daisy3/.install-doc $(TARGET_DIR)/state/modules/dtbook-to-daisy3/modified-since-release_ : \
	modules/dtbook-to-daisy3/src/main/resources/images/math_formulae.png \
	modules/dtbook-to-daisy3/src/main/resources/META-INF/catalog.xml \
	modules/dtbook-to-daisy3/src/main/resources/xml/dtbook-to-daisy3.convert.xpl \
	modules/dtbook-to-daisy3/src/main/resources/xml/mathml-fallback.xsl \
	modules/dtbook-to-daisy3/src/main/resources/xml/dtbook-to-daisy3.xpl \
	modules/dtbook-to-daisy3/src/main/resources/xml/library.xpl
modules/dtbook-to-daisy3/.test modules/dtbook-to-daisy3/.install-doc : \
	modules/dtbook-to-daisy3/src/test/resources/tts-config.xml \
	modules/dtbook-to-daisy3/src/test/resources/logback.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_5696_xmldtbook_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_programme_tv.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/dtbook.2005.basic.css \
	modules/dtbook-to-daisy3/src/test/resources/samples/theme.css \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_4867.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_philo.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_21410_xmldtbook_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_5857_xmldtbook_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_economiedesetatsunisl_baudchon_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_7277.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_11007_xmldtbook_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_19986_xmldtbook_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_10312_philo.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_7019_xmldtbook_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_9400_xmldtbook_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/skippable.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_9868_intro_droit.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_10483.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/dtbookbasic.css \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_6776.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/style.css \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_1724.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_ideedusieclel_pennac_1.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/minimal.xml \
	modules/dtbook-to-daisy3/src/test/resources/samples/image.png \
	modules/dtbook-to-daisy3/src/test/resources/samples/shuffled_1449_dune_herbert.xml \
	modules/dtbook-to-daisy3/src/test/java/XProcSpecTest.java \
	modules/dtbook-to-daisy3/src/test/java/org/daisy/pipeline/dtbook2daisy3/FullConversionTest.java \
	modules/dtbook-to-daisy3/src/test/xprocspec/test_dtbook-to-daisy3.xprocspec \
	modules/dtbook-to-daisy3/src/test/xprocspec/test_dtbook-to-daisy3.script.xprocspec
modules/dtbook-to-daisy3/.install-doc : \
	modules/dtbook-to-daisy3/doc/index.md
.make/mk/modules/dtbook-to-daisy3/sources.mk : \
	modules/dtbook-to-daisy3/src \
	modules/dtbook-to-daisy3/src/test \
	modules/dtbook-to-daisy3/src/test/resources \
	modules/dtbook-to-daisy3/src/test/resources/samples \
	modules/dtbook-to-daisy3/src/test/java \
	modules/dtbook-to-daisy3/src/test/java/org \
	modules/dtbook-to-daisy3/src/test/java/org/daisy \
	modules/dtbook-to-daisy3/src/test/java/org/daisy/pipeline \
	modules/dtbook-to-daisy3/src/test/java/org/daisy/pipeline/dtbook2daisy3 \
	modules/dtbook-to-daisy3/src/test/xprocspec \
	modules/dtbook-to-daisy3/src/main \
	modules/dtbook-to-daisy3/src/main/resources \
	modules/dtbook-to-daisy3/src/main/resources/images \
	modules/dtbook-to-daisy3/src/main/resources/META-INF \
	modules/dtbook-to-daisy3/src/main/resources/xml \
	modules/dtbook-to-daisy3/doc
