package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

import java.util.HashMap;
import java.util.Map;

class LatexHyphenator extends AbstractHyphenator {
    private final HyphenationConfig hyphenator;

    private final Map<String, String> hyphCache = new HashMap<>();

    LatexHyphenator(HyphenationConfig hyphenator) throws HyphenatorConfigurationException {
        this.hyphenator = hyphenator;
        this.beginLimit = this.hyphenator.getDefaultBeginLimit();
        this.endLimit = this.hyphenator.getDefaultEndLimit();
    }

    @Override
    public String hyphenate(String phrase) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : phrase.split(" ")) {
            if (!first) {
                sb.append(" ");
            }
            if (!hyphCache.containsKey(s)) {
                hyphCache.put(s, hyphenator.getHyphenator().hyphenate(s, getBeginLimit(), getEndLimit()));
            }
            sb.append(hyphCache.get(s));
            first = false;
        }
        if (phrase.endsWith(" ")) {
            sb.append(" ");
        }

        return sb.toString();
    }

}
