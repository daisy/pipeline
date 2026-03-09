modules/braille/libhyphen-utils/.test modules/braille/libhyphen-utils/.install modules/braille/libhyphen-utils/.install-doc $(TARGET_DIR)/state/modules/braille/libhyphen-utils/modified-since-release_ : \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_ru_RU.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_et_EE.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_pt_PT.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_bg_BG.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_sl_SI.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_hu_HU.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_sr.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_no_NO.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_fr.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_da_DK.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_pt_BR.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_en_US.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_es_ANY.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_sk_SK.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_gl.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_fi.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_sh.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_lt_LT.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_hr_HR.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_lv_LV.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_zu_ZA.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_uk_UA.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_en_GB.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_it_IT.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_ro_RO.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_nl_NL.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_el_GR.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_pl_PL.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_te_IN.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_cs_CZ.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_af_ZA.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_ca.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_nb_NO.dic \
	modules/braille/libhyphen-utils/src/main/resources/tables/hyph_nn_NO.dic \
	modules/braille/libhyphen-utils/src/main/resources/META-INF/catalog.xml \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/LibhyphenTableRegistry.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/mac/LibhyphenNativePathForMacOS.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/linux/LibhyphenNativePathForLinux.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/windows/LibhyphenNativePathForWindows.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/LibhyphenJnaImpl.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenTablePath.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenHyphenator.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenTableResolver.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/LibhyphenTableProvider.java \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/package-info.java \
	modules/braille/libhyphen-utils/src/main/README.md
modules/braille/libhyphen-utils/.test modules/braille/libhyphen-utils/.install-doc : \
	modules/braille/libhyphen-utils/src/test/xspec/.gitkeep \
	modules/braille/libhyphen-utils/src/test/resources/logback.xml \
	modules/braille/libhyphen-utils/src/test/resources/tables/standard.dic \
	modules/braille/libhyphen-utils/src/test/resources/tables/non-standard.dic \
	modules/braille/libhyphen-utils/src/test/resources/OSGI-INF/table-path.xml \
	modules/braille/libhyphen-utils/src/test/resources/phony.xsl \
	modules/braille/libhyphen-utils/src/test/resources/foobar.dic \
	modules/braille/libhyphen-utils/src/test/java/TablePath.java \
	modules/braille/libhyphen-utils/src/test/java/ignore \
	modules/braille/libhyphen-utils/src/test/java/XSpecAndXProcSpecTest.java \
	modules/braille/libhyphen-utils/src/test/java/LibhyphenCoreTest.java
modules/braille/libhyphen-utils/.install-doc : \
	modules/braille/libhyphen-utils/doc/index.md
.make/mk/modules/braille/libhyphen-utils/sources.mk : \
	modules/braille/libhyphen-utils/src \
	modules/braille/libhyphen-utils/src/test \
	modules/braille/libhyphen-utils/src/test/xspec \
	modules/braille/libhyphen-utils/src/test/resources \
	modules/braille/libhyphen-utils/src/test/resources/tables \
	modules/braille/libhyphen-utils/src/test/resources/OSGI-INF \
	modules/braille/libhyphen-utils/src/test/java \
	modules/braille/libhyphen-utils/src/main \
	modules/braille/libhyphen-utils/src/main/resources \
	modules/braille/libhyphen-utils/src/main/resources/tables \
	modules/braille/libhyphen-utils/src/main/resources/META-INF \
	modules/braille/libhyphen-utils/src/main/java \
	modules/braille/libhyphen-utils/src/main/java/org \
	modules/braille/libhyphen-utils/src/main/java/org/daisy \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/mac \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/linux \
	modules/braille/libhyphen-utils/src/main/java/org/daisy/pipeline/braille/libhyphen/impl/windows \
	modules/braille/libhyphen-utils/doc
