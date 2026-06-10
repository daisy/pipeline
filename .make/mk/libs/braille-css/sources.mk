libs/braille-css/.test libs/braille-css/.install libs/braille-css/.install-doc $(TARGET_DIR)/state/libs/braille-css/modified-since-release_ : \
	libs/braille-css/src/main/java/org/daisy/braille/css/RuleTextTransform.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/InlineStyle.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/TermDotPattern.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/SimpleInlineStyle.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/RuleVolumeArea.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/SupportedBrailleCSS.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/Preparator.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/BrailleCSSExtension.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/CombinedSelectorImpl.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/BrailleCSSProperty.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/SelectorImpl.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/VendorAtRule.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/PropertyValue.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/RuleMarginImpl.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/LanguageTag.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/LanguageRange.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/Dimension.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/RuleHyphenationResource.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/BrailleCSSRuleFactory.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/BrailleCSSParserFactory.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/RuleVolume.java \
	libs/braille-css/src/main/java/org/daisy/braille/css/RuleCounterStyle.java \
	libs/braille-css/src/main/antlr3/org/daisy/braille/css/BrailleCSSTreeParser.g \
	libs/braille-css/src/main/antlr3/org/daisy/braille/css/BrailleCSSParser.g \
	libs/braille-css/src/main/antlr3/org/daisy/braille/css/BrailleCSSLexer.g
libs/braille-css/.test libs/braille-css/.install-doc : \
	libs/braille-css/src/test/java/VolumesTest.java \
	libs/braille-css/src/test/java/InlineStyleTest.java \
	libs/braille-css/src/test/java/PseudoClassTest.java \
	libs/braille-css/src/test/java/PseudoElementsTest.java \
	libs/braille-css/src/test/java/VendorExtensionsTest.java
.make/mk/libs/braille-css/sources.mk : \
	libs/braille-css/src \
	libs/braille-css/src/test \
	libs/braille-css/src/test/java \
	libs/braille-css/src/main \
	libs/braille-css/src/main/java \
	libs/braille-css/src/main/java/org \
	libs/braille-css/src/main/java/org/daisy \
	libs/braille-css/src/main/java/org/daisy/braille \
	libs/braille-css/src/main/java/org/daisy/braille/css \
	libs/braille-css/src/main/antlr3 \
	libs/braille-css/src/main/antlr3/org \
	libs/braille-css/src/main/antlr3/org/daisy \
	libs/braille-css/src/main/antlr3/org/daisy/braille \
	libs/braille-css/src/main/antlr3/org/daisy/braille/css
