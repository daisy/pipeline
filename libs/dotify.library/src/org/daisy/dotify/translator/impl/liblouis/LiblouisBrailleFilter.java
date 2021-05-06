package org.daisy.dotify.translator.impl.liblouis;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.ResolvableText;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.translator.DefaultMarkerProcessor;
import org.liblouis.CompilationException;
import org.liblouis.DisplayException;
import org.liblouis.DisplayTable.Fallback;
import org.liblouis.DisplayTable.UnicodeBrailleDisplayTable;
import org.liblouis.TranslationResult;
import org.liblouis.Translator;
import org.liblouis.Typeform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LiblouisBrailleFilter implements BrailleFilter {
    private static final Logger LOGGER = Logger.getLogger(LiblouisBrailleFilter.class.getCanonicalName());
    private static final int SOFT_HYPHEN = 0x00ad;
    private static final int ZERO_WIDTH_SPACE = 0x200b;
    private static final int LIBLOUIS_NO_BREAKPOINT = 0;
    private static final int LIBLOUIS_SOFT_HYPEN = 1;
    private static final int LIBLOUIS_ZERO_WIDTH_SPACE = 2;
    private final String loc;
    private final HyphenatorFactoryMakerService hyphenatorFactoryMaker;
    private final Map<String, HyphenatorInterface> hyphenators;
    private final Translator table;
    private final Map<String, Typeform> typeformMap;
    private final LiblouisMarkerProcessor mp;

    LiblouisBrailleFilter(
        TranslatorSpecification ts,
        LiblouisMarkerProcessor mp,
        HyphenatorFactoryMakerService hyphenatorFactoryMaker
    ) {
        this.loc = ts.getLocale();
        this.hyphenatorFactoryMaker = hyphenatorFactoryMaker;
        this.hyphenators = new HashMap<>();
        try {
            this.table = new Translator(LiblouisSpecifications.getMap().get(ts));
        } catch (CompilationException e) {
            throw new IllegalArgumentException(e);
        }
        this.typeformMap = table.getSupportedTypeforms().stream()
                .collect(Collectors.toMap(x -> x.getName(), x -> x));
        addTypeformAlias("italic", "em");
        addTypeformAlias("bold", "strong");
        this.mp = mp;
    }

    private void addTypeformAlias(String name, String alias) {
        if (typeformMap.containsKey(name) && !typeformMap.containsKey(alias)) {
            Typeform t = typeformMap.get(name);
            typeformMap.put(alias, t);
        }
    }

    @Override
    public String filter(Translatable specification) throws TranslationException {
        if (specification.getText().isEmpty()) {
            return "";
        }
        String locale = specification.getLocale();
        if (locale == null) {
            locale = loc;
        }

        // Attributes should be processed here. To do that, the text must be split up into
        // the attribute parts, processed and then reassembled with updated attributes.
        // The updated attributes should then be used when creating the type form below.
        // See also https://github.com/brailleapps/dotify.translator.impl/issues/4
        // Something like this:
        //      String[] t = splitByAttribute(specification.getText(), specification.getAttributes());
        //      AttributeWithContext atts = toAttributeWithContext(specification.getAttributes(), t);
        //      List<String> x = Arrays.asList(mp.getMarkerProcessor()
        //          .processAttributesRetain(specification.getAttributes(), t));
        //      TextAttribute ta = DefaultMarkerProcessor.toTextAttribute(atts, x);
        //      String text = x.stream().collect(Collectors.toList());

        String text = specification.getText();

        if (!specification.shouldMarkCapitalLetters()) {
            //TODO: toLowerCase may not always do what we want here,
            //it depends on the lower case algorithm and the rules
            //of the braille for that language
            text = text.toLowerCase(Locale.ROOT);
        }

        if (specification.isHyphenating()) {
            HyphenatorInterface h = hyphenators.get(locale);
            if (h == null) {
                try {
                    h = hyphenatorFactoryMaker.newHyphenator(locale);
                } catch (HyphenatorConfigurationException e) {
                    throw new LiblouisBrailleFilterException(e);
                }
                hyphenators.put(locale, h);
            }
            text = h.hyphenate(text);
        }

        // Only style attributes from Liblouis itself are processed here
        LiblouisTranslatable louisSpec = toLiblouisSpecification(text, specification.getText());
        TextAttribute ta = specification.getAttributes();
        Typeform[] typeForm;
        if (ta == null) {
            typeForm = new Typeform[louisSpec.getCharAtts().length];
        } else {
            typeForm = toTypeForm(ta, typeformMap);
        }

        try {
            return toBrailleFilterString(
                louisSpec.getText(),
                table.translate(
                    louisSpec.getText(),
                    typeForm,
                    louisSpec.getCharAtts(),
                    louisSpec.getInterCharAtts(),
                    new UnicodeBrailleDisplayTable(Fallback.MASK)
                )
            );
        } catch (org.liblouis.TranslationException | DisplayException e) {
            throw new LiblouisBrailleFilterException(e);
        }
    }

    @Override
    public String filter(TranslatableWithContext specification) throws TranslationException {
        if (specification.getTextToTranslate().isEmpty()) {
            return "";
        }

        Stream<String> inStream = specification.getTextToTranslate().stream().map(v -> v.resolve());
        List<String> texts;

        if (mp != null && specification.getAttributes().isPresent()) {
            Stream<String> preceding = specification.getPrecedingText().stream().map(v -> v.resolve());
            Stream<String> following = specification.getFollowingText().stream().map(v -> v.peek());
            List<String> textsI = Stream.concat(
                Stream.concat(preceding, inStream), following
            ).collect(Collectors.toList());
            String[] out = mp.getMarkerProcessor().processAttributesRetain(specification.getAttributes().get(), textsI);
            int start = specification.getPrecedingText().size();
            int end = start + specification.getTextToTranslate().size();
            texts = Arrays.asList(out).subList(start, end);
        } else {
            texts = inStream.collect(Collectors.toList());
        }

        Processor p = new Processor(specification.getTextToTranslate().get(0));
        int i = 0;
        for (ResolvableText t : specification.getTextToTranslate()) {
            p.process(texts.get(i), t);
            i++;
        }
        p.flush();

        String strIn = p.textB.toString();
        String strHyph = p.hyphB.toString();

        LiblouisTranslatable louisSpec = toLiblouisSpecification(strHyph, strIn);

        Typeform[] typeForm;

        if (specification.getAttributes().isPresent()) {
            List<String> preceding = specification.getPrecedingText().stream().map(
                v -> v.resolve()).collect(Collectors.toList()
            );
            List<String> following = specification.getFollowingText().stream().map(
                v -> v.peek()).collect(Collectors.toList()
            );
            List<String> textsI = Stream.concat(
                Stream.concat(preceding.stream(), p.parts.stream()), following.stream()
            ).collect(Collectors.toList());
            TextAttribute ta = DefaultMarkerProcessor.toTextAttribute(specification.getAttributes().get(), textsI);
            Typeform[] typeForm2 = toTypeForm(ta, typeformMap);
            int start = preceding.stream().mapToInt(v -> v.length()).sum();
            int end = start + strIn.length();
            typeForm = Arrays.copyOfRange(typeForm2, start, end);
        } else {
            typeForm = new Typeform[louisSpec.getCharAtts().length];
        }

        try {
            return toBrailleFilterString(
                louisSpec.getText(),
                table.translate(
                    louisSpec.getText(),
                    typeForm,
                    louisSpec.getCharAtts(),
                    louisSpec.getInterCharAtts(),
                    new UnicodeBrailleDisplayTable(Fallback.MASK)
                )
            );
        } catch (org.liblouis.TranslationException | DisplayException e) {
            throw new LiblouisBrailleFilterException(e);
        }
    }

    private class Processor {

        private Processor(ResolvableText props) {
            processorLocale = props.getLocale();
            hyphenate = props.shouldHyphenate();
            markCapitals = props.shouldMarkCapitalLetters();
        }

        Optional<String> processorLocale;
        boolean hyphenate;
        boolean markCapitals;

        List<String> parts = new ArrayList<>();
        StringBuilder textB = new StringBuilder();
        StringBuilder hyphB = new StringBuilder();
        StringBuilder toTranslate = new StringBuilder();

        private void process(String s, ResolvableText props) {
            if (
                !props.getLocale().equals(processorLocale) ||
                props.shouldHyphenate() != hyphenate ||
                props.shouldMarkCapitalLetters() != markCapitals
            ) {
                //Flush
                flush();
                // Set
                processorLocale = props.getLocale();
                hyphenate = props.shouldHyphenate();
                markCapitals = props.shouldMarkCapitalLetters();
                toTranslate = new StringBuilder();
            }
            parts.add(s);
            toTranslate.append(s);
        }

        private void flush() {
            if (toTranslate.length() == 0) {
                return;
            }
            String text = toTranslate.toString();
            String hyphText = text;
            if (!markCapitals) {
                //TODO: toLowerCase may not always do what we want here,
                //it depends on the lower case algorithm and the rules
                //of the braille for that language
                text = text.toLowerCase(Locale.ROOT);
            }
            if (hyphenate) {
                String locale = processorLocale.orElse(loc);
                HyphenatorInterface hx = hyphenators.get(locale);
                if (hx == null) {
                    try {
                        hx = hyphenatorFactoryMaker.newHyphenator(locale);
                    } catch (HyphenatorConfigurationException e) {
                        if (LOGGER.isLoggable(Level.WARNING)) {
                            LOGGER.log(Level.WARNING, String.format("Failed to create hyphenator for %s", locale), e);
                        }
                    }
                    hyphenators.put(locale, hx);
                }
                hyphText = hx.hyphenate(text);
            }

            textB.append(text);
            hyphB.append(hyphText);
        }

    }

    /**
     * Maps a translatable and the corresponding hyphenated string to a set of data that can be
     * used with Liblouis. The hyphenated string is used to set the intercharacter attributes.
     *
     * @param hyphStr  the hyphenated string
     * @param inputStr the input string
     * @return hyphenation information
     */
    static LiblouisTranslatable toLiblouisSpecification(String hyphStr, String inputStr) {
        if (hyphStr.length() < inputStr.length()) {
            throw new IllegalArgumentException("The hyphenated string cannot be shorter than the input string");
        }

        int[] cpHyph = hyphStr.codePoints().toArray();
        int[] cpInput = inputStr.codePoints().toArray();
        int j = 0;
        int flag;
        int[] interCharAttr = new int[cpInput.length - 1];
        int[] charAtts = new int[cpInput.length];

        for (int i = 0; i < cpInput.length; i++) {
            charAtts[i] = i;
            flag = LIBLOUIS_NO_BREAKPOINT;
            while (j < cpHyph.length && i < cpInput.length - 1 && cpInput[i + 1] != cpHyph[j]) {
                if (cpHyph[j] == SOFT_HYPHEN) {
                    flag = LIBLOUIS_SOFT_HYPEN;
                } else if (cpHyph[j] == ZERO_WIDTH_SPACE && flag != LIBLOUIS_SOFT_HYPEN) {
                    flag = LIBLOUIS_ZERO_WIDTH_SPACE;
                } else if (cpInput[i] != cpHyph[j] && cpInput[i + 1] != cpHyph[j + 1]) {
                    throw new RuntimeException("'" + hyphStr + ":" + inputStr + "'");
                }
                j++;
            }
            j++;
            if (i < cpInput.length - 1) {
                interCharAttr[i] = flag;
            }
        }
        return new LiblouisTranslatable(inputStr, charAtts, interCharAttr);
    }

    /**
     * Converts a text attribute to its "type form" equivalent.
     *
     * @param attr the text attribute
     * @param map  the text attribute name to type form value map
     * @return returns an array with the corresponding values
     */
    static Typeform[] toTypeForm(TextAttribute attr, Map<String, Typeform> map) {
        Typeform[] ret = new Typeform[attr.getWidth()];
        Typeform typeForm = Typeform.PLAIN_TEXT;
        if (attr.getDictionaryIdentifier() != null) {
            typeForm = Optional.ofNullable(map.get(attr.getDictionaryIdentifier())).orElse(typeForm);
        }

        if (attr.hasChildren()) {
            int offset = 0;
            for (TextAttribute t : attr) {
                Typeform[] v = toTypeForm(t, map);
                //Note: v.length == t.getWidth()
                for (int i = 0; i < v.length; i++) {
                    ret[i + offset] = typeForm.add(v[i]);
                }
                offset += t.getWidth();
            }
        } else {
            for (int i = 0; i < ret.length; i++) {
                ret[i] = typeForm;
            }
        }
        return ret;
    }

    private static String toBrailleFilterString(String input, TranslationResult res) {
        return toBrailleFilterString(
            input, res.getBraille(), res.getCharacterAttributes(), res.getInterCharacterAttributes()
        );
    }

    /**
     * Modifies a string from Liblouis into a string that is compatible with {@link BrailleFilter}
     * by adding hyphenation characters (soft hyphen and zero width space).
     *
     * @param str           the Liblouis string
     * @param interCharAttr the inter char attributes.
     * @return a string
     */
    static String toBrailleFilterString(String input, String str, int[] charAtts, int[] interCharAttr) {
        StringBuilder sb = new StringBuilder();
        int[] inputCodePoints = input.codePoints().toArray();
        int[] codePoints = str.codePoints().toArray();
        int prvInputIndex = -1;
        int inputIndex, inputCP;
        for (int outputIndex = 0; outputIndex < codePoints.length; outputIndex++) {
            inputIndex = charAtts[outputIndex];
            inputCP = inputCodePoints[inputIndex];
            // The following is needed because some tables in Liblouis translate spaces into braille cells, e.g. Danish.
            // The BrailleFilter contract requires spaces to be preserved.
            if (Character.isWhitespace(inputCP)) {
                // If the input index for the output index is the same as the previous
                // input index, then this output character belongs to the same input character.
                // If so, the character has already been processed, and should not be added to the
                // output again.
                if (prvInputIndex != inputIndex) {
                    sb.appendCodePoint(' ');
                }
                prvInputIndex = inputIndex;
            } else {
                prvInputIndex = -1;
                sb.appendCodePoint(codePoints[outputIndex]);
            }
            if (outputIndex < interCharAttr.length) {
                switch (interCharAttr[outputIndex]) {
                    case LIBLOUIS_NO_BREAKPOINT:
                        break;
                    case LIBLOUIS_SOFT_HYPEN:
                        sb.append('\u00ad');
                        break;
                    case LIBLOUIS_ZERO_WIDTH_SPACE:
                        sb.append('\u200b');
                        break;
                    default:
                }
            }
        }
        return sb.toString();
    }

}
