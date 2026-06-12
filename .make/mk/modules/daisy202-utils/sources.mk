modules/daisy202-utils/.test modules/daisy202-utils/.install modules/daisy202-utils/.install-doc $(TARGET_DIR)/state/modules/daisy202-utils/modified-since-release_ : \
	modules/daisy202-utils/src/main/resources/META-INF/catalog.xml \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/daisy202-validator.script.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/steps/validate.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/steps/validate.check-heading-hierarchy.xsl \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/steps/validate.check-references.xsl \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/steps/validate.smil-times-2.xsl \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/steps/validate.smil-times-1.xsl \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/i18n.xml \
	modules/daisy202-utils/src/main/resources/xml/xproc/fix-audio-file-order.script.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/rename-files.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/fix-audio-file-order.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/load/load.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/load/ncc-to-smil-fileset.xsl \
	modules/daisy202-utils/src/main/resources/xml/xproc/audio-transcode.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/smils-in-reading-order.xsl \
	modules/daisy202-utils/src/main/resources/xml/xproc/daisy202-library.xpl \
	modules/daisy202-utils/src/main/resources/xml/xproc/audio-files-in-reading-order.xsl \
	modules/daisy202-utils/src/main/resources/xml/xproc/update-links.xpl \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202msmil.rng \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202ncc.rng \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202discinfo.dtd \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202meta.rng \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202msmil.dtd \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202nccmulti.dtd \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202ncc.dtd \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202smil.dtd \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202/d202smil.rng \
	modules/daisy202-utils/src/main/resources/xml/schemas/relaxngcommon/datatypes.rng \
	modules/daisy202-utils/src/main/resources/xml/schemas/relaxngcommon/attributes.rng \
	modules/daisy202-utils/src/main/resources/xml/xslt/library.xsl
modules/daisy202-utils/.test modules/daisy202-utils/.install-doc : \
	modules/daisy202-utils/src/test/resources/logback.xml \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0001.smil \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0006.smil \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0007.smil \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0004.smil \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0003.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0002.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0001.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/default.css \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0005.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0004.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/tpbnarrator_res.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0006.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0007.mp3 \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0005.smil \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0002.smil \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0003.smil \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/content.html \
	modules/daisy202-utils/src/test/resources/dontworrybehappy/ncc.html \
	modules/daisy202-utils/src/test/resources/audio.wav \
	modules/daisy202-utils/src/test/java/XProcSpecTest.java \
	modules/daisy202-utils/src/test/xprocspec/test_fix-audio-file-order.xprocspec \
	modules/daisy202-utils/src/test/xprocspec/test_daisy202-validator.script.xprocspec \
	modules/daisy202-utils/src/test/xprocspec/test_audio-transcode.xprocspec \
	modules/daisy202-utils/src/test/xprocspec/test_validate-ncc.xprocspec \
	modules/daisy202-utils/src/test/xprocspec/test_load.xprocspec \
	modules/daisy202-utils/src/test/xprocspec/test_daisy202-validator.xprocspec
modules/daisy202-utils/.install-doc : \
	modules/daisy202-utils/doc/daisy202-validator.md \
	modules/daisy202-utils/doc/index.md \
	modules/daisy202-utils/doc/daisy202-unscrambler.md
.make/mk/modules/daisy202-utils/sources.mk : \
	modules/daisy202-utils/src \
	modules/daisy202-utils/src/test \
	modules/daisy202-utils/src/test/resources \
	modules/daisy202-utils/src/test/resources/dontworrybehappy \
	modules/daisy202-utils/src/test/java \
	modules/daisy202-utils/src/test/xprocspec \
	modules/daisy202-utils/src/main \
	modules/daisy202-utils/src/main/resources \
	modules/daisy202-utils/src/main/resources/META-INF \
	modules/daisy202-utils/src/main/resources/xml \
	modules/daisy202-utils/src/main/resources/xml/xproc \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate \
	modules/daisy202-utils/src/main/resources/xml/xproc/validate/steps \
	modules/daisy202-utils/src/main/resources/xml/xproc/load \
	modules/daisy202-utils/src/main/resources/xml/schemas \
	modules/daisy202-utils/src/main/resources/xml/schemas/d202 \
	modules/daisy202-utils/src/main/resources/xml/schemas/relaxngcommon \
	modules/daisy202-utils/src/main/resources/xml/xslt \
	modules/daisy202-utils/doc
