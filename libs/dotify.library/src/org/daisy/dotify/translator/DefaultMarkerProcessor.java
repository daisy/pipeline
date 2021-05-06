package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.AttributeWithContext;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides a default marker processor implementation.
 *
 * @author Joel Håkansson
 */
public class DefaultMarkerProcessor {
    private static final Logger logger = Logger.getLogger(DefaultMarkerProcessor.class.getCanonicalName());
    private final Map<String, MarkerDictionary> specs;

    /**
     * Provides a builder for the marker processor.
     */
    public static class Builder {
        private final Map<String, MarkerDictionary> specs;

        /**
         * Creates a new builder.
         */
        public Builder() {
            specs = new HashMap<>();
        }

        /**
         * Adds a marker dictionary.
         *
         * @param identifier the identifier
         * @param def        the dictionary
         * @return returns this builder
         */
        public Builder addDictionary(String identifier, MarkerDictionary def) {
            specs.put(identifier, def);
            return this;
        }

        /**
         * Creates a new default marker processor using the current
         * state of the builder.
         *
         * @return returns a new marker processor
         */
        public DefaultMarkerProcessor build() {
            return new DefaultMarkerProcessor(this);
        }
    }

    private DefaultMarkerProcessor(Builder builder) {
        this.specs = builder.specs;
    }

    /**
     * Processes the input text and attributes into a text containing
     * markers at the appropriate positions. The length of the text(s)
     * must match the text attributes specified width.
     *
     * @param atts the text attributes that apply to the text.
     * @param text the text(s) to process
     * @return returns a string with markers
     * @throws IllegalArgumentException if the specified attributes does not match the text.
     */
    public String processAttributes(TextAttribute atts, String... text) {
        return join(processAttributesRetain(atts, text));
    }

    /**
     * Processes the input text chunks and attributes into a text containing
     * markers at the appropriate positions while retaining the text
     * partition as specified by the input array. The length of the texts
     * must match the text attributes specified width.
     *
     * @param text the texts to process
     * @param atts the text attributes that apply to the text.
     * @return returns an array of strings with markers
     * @throws IllegalArgumentException if the specified attributes does not match the text.
     */
    public String[] processAttributesRetain(TextAttribute atts, String[] text) {
        if (atts == null) {
            return text;
        } else {
            StringBuilder combined = new StringBuilder();
            for (String s : text) {
                combined.append(s);
            }
            int textLen = combined.length();
            if (atts.getWidth() != textLen) {
                throw new IllegalArgumentException(
                    "Text attribute width (" + atts.getWidth() + ") does not match text length (" + textLen + ")."
                );
            }
            String[] ret = new String[text.length > 0 ? text.length : 1];
            Arrays.fill(ret, "");
            Marker m = getMarker(combined.toString(), atts);

            if (m != null) {
                ret[0] = m.getPrefix() + (ret.length > 0 ? ret[0] : "");
            }
            int startInx = 0;
            if (atts.hasChildren()) {
                for (TextAttribute d : atts) {
                    SubstringReturn sr = substrings(text, startInx, startInx + d.getWidth());
                    String[] res = processAttributesRetain(d, sr.getStrings());
                    for (int i = 0; i < res.length; i++) {
                        ret[i + sr.getArrayStart()] += res[i];
                    }
                    startInx += d.getWidth();
                }
            } else {
                SubstringReturn sr = substrings(text, 0, atts.getWidth());
                String[] res = sr.getStrings();
                for (int i = 0; i < res.length; i++) {
                    ret[i + sr.getArrayStart()] += res[i];
                }
            }
            if (m != null) {
                ret[text.length > 0 ? text.length - 1 : 0] += m.getPostfix();
            }
            return ret;
        }
    }

    /**
     * Processes the input text chunks and attributes into a text containing
     * markers at the appropriate positions while retaining the text
     * partition as specified by the input array. The length of the texts
     * must match the text attributes specified width.
     *
     * @param text the texts to process
     * @param atts the text attributes that apply to the text.
     * @return returns an array of strings with markers
     * @throws IllegalArgumentException if the specified attributes does not
     *                                  match the text.
     */
    public String[] processAttributesRetain(AttributeWithContext atts, List<String> text) {
        if (atts == null) {
            return text.toArray(new String[text.size()]);
        } else {
            if (atts.getWidth() != text.size()) {
                throw new IllegalArgumentException(
                    "Attribute context width (" + atts.getWidth() + ") " +
                    "does not match text list size (" + text.size() + ")."
                );
            }
            Marker m = getMarker(text.stream().collect(Collectors.joining()), toTextAttribute(atts, text));
            String[] ret = new String[text.size() > 0 ? text.size() : 1];
            Arrays.fill(ret, "");

            if (m != null) {
                ret[0] = m.getPrefix() + (ret.length > 0 ? ret[0] : "");
            }
            int startInx = 0;
            if (atts.hasChildren()) {
                for (AttributeWithContext d : atts) {
                    List<String> sr = text.subList(startInx, startInx + d.getWidth());
                    String[] res = processAttributesRetain(d, sr);
                    for (int i = 0; i < res.length; i++) {
                        ret[i + startInx] += res[i];
                    }
                    startInx += d.getWidth();
                }
            } else {
                List<String> sr = text.subList(0, atts.getWidth());
                String[] res = sr.toArray(new String[sr.size()]);
                for (int i = 0; i < res.length; i++) {
                    ret[i] += res[i];
                }
            }
            if (m != null) {
                ret[text.size() > 0 ? text.size() - 1 : 0] += m.getPostfix();
            }
            return ret;
        }
    }

    public static TextAttribute toTextAttribute(AttributeWithContext c, List<String> texts) {
        return toTextAttribute(c, texts, 0);
    }

    static TextAttribute toTextAttribute(AttributeWithContext c, List<String> texts, int offset) {
        DefaultTextAttribute.Builder ret = new DefaultTextAttribute.Builder(c.getName().orElse(null));
        if (offset >= texts.size()) {
            throw new IllegalArgumentException();
        }
        if (c.hasChildren()) {
            int offs = offset;
            int w = 0;
            for (AttributeWithContext cc : c) {
                TextAttribute ta = toTextAttribute(cc, texts, offs);
                offs += cc.getWidth();
                w += ta.getWidth();
                ret.add(ta);
            }
            return ret.build(w);
        } else {
            int start = offset;
            int end = start + c.getWidth();
            return ret.build(texts.subList(start, end).stream()
                    .mapToInt(v -> v.length())
                    .sum());
        }
    }

    /**
     * Gets the substrings within the string arrays, using unified start and
     * end indexes.
     *
     * @param strs
     * @param startInx the start index, from the beginning of the first string
     * @param endInx   the end index,
     * @return returns a substring
     * @throws IndexOutOfBoundsException if end index is less than or equal to start index
     */
    private static SubstringReturn substrings(String[] strs, int startInx, int endInx) {
        if (strs.length < 1) {
            return new SubstringReturn(new String[]{}, 0);
        } else if (endInx < startInx) {
            throw new IndexOutOfBoundsException("End index must be greater than start index.");
        } else if (endInx == startInx) {
            return new SubstringReturn(new String[]{}, 0);
        } else if (strs.length == 1) {
            return new SubstringReturn(new String[]{strs[0].substring(startInx, endInx)}, 0);
        } else {
            int len = 0;
            for (String s : strs) {
                len += s.length();
            }
            if (endInx > len) {
                throw new IndexOutOfBoundsException(
                    "End index " + +endInx + " is beyond the length of the input (" + len + ")"
                );
            }
            int startOffset = startInx;
            int aStart = 0;
            while (strs[aStart].length() <= startOffset) {
                startOffset -= strs[aStart].length();
                aStart++;
            }
            int endOffset = endInx;
            int aEnd = 0;
            while (strs[aEnd].length() < endOffset) {
                endOffset -= strs[aEnd].length();
                aEnd++;
            }
            List<String> ret = new ArrayList<>();
            for (int i = aStart; i <= aEnd; i++) {
                if (i == aStart && i == aEnd) {
                    ret.add(strs[aStart].substring(startOffset, endOffset));
                } else if (i > aStart && i < aEnd) {
                    ret.add(strs[i]);
                } else if (i == aStart) {
                    ret.add(strs[i].substring(startOffset));
                } else if (i == aEnd) {
                    ret.add(strs[i].substring(0, endOffset));
                } else {
                    ret.add("");
                }
            }
            return new SubstringReturn(ret.toArray(new String[]{}), aStart);
        }
    }

    private String join(String[] strs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            sb.append(strs[i]);
        }
        return sb.toString();
    }

    private Marker getMarker(String text, TextAttribute atts) {
        String specKey = atts.getDictionaryIdentifier();
        if (specKey != null) {
            MarkerDictionary def = specs.get(specKey);
            if (def != null) {
                try {
                    return def.getMarkersFor(text, atts);
                } catch (MarkerNotFoundException e) {
                    logger.log(Level.WARNING, specKey + " markers cannot be applied to the text: " + text);
                } catch (MarkerNotCompatibleException e) {
                    logger.log(Level.WARNING, specKey + " markers cannot be applied to this structure.");
                }
            } else {
                logger.warning("Undefined attribute: " + specKey);
            }
        }
        return null;
    }

    private static class SubstringReturn {
        private final String[] strings;
        private final int arrayStart;

        public SubstringReturn(String[] strings, int arrayStart) {
            super();
            this.strings = strings;
            this.arrayStart = arrayStart;
        }

        public String[] getStrings() {
            return strings;
        }

        public int getArrayStart() {
            return arrayStart;
        }

    }

}
