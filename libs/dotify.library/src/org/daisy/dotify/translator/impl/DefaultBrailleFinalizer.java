package org.daisy.dotify.translator.impl;

import org.daisy.dotify.translator.BrailleFinalizer;

/**
 * Provides a default braille finalizer which maps spaces to
 * braille space (0x2800) and hyphens to braille character
 * 3-6 (0x2824). For a configurable version of this class
 * see {@link ConfigurableBrailleFinalizer}.
 *
 * @author Joel Håkansson
 */
public class DefaultBrailleFinalizer implements BrailleFinalizer {
    @Override
    public String finalizeBraille(String input) {
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case ' ':
                    sb.append('\u2800');
                    break;
                case '\u00a0':
                    sb.append('\u2800');
                    break;
                case '-':
                    sb.append('\u2824');
                    break;
                case '\u00ad':
                    sb.append('\u2824');
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
