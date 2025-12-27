package cz.vutbr.web.css;

import java.util.List;

import cz.vutbr.web.css.StyleSheet.Origin;

/**
 * Base class for elements of CSS definition.
 * All rules can be created as immutable objects, but
 * this immutability can be changed by functions
 * {@code unlock()} or {@code replaceAll()} 
 * Rule is generally collection of other, finer grained object.
 * 
 * @author kapy
 */
public interface Rule<T> extends List<T> {  
	   
	/**
	 * Replaces all elements stored inside. Replaces changes
	 * whole collection, so it can be used to unlock immutable object.
	 * 	 
	 * @param replacement New list
	 * @return Modified collection
	 */
	Rule<T> replaceAll(List<T> replacement);
	
	/**
	 * Unlocks immutable object by changing collection from
	 * immutable to mutable
	 * @return Modified collection
	 */
	Rule<T> unlock();
	
	/**
	 * Returns underlying collection as list. This list is shared
	 * with Rule, so can be directly modified
	 * @return Underlying collection
	 */
	List<T> asList();
	
	/**
	 * Sets the origin of the owner style sheet (user agent, user, or author) that contains this rule.
	 * @param origin The origin to be set
	 */
	public void setOrigin(Origin origin);
	
	/**
	 * Gets the origin of the owner style sheet (user agent, user, or author) that contains this rule.
	 * @return the origin of the stylesheet.
	 */
	public Origin getOrigin();

	/**
	 * Sets the media query list associated with this rule.
	 *
	 * An empty list or {@code null} means that this rule applies to all media (equivalent to "all").
	 */
	public void setMediaQueries(MediaQueryList media);

	/**
	 * Gets the media query list associated with this rule.
	 *
	 * An empty list or {@code null} means that this rule applies to all media (equivalent to "all").
	 */
	public MediaQueryList getMediaQueries();

}
