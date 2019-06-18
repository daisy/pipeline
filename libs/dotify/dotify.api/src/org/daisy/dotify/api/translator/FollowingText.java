package org.daisy.dotify.api.translator;

/**
 * Provides information about an item that comes after the items 
 * being translated.
 * 
 * @author Joel Håkansson
 * @see ResolvableText
 */
public interface FollowingText extends TextProperties {
	
	/**
	 * Peeks the value of this item. The returned string may
	 * be different from call to call.
	 * 
	 * @return the value, never null
	 */
	public String peek();
	
	/**
	 * Returns true if this item does not change.
	 * @return true if this item is static, false otherwise
	 */
	public boolean isStatic();

}
