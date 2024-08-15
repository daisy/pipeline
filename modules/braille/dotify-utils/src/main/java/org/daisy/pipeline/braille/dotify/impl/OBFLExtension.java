package org.daisy.pipeline.braille.dotify.impl;

import java.util.Arrays;
import java.util.Map;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.domassign.DeclarationTransformer;

import org.daisy.braille.css.BrailleCSSExtension;

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

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		// note that SupportedBrailleCSS already normalizes property names, so no need to do it here
		return transformer.parseDeclaration(d, properties, values);
	}
}
