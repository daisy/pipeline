package cz.vutbr.web.css;

/**
 * Special case of rule, where rule is meant to be comparable
 * with other rules to determine priority of CSS declarations
 * @author kapy
 *
 * @param <T> Internal content of rule
 */
public interface RuleBlock<T> extends Rule<T>, Cloneable {

	public Object clone();
	
}
