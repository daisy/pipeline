package cz.vutbr.web.css;

/**
 * Contains CSS rules associated with a {@link FeatureCondition}.
 *
 * @author bert
 */
public interface RuleSupports extends RuleBlock<RuleBlock<?>>, PrettyOutput {

	FeatureCondition getCondition();

}
