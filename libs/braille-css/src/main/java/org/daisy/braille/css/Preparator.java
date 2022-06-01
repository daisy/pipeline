package org.daisy.braille.css;

import java.util.List;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleFactory;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.csskit.antlr.SimplePreparator;

import org.w3c.dom.Element;

public class Preparator extends SimplePreparator {
	
	private static final RuleFactory ruleFactoryInstance = new BrailleCSSRuleFactory();
	private final BrailleCSSParserFactory.Context context;
	
	public Preparator(Element e, boolean inlinePriority, BrailleCSSParserFactory.Context context) {
		super(e, inlinePriority, ruleFactoryInstance);
		this.context = context;
	}
	
	public RuleVolume prepareRuleVolume(List<Declaration> declarations,
	                                    List<RuleVolumeArea> volumeAreas,
	                                    String pseudo,
	                                    String pseudoFuncArg) {
		if ((declarations == null || declarations.isEmpty()) &&
		    (volumeAreas == null || volumeAreas.isEmpty())) {
			log.debug("Empty RuleVolume was omited");
			return null; }
		RuleVolume rv = new RuleVolume(pseudo, pseudoFuncArg,
		                               context == BrailleCSSParserFactory.Context.VOLUME
		                               || context == BrailleCSSParserFactory.Context.ELEMENT);
		if (declarations != null)
			for (Declaration d : declarations)
				rv.add(d);
		if (volumeAreas != null)
			for (RuleVolumeArea a : volumeAreas)
				rv.add(a);
		log.info("Create @volume as with:\n{}", rv);
		return rv;
	}
	
	public RuleVolumeArea prepareRuleVolumeArea(String area, List<Declaration> declarations, List<RulePage> pageRules) {
		if ((declarations == null || declarations.isEmpty())) {
			log.debug("Empty RuleVolumeArea was omited");
			return null; }
		RuleVolumeArea rva = new RuleVolumeArea(area);
		if (declarations != null)
			for (Declaration d : declarations)
				rva.add(d);
		if (pageRules != null)
			for (RulePage p : pageRules)
				rva.add(p);
		log.info("Create @" + area + " with:\n{}", rva);
		return rva;
	}

	public RuleTextTransform prepareRuleTextTransform(List<Declaration> declarations) {
		return prepareRuleTextTransform(null, declarations);
	}
	
	public RuleTextTransform prepareRuleTextTransform(String name, List<Declaration> declarations) {
		if ((declarations == null || declarations.isEmpty())) {
			log.debug("Empty RuleTextTransform was omited");
			return null; }
		RuleTextTransform rtt = name == null ? new RuleTextTransform() : new RuleTextTransform(name);
		rtt.replaceAll(declarations);
		log.info("Create @text-transform with:\n{}", rtt);
		return rtt;
	}
	
	public RuleCounterStyle prepareRuleCounterStyle(String name, List<Declaration> declarations) {
		if ((declarations == null || declarations.isEmpty())) {
			log.debug("Empty RuleCounterStyle was omited");
			return null; }
		RuleCounterStyle rcs = new RuleCounterStyle(name);
		rcs.replaceAll(declarations);
		log.info("Create @counter-style with:\n{}", rcs);
		return rcs;
	}
}
