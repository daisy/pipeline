modules/libhyphen-utils/.test modules/libhyphen-utils/.install modules/libhyphen-utils/.install-doc $(TARGET_DIR)/state/modules/libhyphen-utils/modified-since-release_ : \
	modules/libhyphen-utils/src/main/resources/tables/hyph_ru_RU.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_et_EE.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_pt_PT.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_bg_BG.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_sl_SI.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_hu_HU.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_sr.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_no_NO.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_fr.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_da_DK.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_pt_BR.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_en_US.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_es_ANY.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_sk_SK.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_gl.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_fi.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_sh.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_lt_LT.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_hr_HR.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_lv_LV.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_zu_ZA.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_uk_UA.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_en_GB.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_it_IT.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_ro_RO.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_nl_NL.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_el_GR.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_pl_PL.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_te_IN.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_cs_CZ.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_af_ZA.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_ca.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_nb_NO.dic \
	modules/libhyphen-utils/src/main/resources/tables/hyph_nn_NO.dic \
	modules/libhyphen-utils/src/main/resources/META-INF/catalog.xml \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/LibhyphenTableRegistry.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/mac/LibhyphenNativePathForMacOS.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/linux/LibhyphenNativePathForLinux.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/windows/LibhyphenNativePathForWindows.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/LibhyphenJnaImpl.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenTablePath.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenHyphenator.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenTableResolver.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenTableProvider.java \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/package-info.java \
	modules/libhyphen-utils/src/main/README.md
modules/libhyphen-utils/.test modules/libhyphen-utils/.install-doc : \
	modules/libhyphen-utils/src/test/xspec/.gitkeep \
	modules/libhyphen-utils/src/test/resources/logback.xml \
	modules/libhyphen-utils/src/test/resources/tables/standard.dic \
	modules/libhyphen-utils/src/test/resources/tables/non-standard.dic \
	modules/libhyphen-utils/src/test/resources/phony.xsl \
	modules/libhyphen-utils/src/test/resources/foobar.dic \
	modules/libhyphen-utils/src/test/java/TablePath.java \
	modules/libhyphen-utils/src/test/java/ignore \
	modules/libhyphen-utils/src/test/java/XSpecAndXProcSpecTest.java \
	modules/libhyphen-utils/src/test/java/LibhyphenCoreTest.java
modules/libhyphen-utils/.install-doc : \
	modules/libhyphen-utils/doc/index.md
.make/mk/modules/libhyphen-utils/sources.mk : \
	modules/libhyphen-utils/src \
	modules/libhyphen-utils/src/test \
	modules/libhyphen-utils/src/test/xspec \
	modules/libhyphen-utils/src/test/resources \
	modules/libhyphen-utils/src/test/resources/tables \
	modules/libhyphen-utils/src/test/java \
	modules/libhyphen-utils/src/main \
	modules/libhyphen-utils/src/main/resources \
	modules/libhyphen-utils/src/main/resources/tables \
	modules/libhyphen-utils/src/main/resources/META-INF \
	modules/libhyphen-utils/src/main/java \
	modules/libhyphen-utils/src/main/java/org \
	modules/libhyphen-utils/src/main/java/org/daisy \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/mac \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/linux \
	modules/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/windows \
	modules/libhyphen-utils/doc
