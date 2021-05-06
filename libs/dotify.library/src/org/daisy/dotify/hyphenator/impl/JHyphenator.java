package org.daisy.dotify.hyphenator.impl;

import ch.sbs.jhyphen.Hyphenator;
import ch.sbs.jhyphen.StandardHyphenationException;
import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

class JHyphenator extends AbstractHyphenator {
    public static final byte SHY = 1;

    private Hyphenator instance;
    private final Map<String, String> hyphCache = new HashMap<>();

    JHyphenator(String locale) throws HyphenatorConfigurationException {
        try {
            ResourceBundle p = PropertyResourceBundle.getBundle("org/daisy/dotify/hyphenator/impl/JHyphenator");
            instance = new Hyphenator(new File("/usr/share/hyphen/", p.getString(locale)));
        } catch (Exception e) {
            throw new JHyphenatorConfigurationException(e);
        }
    }

    @Override
    public String hyphenate(String phrase) throws StandardHyphenationException {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s : phrase.split(" ")) {
            if (!first) {
                sb.append(" ");
            }

            if (s.isEmpty()) {
                sb.append(" ");
                continue;
            }

            if (!hyphCache.containsKey(s)) {
                hyphCache.put(s, handleWord(s, instance.hyphenate(s)));
            }
            sb.append(hyphCache.get(s));
            first = false;
        }

        if (phrase.endsWith(" ")) {
            sb.append(" ");
        }

        return sb.toString();
    }

    protected String handleWord(String s, byte[] arr) {
        int len = arr.length;

        if (len <= getBeginLimit() + getEndLimit()) {
            return s;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < getBeginLimit() - 1; i++) {
            arr[i] = 0;
        }

        for (int i = len - getEndLimit() + 1; i < len; i++) {
            arr[i] = 0;
        }
        for (int i = 0; i < s.length(); i++) {
            sb.append(s.charAt(i));
            if (arr.length > i && arr[i] == SHY) {
                sb.append("\u00AD");
            }
        }

        return sb.toString();
    }
}
