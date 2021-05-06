package org.daisy.dotify.api.translator;

/**
 * <p>Provides an interface for texts that may change during
 * the translation process. This may for example happen when
 * the resolvable text is a page number reference.</p>
 *
 * <p>Note that the value that is translated should always be the value returned by
 * {@link #resolve()}, because the following translatable may depend on the accurate
 * history of what has already been translated. However, since the correctness of
 * the result may be depending on <i>when</i> {@link #resolve()} is called, it should not
 * be called prematurely.</p>
 *
 * <p>In some cases it is useful to know beforehand if the content might
 * change between calls to {@link #peek()} or between the last call to
 * {@link #peek()} and the first call to {@link #resolve()}.
 * {@link #isStatic()} makes it possible for an implementation to
 * know if the content could change or not when processing
 * {@link AttributeWithContext} on the translatable.</p>
 *
 * <p>Note that an implementation must still be able to handle both cases somehow
 * and different strategies may be necessary depending on the circumstances.</p>
 *
 * <p>For example, a sequence of two {@link ResolvableText} items are translated
 * separately. The first item is a static text containing two words. When the first
 * item is translated the second item is accessible via the {@link FollowingText} interface.
 * If the second item is also static it is possible to correctly determine if
 * for example phrase markers (typically requiring at least three words) should
 * be used by counting the words in the two items using {@link #resolve()} on the
 * first item and {@link #peek()} on the second.
 * However, if the second item is non-static, an implementation cannot know
 * beforehand what it will return upon its first call to {@link #resolve()}.
 * If {@link #peek()} on the second item returns the empty string, a translator
 * implementation might prefer to use word markers for the first item
 * and would consequently be forced to do the same for the second item if it turns out
 * to be non-empty. Conversely, if {@link #peek()} returns something, an implementation
 * might choose to use phrase markers and run the risk of having to use phrase markers
 * on something that isn't a phrase.</p>
 *
 * <p>Obviously, correct information should always be provided if possible,
 * but no degree of similarity can really be guaranteed between {@link #peek()} and
 * {@link #resolve()}.</p>
 *
 * @author Joel Håkansson
 */
public interface ResolvableText extends FollowingText, PrecedingText {

    /**
     * Peeks the value of this item. The returned string may
     * be different from call to call.
     * <p>
     * Note that after a call to {@link #resolve()}, this method
     * should consistently return the same result.
     *
     * @return the value, never null
     */
    public String peek();

    /**
     * Resolves the item. Once the value has been resolved,
     * it cannot change between calls in the same context.
     * This applies to both {@link #peek()} and {@link #resolve()}.
     *
     * @return the value, never null
     */
    public String resolve();

}
