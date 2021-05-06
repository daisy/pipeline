package org.daisy.dotify.api.translator;

import java.util.List;
import java.util.Optional;

/**
 * <p>Provides a specification for a list of texts to translate.
 * This class is best suited for use cases where the input is
 * naturally segmented and contains some segments with dynamic content
 * (in other words, content that may change after it has been
 * submitted to a {@link BrailleTranslator} or a {@link BrailleFilter}).</p>
 *
 * <p>This class has the following characteristics:</p>
 * <ul>
 * <li>it applies to a list of text segments</li>
 * <li>style attributes apply to whole segments only</li>
 * <li>style attributes may span more than one instance of {@link TranslatableWithContext}</li>
 * <li>a segment can have a content size that is unknown when the translatable is created</li>
 * <li>it provides information about the context of the text segments to translate</li>
 * </ul>
 *
 * @author Joel Håkansson
 * @see Translatable
 */
public class TranslatableWithContext {
    private final TextWithContext texts;
    private final Optional<AttributeWithContext> attributes;

    /**
     * Provides a builder for translatable objects.
     *
     * @author Joel Håkansson
     */
    public static class Builder {
        private final TextWithContext texts;
        private AttributeWithContext attributes = null;

        private Builder(TextWithContext texts) {
            this.texts = texts;
        }

        /**
         * Sets the attribute context for this builder.
         *
         * @param value the attribute context
         * @return this object
         */
        public Builder attributes(AttributeWithContext value) {
            this.attributes = value;
            return this;
        }

        /**
         * Builds a new Translatable object using the current
         * status of this builder.
         *
         * @return a Translatable instance
         */
        public TranslatableWithContext build() {
            return new TranslatableWithContext(this);
        }

    }

    private TranslatableWithContext(Builder builder) {
        this.texts = builder.texts;
        this.attributes = Optional.ofNullable(builder.attributes);
    }

    /**
     * Creates a new Translatable.Builder with the specified text.
     *
     * @param texts the text to translate
     * @return a new Translatable.Builder
     */
    public static Builder text(TextWithContext texts) {
        return new Builder(texts);
    }

    /**
     * <p>Returns a new Translatable builder for this list.
     * The current item is the specified <code>index</code>.</p>
     *
     * @param list  the list
     * @param index the index of the current item
     * @param <T>   the type of items
     * @return a new context
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   (<code>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</code>)
     */
    public static <T extends ResolvableText> Builder from(List<T> list, int index) {
        return new Builder(newContext(list, index));
    }

    /**
     * <p>Returns a new Translatable builder for this list.
     * The current items are between the specified <code>fromIndex</code>,
     * inclusive, and <code>toIndex</code>, exclusive. (If
     * <code>fromIndex</code> and <code>toIndex</code> are equal, the current items list
     * is empty, however this isn't useful in practice.)</p>
     *
     * @param list      the list
     * @param fromIndex low endpoint (inclusive) of the current subList
     * @param toIndex   high endpoint (exclusive) of the current subList
     * @param <T>       the type of items
     * @return a new context
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   (<code>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</code>)
     */
    public static <T extends ResolvableText> Builder from(List<T> list, int fromIndex, int toIndex) {
        return new Builder(newContext(list, fromIndex, toIndex));
    }

    /**
     * <p>Returns a context for this list where the current item is at the specified
     * <code>index</code>.</p>
     *
     * <p>See {@link #newContext(int, int)} for more information about the returned
     * lists.</p>
     *
     * @param list  the list
     * @param index the index of the current item
     * @param <T>   the type of resolvable
     * @return a new context
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   (<code>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</code>)
     */
    static <T extends ResolvableText> TextWithContext newContext(List<T> list, int index) {
        return newContext(list, index, index + 1);
    }

    /**
     * <p>Returns a context for this list where the current items are between the specified
     * <code>fromIndex</code>, inclusive, and <code>toIndex</code>, exclusive. (If
     * <code>fromIndex</code> and <code>toIndex</code> are equal, the current items list
     * is empty, however this isn't useful in practice.)</p>
     *
     * <p>The returned list is backed by the original list, so non-structural
     * changes in the returned list are reflected in this list, and vice-versa.</p>
     *
     * <p>The semantics of the lists returned by this method become undefined if
     * the backing list is <i>structurally modified</i> in
     * any way other than via the returned lists.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)</p>
     *
     * @param list      the list
     * @param fromIndex low endpoint (inclusive) of the current subList
     * @param toIndex   high endpoint (exclusive) of the current subList
     * @param <T>       the type of resolvable
     * @return a new context
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   (<code>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</code>)
     */
    static <T extends ResolvableText> TextWithContext newContext(List<T> list, int fromIndex, int toIndex) {
        return new TextWithContext() {
            private final List<? extends PrecedingText> preceding = list.subList(0, fromIndex);
            private final List<? extends FollowingText> following = list.subList(toIndex, list.size());
            private final List<? extends ResolvableText> current = list.subList(fromIndex, toIndex);

            @SuppressWarnings("unchecked")
            @Override
            public List<PrecedingText> getPrecedingText() {
                return (List<PrecedingText>) preceding;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<FollowingText> getFollowingText() {
                return (List<FollowingText>) following;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<ResolvableText> getTextToTranslate() {
                return (List<ResolvableText>) current;
            }
        };
    }

    /**
     * Gets the text items in the context that preceded
     * the current text. The preceding text may have been
     * translated with the same translator, or it may have been
     * translated with another translator.
     *
     * @return the preceding text items
     */
    public List<PrecedingText> getPrecedingText() {
        return texts.getPrecedingText();
    }

    /**
     * Gets the text items in the context that follows
     * the current text. The following text may be translated
     * with the same translator, or it may be translated with
     * another translator.
     *
     * @return the following text items
     */
    public List<FollowingText> getFollowingText() {
        return texts.getFollowingText();
    }

    /**
     * Gets the text items to translate.
     *
     * @return the text to translate
     */
    public List<ResolvableText> getTextToTranslate() {
        return texts.getTextToTranslate();
    }

    /**
     * Gets the attribute context.
     *
     * @return the attribute context
     */
    public Optional<AttributeWithContext> getAttributes() {
        return attributes;
    }
}
