package org.daisy.dotify.translator;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;
import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMakerService;
import org.daisy.dotify.api.hyphenator.HyphenatorInterface;
import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.ResolvableText;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.common.text.StringFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides a configurable braille filter, in cases where a full implementation
 * is not needed. This implementation first translates the markers, then hyphenates
 * the text, and then sends the result to the supplied string filter.
 *
 * @author Joel Håkansson
 */
public class DefaultBrailleFilter implements BrailleFilter {
    private final String loc;
    private final StringFilter filter;
    private final DefaultMarkerProcessor tap;
    private final HyphenatorFactoryMakerService hyphenatorFactoryMaker;
    private final Map<String, HyphenatorInterface> hyphenators;

    /**
     * Creates a new default braille filter with the supplied parameters.
     *
     * @param filter                 the braille filter to use
     * @param locale                 the locale of the implementation
     * @param hyphenatorFactoryMaker the hyphenator factory maker
     */
    public DefaultBrailleFilter(
        StringFilter filter,
        String locale,
        HyphenatorFactoryMakerService hyphenatorFactoryMaker
    ) {
        this(filter, locale, null, hyphenatorFactoryMaker);
    }

    /**
     * Creates a new default braille filter with the supplied parameters.
     *
     * @param filter                 the braille filter to use
     * @param locale                 the locale of the implementation
     * @param tap                    the marker processor
     * @param hyphenatorFactoryMaker the hyphenator factory maker
     */
    public DefaultBrailleFilter(
        StringFilter filter,
        String locale,
        DefaultMarkerProcessor tap,
        HyphenatorFactoryMakerService hyphenatorFactoryMaker
    ) {
        this.loc = locale;
        this.filter = filter;
        this.tap = tap;
        this.hyphenators = new HashMap<>();
        this.hyphenatorFactoryMaker = hyphenatorFactoryMaker;
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
                    throw new DefaultBrailleFilterException(e);
                }
                hyphenators.put(locale, h);
            }
            text = h.hyphenate(text);
        }

        if (tap != null) {
            text = tap.processAttributes(specification.getAttributes(), text);
        }

        return filter.filter(text);
    }

    @Override
    public String filter(TranslatableWithContext specification) throws TranslationException {
        if (specification.getTextToTranslate().isEmpty()) {
            return "";
        }
        Stream<String> inStream = specification.getTextToTranslate().stream().map(v -> v.resolve());
        List<String> texts;

        if (tap != null && specification.getAttributes().isPresent()) {
            Stream<String> preceding = specification.getPrecedingText().stream().map(v -> v.resolve());
            Stream<String> following = specification.getFollowingText().stream().map(v -> v.peek());
            List<String> textsI = Stream.concat(
                Stream.concat(preceding, inStream),
                following
            ).collect(Collectors.toList());
            String[] out = tap.processAttributesRetain(specification.getAttributes().get(), textsI);
            int start = specification.getPrecedingText().size();
            int end = start + specification.getTextToTranslate().size();
            texts = Arrays.asList(out).subList(start, end);
        } else {
            texts = inStream.collect(Collectors.toList());
        }

        // We've checked that there is at least one text to translate
        Optional<String> l = specification.getTextToTranslate().get(0).getLocale();
        boolean h = specification.getTextToTranslate().get(0).shouldHyphenate();
        boolean m = specification.getTextToTranslate().get(0).shouldMarkCapitalLetters();

        int i = 0;
        StringBuilder ret = new StringBuilder();
        StringBuilder toTranslate = new StringBuilder();
        for (ResolvableText t : specification.getTextToTranslate()) {
            if (!t.getLocale().equals(l) || t.shouldHyphenate() != h || t.shouldMarkCapitalLetters() != m) {
                //Flush
                Translatable.Builder b = Translatable.text(toTranslate.toString())
                        .hyphenate(h)
                        .markCapitalLetters(m);
                if (l.isPresent()) {
                    b.locale(l.get());
                }
                ret.append(filter(b.build()));
                // Set
                l = t.getLocale();
                h = t.shouldHyphenate();
                m = t.shouldMarkCapitalLetters();
                toTranslate = new StringBuilder();
            }
            toTranslate.append(texts.get(i));
            i++;
        }
        if (toTranslate.length() > 0) {
            //Flush
            Translatable.Builder b = Translatable.text(toTranslate.toString())
                    .hyphenate(h)
                    .markCapitalLetters(m);
            if (l.isPresent()) {
                b.locale(l.get());
            }
            ret.append(filter(b.build()));
        }
        return ret.toString();
    }

    private class DefaultBrailleFilterException extends TranslationException {

        /**
         *
         */
        private static final long serialVersionUID = 6102686243949860112L;

        DefaultBrailleFilterException(Throwable cause) {
            super(cause);
        }

    }
}
