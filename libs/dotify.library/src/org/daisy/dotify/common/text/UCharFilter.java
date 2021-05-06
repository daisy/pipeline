package org.daisy.dotify.common.text;

import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Implements StringFilter using UCharReplacer.
 *
 * @author Joel Hakansson
 * @version 4 maj 2009
 * @since 1.0
 */
public class UCharFilter extends SimpleUCharReplacer implements StringFilter {
    private static final Logger logger = Logger.getLogger(UCharFilter.class.getCanonicalName());
    /**
     *
     */
    private static final long serialVersionUID = -6510523268450839023L;

    /**
     * Create a new CharFilter.
     *
     * @param table        relative path to replacement table, see UCharReplacement for more information
     * @param autoComplete adds upper/lower case entries with the same value, where missing according to the
     *                     specified locale
     */
    public UCharFilter(URL table, Locale autoComplete) {
        super();
        try {
            addSubstitutionTable(table);
        } catch (Exception e) {
            logger.throwing(UCharFilter.class.getCanonicalName(), "UCharFilter",  e);
        }
        if (autoComplete != null) {
            putAll(fillInCase(this, autoComplete));
        }
    }

    /**
     * Create a new CharFilter.
     *
     * @param table        relative path to replacement table, see UCharReplacement for more information
     * @param autoComplete adds upper/lower case entries with the same value, where missing according to the
     *                     specified locale
     */
    public UCharFilter(Locale autoComplete, URL... table) {
        super();
        for (URL t : table) {
            try {
                addSubstitutionTable(t);
            } catch (Exception e) {
                logger.throwing(UCharFilter.class.getCanonicalName(), "UCharFilter",  e);
            }
        }
        if (autoComplete != null) {
            putAll(fillInCase(this, autoComplete));
        }
    }

    private static Map<Integer, String> fillInCase(Map<Integer, String> map, Locale autoComplete) {
        Map<Integer, String> add = new HashMap<>();
        String substitute;
        String codePointStr;
        String newStr;
        for (Integer codePoint : map.keySet()) {
            substitute = map.get(codePoint);
            if (substitute != null) {
                codePointStr = new String(Character.toChars(codePoint));
                if (codePointStr.equals((newStr = codePointStr.toUpperCase(autoComplete)).toLowerCase(autoComplete))) {
                    if (newStr.codePointCount(0, newStr.length()) == 1) {
                        int uppercase = newStr.codePointAt(0);
                        if (!map.containsKey(uppercase)) {
                            add.put(uppercase, substitute);
                        }
                    }
                } else if (
                        codePointStr.equals(
                            (newStr = codePointStr.toLowerCase(autoComplete)
                        ).toUpperCase(autoComplete)) &&
                                newStr.codePointCount(0, newStr.length()) == 1
                ) {
                    int lowercase = newStr.codePointAt(0);
                    if (!map.containsKey(lowercase)) {
                        add.put(lowercase, substitute);
                    }
                }
            }
        }
        return add;
    }

    @Override
    public String filter(String str) {
        return replace(str).toString();
    }

}
