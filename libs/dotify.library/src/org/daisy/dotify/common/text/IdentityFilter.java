package org.daisy.dotify.common.text;

/**
 * Provides an identity filter, in other words the input is returned unchanged.
 *
 * @author Joel Håkansson
 */
public class IdentityFilter implements StringFilter {

    @Override
    public String filter(String str) {
        return str;
    }

}
