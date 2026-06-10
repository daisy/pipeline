package cz.vutbr.web.css;

/**
 * A logical condition on one or more user agent features.
 *
 * @author bert
 */
public interface FeatureCondition extends PrettyOutput {
	boolean isSatisfied(FeatureSpec userAgent);
}
