package cz.vutbr.web.css;

/**
 * A feature query, used for testing whether the user agent ({@link FeatureSpec})
 * supports CSS property:value pairs.
 *
 * @author bert
 */
public interface FeatureQuery extends Rule<FeatureCondition>, FeatureCondition {

	/**
	 * Invert the condition
	 */
	FeatureQuery negate();

}
