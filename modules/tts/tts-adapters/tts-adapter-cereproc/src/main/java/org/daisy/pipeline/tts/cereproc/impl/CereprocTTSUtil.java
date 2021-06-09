package org.daisy.pipeline.tts.cereproc.impl;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

import org.daisy.common.file.URLs;

class CereprocTTSUtil {

    private final Optional<Locale> locale;
    private final RegexReplace regexReplacer;
    private final UCharReplacer charReplacer;

    public CereprocTTSUtil(Optional<Locale> locale) {
        this.locale = locale;
        this.regexReplacer = initRegexRules();
        try {
            this.charReplacer = initCharSubstitutionRules();
        } catch (IOException e) {
            throw new RuntimeException("Char substitution files not found");
        }
    }

    private RegexReplace initRegexRules() {
        String lang = getCurrentLanguage();
        if ("sv".equals(lang)) {
            return new RegexReplace(URLs.getResourceFromJAR("/regex/cereproc_sv.xml", CereprocTTSUtil.class));
        } else if ("en".equals(lang)) {
            return new RegexReplace(URLs.getResourceFromJAR("/regex/cereproc_en.xml", CereprocTTSUtil.class));
        } else {
            throw new IllegalStateException();
        }
    }

    private UCharReplacer initCharSubstitutionRules() throws IOException {
        UCharReplacer charReplacer = new UCharReplacer();
        charReplacer.addSubstitutionTable(
            URLs.getResourceFromJAR("/charsubst/character-translation-table.xml",
                                    CereprocTTSUtil.class));
        String lang = getCurrentLanguage();
        if ("sv".equals(lang)) {
            charReplacer.addSubstitutionTable(
                URLs.getResourceFromJAR("/charsubst/character-translation-table_sv.xml",
                                        CereprocTTSUtil.class));
        } else if ("en".equals(lang)) {
            charReplacer.addSubstitutionTable(
                URLs.getResourceFromJAR("/charsubst/character-translation-table_en.xml",
                                        CereprocTTSUtil.class));
        }
        return charReplacer;
    }

    String applyRegex(String text) {
        return regexReplacer.filter(text);
    }

    String applyCharacterSubstitution(String text) {
        return charReplacer.replace(text).toString();
    }

    public String applyAll(String text) {
        return applyCharacterSubstitution(applyRegex(text));
    }

    private String getCurrentLanguage() {
        if (locale.isPresent()) {
            return locale.get().getLanguage();
        } else {
            return "";
        }
    }
}
