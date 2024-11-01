package org.daisy.pipeline.braille.dotify.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.Selector.PseudoClass;
import cz.vutbr.web.css.Selector.PseudoElement;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.csskit.OutputUtil;
import cz.vutbr.web.domassign.DeclarationTransformer;

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.SelectorImpl.PseudoElementImpl;
import org.daisy.braille.css.VendorAtRule;

import org.osgi.service.component.annotations.Component;

/**
 * @author bert
 */
@Component(
	name = "org.daisy.pipeline.braille.dotify.impl.OBFLExtension",
	service = {
		BrailleCSSExtension.class
	}
)
public class OBFLExtension extends BrailleCSSExtension {

	private final static String prefix = "-obfl-";
	private final DeclarationTransformer transformer;

	public OBFLExtension() {
		super(new SupportedOBFLProperties(true, false, prefix));
		this.transformer = (DeclarationTransformer)css;
	}

	///////////////////////////////////////////////////////////////
	// BrailleCSSExtension
	///////////////////////////////////////////////////////////////

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public boolean parseContentTerm(Term<?> term, TermList list) {
		if (term instanceof TermFunction) {
			TermFunction f = (TermFunction)term;
			String funcName = f.getFunctionName();
			boolean normalize = false;
			if (!funcName.startsWith(prefix)) {
				funcName = prefix + funcName;
				normalize = true;
			}
			if ((prefix + "evaluate").equals(funcName)) {
				if (f.size() != 1)
					return false;
				if (!(f.get(0) instanceof TermString))
					return false;
			} else if ((prefix + "marker-indicator").equals(funcName)) {
				if (f.size() != 2)
					return false;
				if (!(f.get(0) instanceof TermIdent))
					return false;
				if (!(f.get(1) instanceof TermString))
					return false;
			} else if ((prefix + "collection").equals(funcName)) {
				if (f.size() < 1 || f.size() > 2)
					return false;
				if (!(f.get(0) instanceof TermIdent))
					return false;
				if (f.size() == 2 && !(f.get(1) instanceof TermIdent))
					return false;
			} else
				return false;
			if (normalize)
				f = f.setFunctionName(funcName);
			list.add(term);
			return true;
		}
		return false;
	}

	@Override
	public TermIdent parseCounterName(Term<?> term) {
		TermIdent counterName = null;
		if (term instanceof TermIdent) {
			TermIdent ident = (TermIdent)term;
			String name = ident.getValue();
			if (name.startsWith(prefix))
				name = name.substring(prefix.length());
			if ("page".equals(name) ||
			    "volume".equals(name))
				counterName = ident;
			else if ("volumes".equals(name) ||
			         "sheets-in-document".equals(name) ||
			         "sheets-in-volume".equals(name) ||
			         "started-volume-number".equals(name) ||
			         "started-page-number".equals(name) ||
			         "started-volume-first-content-page".equals(name)) {
				counterName = ident;
				name = prefix + name;
			}
			if (counterName != null && !counterName.getValue().equals(name))
				// normalize
				counterName.setValue(name);
		}
		if (counterName != null)
			return counterName;
		else
			throw new IllegalArgumentException("Unknown counter name " + term);
	}

	@Override
	public PseudoClass createPseudoClass(String name) throws IllegalArgumentException {
		return createPseudoClassFunction(name);
	}

	@Override
	public PseudoClass createPseudoClassFunction(String name, String... args) throws IllegalArgumentException {
		if (name.startsWith(":"))
			name = name.substring(1); // should not happen
		if (name.startsWith(prefix))
			name = name.substring(prefix.length()); // should not happen
		OBFLPseudoClass.Type type = null; {
			try {
				type = OBFLPseudoClass.Type.valueOf(name.replaceAll("-", "_").toUpperCase());
			} catch (IllegalArgumentException e) {
			}
		}
		if (type != null)
			return new OBFLPseudoClass(type, args);
		throw new IllegalArgumentException("Unknown pseudo-class :" + name);
	}

	@Override
	public PseudoElement createPseudoElement(String name) throws IllegalArgumentException {
		if (name.startsWith(":"))
			// maybe a single colon was used for a pseudo element
			name = name.substring(1);
		if (name.startsWith(prefix))
			name = name.substring(prefix.length());
		try {
			OBFLPseudoElement.Type type = OBFLPseudoElement.Type.valueOf(name.replaceAll("-", "_").toUpperCase());
			return new OBFLPseudoElement(type);
		} catch (IllegalArgumentException e) {
		}
		// when pseuo class is prefixed with -obfl-, BrailleCSSTreeParser calls RuleFactory.createPseudoElement()
		// without first trying RuleFactory.createPseudoClass()
		OBFLPseudoClass.Type type = null; {
			try {
				type = OBFLPseudoClass.Type.valueOf(name.replaceAll("-", "_").toUpperCase());
			} catch (IllegalArgumentException e) {
			}
		}
		if (type != null)
			return new OBFLPseudoClass(type);
		throw new IllegalArgumentException("Unknown pseudo-element ::" + name);
	}

	@Override
	public PseudoElement createPseudoElementFunction(String name, String... args) throws IllegalArgumentException {
		if (name.startsWith(":"))
			// maybe a single colon was used for a pseudo element, or it is a prefixed pseudo class
			name = name.substring(1);
		if (name.startsWith(prefix))
			name = name.substring(prefix.length());
		// when pseuo class is prefixed with -obfl-, BrailleCSSTreeParser calls RuleFactory.createPseudoElementFunction()
		// without first trying RuleFactory.createPseudoClassFunction()
		OBFLPseudoClass.Type type = null; {
			try {
				type = OBFLPseudoClass.Type.valueOf(name.replaceAll("-", "_").toUpperCase());
			} catch (IllegalArgumentException e) {
			}
		}
		if (type != null)
			return new OBFLPseudoClass(type, args);
		throw new IllegalArgumentException("Unknown pseudo-element ::" + name);
	}

	// extends PseudoElementImpl to make it stackable (see SelectorImpl)
	private static class OBFLPseudoClass extends PseudoElementImpl implements PseudoClass {

		private enum Type {
			ALTERNATE_SCENARIO("alternate-scenario");

			private final String name;

			private Type(String name) {
				this.name = name;
			}
		}

		private OBFLPseudoClass(Type type, String... args) {
			// pass ":" to indicate to PseudoElementImpl that it is a custom pseudo class
			super(":" + prefix + type.name, args);
			switch (type) {
			case ALTERNATE_SCENARIO:
				switch (args.length) {
				case 0:
					break; // :-obfl-alternate-scenario is equivalent to :-obfl-alternate-scenario(1)
				case 1:
					try {
						Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException(
							"Argument of :" + prefix + type.name + " pseudo-class must be an integer", e);
					}
					break;
				default:
					throw new IllegalArgumentException(":" + prefix + type.name + " pseudo-class accepts at most 1 argument");
				}
				break;
			default: // can not happen
			}
		}
	}

	private static class OBFLPseudoElement extends PseudoElementImpl {

		private enum Type {
			ON_COLLECTION_START("on-collection-start"),
			ON_COLLECTION_END("on-collection-end"),
			ON_TOC_START("on-toc-start"),
			ON_TOC_END("on-toc-end"),
			ON_VOLUME_START("on-volume-start"),
			ON_VOLUME_END("on-volume-end"),
			ON_RESUMED("on-resumed");

			private final String name;

			private Type(String name) {
				this.name = name;
			}
		}

		private OBFLPseudoElement(Type type) {
			super(prefix + type.name);
		}
	}

	@Override
	public VendorAtRule<? extends Rule<?>> createAtRule(String name, List<Rule<?>> content) throws IllegalArgumentException {
		if (name.startsWith(prefix))
			name = name.substring(prefix.length());
		if ("volume-transition".equals(name)) {
			for (Rule<?> r : content)
				if (!(r instanceof OBFLRuleVolumeTransition))
					throw new IllegalArgumentException("Not allowed inside @" + prefix + "volume-transition rule: " + r);
			return new OBFLRuleVolumeTransitions((List<OBFLRuleVolumeTransition>)(List)content);
		}
		OBFLRuleVolumeTransition.Type type = null; {
			try {
				type = OBFLRuleVolumeTransition.Type.valueOf(name.replaceAll("-", "_").toUpperCase());
			} catch (IllegalArgumentException e) {
			}
		}
		if (type != null) {
			for (Rule<?> r : content)
				if (!(r instanceof Declaration))
					throw new IllegalArgumentException("Not allowed inside @" + type.name + ": " + r);
			return new OBFLRuleVolumeTransition(type, (List<Declaration>)(List)content);
		}
		throw new IllegalArgumentException("Unknown at-rule @" + name);
	}

	public static class OBFLRuleVolumeTransitions extends VendorAtRule<OBFLRuleVolumeTransition> implements PrettyOutput {

		private OBFLRuleVolumeTransitions(List<OBFLRuleVolumeTransition> transitions) throws IllegalArgumentException {
			super(prefix + "volume-transition", transitions);
		}

		@Override
		public String toString(int depth) {
			StringBuilder s = new StringBuilder();
			s.append("@" + getName());
			s.append(" ");
			s.append(OutputUtil.RULE_OPENING);
			s = OutputUtil.appendList(s, list, OutputUtil.EMPTY_DELIM, depth + 1);
			s.append(OutputUtil.RULE_CLOSING);
			return s.toString();
		}

		@Override
		public String toString() {
			return toString(0);
		}
	}

	public static class OBFLRuleVolumeTransition extends VendorAtRule<Declaration> implements PrettyOutput {

		public enum Type {
			SEQUENCE_INTERRUPTED("sequence-interrupted"),
			SEQUENCE_RESUMED("sequence-resumed"),
			ANY_INTERRUPTED("any-interrupted"),
			ANY_RESUMED("any-resumed");

			private final String name;

			private Type(String name) {
				this.name = name;
			}
		}

		private OBFLRuleVolumeTransition(Type type, List<Declaration> declarations) {
			super(type.name, declarations);
		}

		@Override
		public String toString(int depth) {
			StringBuilder s = new StringBuilder();
			s.append("@" + prefix + getName());
			s.append(" ");
			s.append(OutputUtil.RULE_OPENING);
			s = OutputUtil.appendList(s, list, OutputUtil.RULE_DELIM, depth + 1);
			s.append(OutputUtil.RULE_CLOSING);
			return s.toString();
		}

		@Override
		public String toString() {
			return toString(0);
		}
	}

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		// note that SupportedBrailleCSS already normalizes property names, so no need to do it here
		return transformer.parseDeclaration(d, properties, values);
	}
}
