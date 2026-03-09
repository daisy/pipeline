modules/scripts-utils/daisy202-utils/.test modules/scripts-utils/daisy202-utils/.install modules/scripts-utils/daisy202-utils/.install-doc $(TARGET_DIR)/state/modules/scripts-utils/daisy202-utils/modified-since-release_ : \
	modules/scripts-utils/daisy202-utils/src/main/resources/META-INF/catalog.xml \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/fix-audio-file-order.script.xpl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/rename-files.xpl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/fix-audio-file-order.xpl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/load/load.xpl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/load/ncc-to-smil-fileset.xsl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/audio-transcode.xpl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/smils-in-reading-order.xsl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/daisy202-library.xpl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/audio-files-in-reading-order.xsl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/update-links.xpl \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202msmil.rng \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202ncc.rng \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202discinfo.dtd \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202meta.rng \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202msmil.dtd \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202nccmulti.dtd \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202ncc.dtd \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202smil.dtd \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202/d202smil.rng \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/relaxngcommon/datatypes.rng \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/relaxngcommon/attributes.rng \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xslt/library.xsl
modules/scripts-utils/daisy202-utils/.test modules/scripts-utils/daisy202-utils/.install-doc : \
	modules/scripts-utils/daisy202-utils/src/test/resources/logback.xml \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0001.smil \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0006.smil \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0007.smil \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0004.smil \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0003.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0002.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0001.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/default.css \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0005.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0004.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/tpbnarrator_res.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0006.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0007.mp3 \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0005.smil \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0002.smil \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/speechgen0003.smil \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/content.html \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy/ncc.html \
	modules/scripts-utils/daisy202-utils/src/test/resources/audio.wav \
	modules/scripts-utils/daisy202-utils/src/test/java/XProcSpecTest.java \
	modules/scripts-utils/daisy202-utils/src/test/xprocspec/test_fix-audio-file-order.xprocspec \
	modules/scripts-utils/daisy202-utils/src/test/xprocspec/test_audio-transcode.xprocspec \
	modules/scripts-utils/daisy202-utils/src/test/xprocspec/test_load.xprocspec
modules/scripts-utils/daisy202-utils/.install-doc : \
	modules/scripts-utils/daisy202-utils/doc/index.md
.make/mk/modules/scripts-utils/daisy202-utils/sources.mk : \
	modules/scripts-utils/daisy202-utils/src \
	modules/scripts-utils/daisy202-utils/src/test \
	modules/scripts-utils/daisy202-utils/src/test/resources \
	modules/scripts-utils/daisy202-utils/src/test/resources/dontworrybehappy \
	modules/scripts-utils/daisy202-utils/src/test/java \
	modules/scripts-utils/daisy202-utils/src/test/xprocspec \
	modules/scripts-utils/daisy202-utils/src/main \
	modules/scripts-utils/daisy202-utils/src/main/resources \
	modules/scripts-utils/daisy202-utils/src/main/resources/META-INF \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xproc/load \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/d202 \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/schemas/relaxngcommon \
	modules/scripts-utils/daisy202-utils/src/main/resources/xml/xslt \
	modules/scripts-utils/daisy202-utils/doc
