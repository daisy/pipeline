package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.common.text.TextFileReader;
import org.daisy.dotify.common.text.TextFileReader.LineData;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


/**
 * Class to decompound words.
 *
 * @author Joel Håkansson
 */
class CWDecompounder {
    static final String SOFT_HYPHEN = "\u00ad";
    private final Map<String, CWHyphenationUnit> stems;
    private final int decompoundLimit;

    /**
     * Creates a new decompounder.
     *
     * @param decompoundLimit the partition limit, 1 or more
     */
    CWDecompounder(int decompoundLimit) {
        this.decompoundLimit = decompoundLimit;
        if (decompoundLimit < 1) {
            throw new IllegalArgumentException("Decompound limit must not be lower than one.");
        }
        stems = new HashMap<>();
    }

    /**
     * Load dictionary.
     *
     * @param url        the dictionary url
     * @param lowerLimit disregard words shorter than lowerLimit
     * @throws IOException if the url cannot be read
     */
    void loadDictionary(String url, int lowerLimit) throws IOException {
        if (lowerLimit < 1) {
            throw new IllegalArgumentException("Decompound limit must not be lower than one.");
        }
        InputStream is = this.getClass().getResourceAsStream(url);
        TextFileReader tfr = new TextFileReader(is);
        LineData data;
        while ((data = tfr.nextLine()) != null) {
            int len = data.getFields()[0].length();
            if (len < lowerLimit || data.getFields().length < 2) {
                continue;
            }
            if (data.getFields().length > 2) {
                stems.put(
                    data.getFields()[0],
                    new CWHyphenationUnit(Integer.parseInt(data.getFields()[1]) + 1, data.getFields()[2])
                );
            } else {
                stems.put(data.getFields()[0], new CWHyphenationUnit(Integer.parseInt(data.getFields()[1]) + 1));
            }
        }
        tfr.close();
        is.close();
    }

    /**
     * Loads a dictionary.
     *
     * @param url the url to the dictionary.
     * @throws IOException if the url cannot be read
     */
    void loadDictionary(String url) throws IOException {
        loadDictionary(url, 1);
    }

    /**
     * Gets the dictionary.
     *
     * @return returns the dictionary
     */
    Map<String, CWHyphenationUnit> getDictionary() {
        return stems;
    }

    /**
     * <p>Finds word compounds. The input word is scanned for possible compounds starting
     * at <tt>beginLimit</tt> and ending at <tt>endLimit</tt>.</p>
     *
     * <p>If evaluate is true, the most likely solution is returned. If evaluate is false,
     * the input word is returned unless there is only one possible solution in the
     * dictionary.</p>
     *
     * <p>For example, if the input is 'taxikväll' (beginLimit=2, endLimit=2) the following
     * string pairs are looked up in the dictionary: ta/xikväll, tax/ikväll, taxi/kväll,
     * taxik/väll, taxikvä/ll. In this case, two solutions are possible (assuming a typical
     * Swedish dictionary): tax/ikväll, taxi/kväll. In this case, if evaluate is true,
     * 'taxi-kväll' is returned, if evaluate is false, 'taxikväll' is returned.</p>
     *
     * @param word       the input word
     * @param beginLimit the shortest substring at the beginning of the word to evaluate against the dictionary
     * @param endLimit   the shortest substring at the end of the word to evaluate against the dictionary
     * @param threshold  an ambiguity threshold in the range [0, 1]. If there are several similar solutions,
     *                   the input word is returned when the weighted difference between two candidates is less
     *                   than the specified value
     * @return returns the word, hyphenated at compound boundaries
     */
    String findCompounds(String word, int beginLimit, int endLimit, double threshold) {
        if (word.length() < decompoundLimit) {
            return word;
        }
        int breakPoint = -1;
        CWHyphenationUnit bp1 = null;
        CWHyphenationUnit bp2 = null;
        int points = -1; // a word in the dictionary is better than if it's not, even if it's never been observed
        CWHyphenationUnit ret = lookup(word);
        if (ret != null) {
            if (ret.hasHyphenation()) {
                // this word has a predefined hyphenation, return it
                return ret.hyphenate(word);
            }
            points = ret.getFreq();
        }
        for (int i = beginLimit; i <= word.length() - endLimit; i++) {
            CWHyphenationUnit val1 = lookup(word.substring(0, i));
            if (val1 != null) {
                CWHyphenationUnit val2 = lookup(word.substring(i, word.length()));
                if (val2 != null) {
                    // Let the least likely part define the likelihood of the combination
                    int cval = (int) Math.sqrt(val1.getFreq() * val2.getFreq());
                    if (Math.abs(cval - points) / (double) (cval + points) < threshold) {
                        // if there are several possible close solutions, fail and return as is
                        return word;
                    }
                    //use this breakpoint if it is more frequent than the previous breakpoint (or the whole word)
                    if (points < cval) {
                        points = cval;
                        breakPoint = i;
                        bp1 = val1;
                        bp2 = val2;
                    }
                }
            }
        }
        if (breakPoint > -1) {
            StringBuilder r = new StringBuilder()
                    .append(bp1.hyphenate(word.substring(0, breakPoint)))
                    .append(SOFT_HYPHEN)
                    .append(bp2.hyphenate(word.substring(breakPoint, word.length())));
            return r.toString();
        } else {
            return word;
        }
    }

    /**
     * Finds a matching hyphenation unit.
     *
     * @param word the word to find
     * @return returns the hyphenation unit, or null if none is found
     */
    private CWHyphenationUnit lookup(String word) {
        CWHyphenationUnit ret = stems.get(word);
        if (ret == null) {
            // Try lower case
            ret = stems.get(word.toLowerCase());
        }
        return ret;
    }

}
