import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryService;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.FollowingText;
import org.daisy.dotify.api.translator.PrecedingText;
import org.daisy.dotify.api.translator.ResolvableText;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.daisy.dotify.api.translator.UnsupportedMetricException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Factory of {@link BrailleTranslator} that takes into account preceding and following text. Used
 * in {@link org.daisy.dotify.formatter.test.LeaderTest}.
 */
public class MockBypassTranslatorFactoryService implements BrailleTranslatorFactoryService {

    @Override
    public Collection<TranslatorSpecification> listSpecifications() {
        return new ArrayList<>();
    }

    @Override
    public boolean supportsSpecification(String locale, String mode) {
        try {
            newFactory().newTranslator(locale, mode);
            return true;
        } catch (TranslatorConfigurationException e) {
            return false;
        }
    }

    @Override
    public BrailleTranslatorFactory newFactory() {
        return new BrailleTranslatorFactory() {
            @Override
            public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException {
                if (!("MOCK".equals(mode) && "Brai".equals(Locale.forLanguageTag(locale).getScript()))) {
                    throw new TranslatorConfigurationException("Factory does not support " + locale + "/" + mode);
                }
                return new BrailleTranslator() {
                    @Override
                    public String getTranslatorMode() {
                        return mode;
                    }
                    @Override
                    public BrailleTranslatorResult translate(Translatable input) throws TranslationException {
                        return new Result(input.getText());
                    }
                    @Override
                    public BrailleTranslatorResult translate(TranslatableWithContext input)
                            throws TranslationException {
                        return new Result(input.getTextToTranslate(),
                                          input.getPrecedingText(),
                                          input.getFollowingText());
                    }
                };
            }
        };
    }

    private static class Result implements BrailleTranslatorResult, Cloneable {

        private int index = 0;
        private String[] remainder;
        private String following;

        private Result(String text) {
            if (" ".equals(text)) {
                // If input text is a space, it may be used for calculating the margin character
                // (see org.daisy.dotify.formatter.impl.common.FormatterCoreContext)
                remainder = new String[]{"\u2800"};
            } else {
                remainder = text.replaceAll("[\\s\u2800]+", " ").trim().split(" ");
            }
        }

        private Result(List<ResolvableText> text, List<PrecedingText> preceding, List<FollowingText> following) {
            String remainder = "";
            for (ResolvableText t : text) {
                remainder += t.resolve();
            }
            remainder = remainder.replaceAll("[\\s\u2800]+", " ");
            if (remainder.startsWith(" ")) {
                boolean keep = false;
                for (PrecedingText t : preceding) {
                    if (t.resolve().replaceAll("[\\s\u2800]+", " ").trim().length() > 0) {
                        keep = true;
                        break;
                    }
                }
                if (!keep) {
                    remainder = remainder.substring(1);
                }
            }
            this.remainder = remainder.split(" ");
            this.following = "";
            if (this.remainder[this.remainder.length - 1].length() > 0) {
                for (FollowingText t : following) {
                    String s = t.peek().replaceAll("[\\s\u2800]+", " ");
                    if (s.contains(" ")) {
                        s = s.substring(0, s.indexOf(" "));
                        this.following += s;
                        break;
                    }
                    this.following += s;
                }
            }
        }

        @Override
        public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
            String row = "";
            while (index < remainder.length - 1) {
                if (row.length() + remainder[index].length() <= limit) {
                    row += remainder[index++];
                    if (row.length() + 1 <= limit) {
                        row += "\u2800";
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            if (index == remainder.length - 1
                && row.length() + remainder[index].length() + following.length() <= limit) {
                row += remainder[index++];
            }
            return row.replaceAll("\u00A0", "\u2800");
        }

        @Override
        public boolean hasNext() {
            return index < remainder.length;
        }

        @Override
        public int countRemaining() {
            return getTranslatedRemainder().length();
        }

        @Override
        public String getTranslatedRemainder() {
            return String.join(" ", remainder);
        }

        @Override
        public BrailleTranslatorResult copy() {
            return (BrailleTranslatorResult) clone();
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError("coding error");
            }
        }

        @Override
        public boolean supportsMetric(String metric) {
            return false;
        }

        @Override
        public double getMetric(String metric) {
            throw new UnsupportedMetricException();
        }
    }

    @Override
    public void setCreatedWithSPI() {}
}
