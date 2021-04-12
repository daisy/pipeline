package org.daisy.pipeline.tts.cereproc.impl.util;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import org.daisy.common.file.URLs;
public class CereprocTTSUtil {

    private Optional<Locale> locale;
    RegexReplace regexReplace;
    UCharReplacer charReplacer;

    public CereprocTTSUtil(Optional<Locale> locale) {
        this.locale = locale;
        initRegexRules();
        try {
            initCharSubstitutionRules();
        } catch (IOException e) {
            throw new RuntimeException("Char substitution files not found");
        }
    }

    private void initRegexRules() {
        URL url;
        String lang  = getCurrentLanguage();

        if (lang.equals("sv")) {
            url = URLs.getResourceFromJAR("/regex/cereproc_sv.xml", CereprocTTSUtil.class);
        } else if (lang.equals("en")) {
            url = URLs.getResourceFromJAR("/regex/cereproc_en.xml", CereprocTTSUtil.class);
        } else {
            return;
        }

        this.regexReplace = new RegexReplace(url);
    }

    private void initCharSubstitutionRules() throws IOException {
        this.charReplacer = new UCharReplacer();
        URL commonSubstRulesFileUrl = URLs.getResourceFromJAR("/charsubst/character-translation-table.xml", CereprocTTSUtil.class);

        this.charReplacer.addSubstitutionTable(commonSubstRulesFileUrl);

        String lang  = getCurrentLanguage();
        URL languageSubstRulesFileUrl;
        if (lang.equals("sv")) {
            languageSubstRulesFileUrl = URLs.getResourceFromJAR("/charsubst/character-translation-table_sv.xml", CereprocTTSUtil.class);
        } else if (lang.equals("en")) {
            languageSubstRulesFileUrl = URLs.getResourceFromJAR("/charsubst/character-translation-table_en.xml", CereprocTTSUtil.class);
        } else {
            return;
        }

        this.charReplacer.addSubstitutionTable(languageSubstRulesFileUrl);
    }

    String applyRegex(String text) {
        return this.regexReplace.filter(text);
    }

    String applyCharacterSubstitution(String text){
        return this.charReplacer.replace(text).toString();
    }

    public String applyAll(String text) {
        String tmp;
        tmp = this.applyRegex(text);
        return this.applyCharacterSubstitution(tmp);
    }

    private String getCurrentLanguage() {
        if (this.locale.isPresent()){
            return this.locale.get().getLanguage();
        } else {
            return "";
        }
    }
}
