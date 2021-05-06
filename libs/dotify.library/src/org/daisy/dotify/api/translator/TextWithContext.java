package org.daisy.dotify.api.translator;

import java.util.List;

/**
 * Provides a translatable text including context.
 *
 * @author Joel Håkansson
 */
public interface TextWithContext {

    /**
     * Gets the already processed items in this context.
     *
     * @return a list of preceding items
     */
    public List<PrecedingText> getPrecedingText();

    /**
     * Gets the following items in this context.
     *
     * @return a list of following items
     */
    public List<FollowingText> getFollowingText();

    /**
     * Gets the items to translate in this context.
     *
     * @return a list of current items
     */
    public List<ResolvableText> getTextToTranslate();

}
