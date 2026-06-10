package cz.vutbr.web.css;

/**
 * Used for testing whether the user agent supports CSS property:value pairs.
 *
 * @author bert
 */
public interface FeatureSpec {

	boolean supportsDeclaration(Declaration decl);

}
