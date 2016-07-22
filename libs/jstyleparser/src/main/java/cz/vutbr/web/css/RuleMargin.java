package cz.vutbr.web.css;

/**
 * Contains CSS rules associated with a specific area in the page margin.
 * 
 * @author Bert Frees, 2012-2015
 */
public interface RuleMargin extends RuleBlock<Declaration>, PrettyOutput {
	
	/**
	 * Returns margin area
	 * @return Margin area
	 */
	public String getMarginArea();
	
}
