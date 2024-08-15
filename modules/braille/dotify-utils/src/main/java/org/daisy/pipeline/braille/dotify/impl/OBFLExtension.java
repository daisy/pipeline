package org.daisy.pipeline.braille.dotify.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermList;
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

	private final String prefix;
	private final DeclarationTransformer transformer;

	public OBFLExtension() {
		this("-obfl-");
	}

	/**
	 * @param prefix If specified, properties are only recognized if they begin with this
	 *               string. Must start and end with a '-'.
	 */
	public OBFLExtension(String prefix) {
		super(new SupportedOBFLProperties(true, false, prefix));
		this.prefix = prefix;
		this.transformer = (DeclarationTransformer)css;
	}

	///////////////////////////////////////////////////////////////
	// BrailleCSSExtension
	///////////////////////////////////////////////////////////////

	@Override
	public String getPrefix() {
		return prefix;
	}

	private final static Set<String> customContentFuncNames = new HashSet<String>(Arrays.asList("-obfl-evaluate",
	                                                                                            "-obfl-marker-indicator",
	                                                                                            "-obfl-collection"));

	@Override
	public boolean parseContentTerm(Term<?> term, TermList list) {
		if (term instanceof TermFunction) {
			String funcName = ((TermFunction)term).getFunctionName();
			if (customContentFuncNames.contains(funcName)) {
				list.add(term);
				return true;
			}
		}
		return false;
	}

	///////////////////////////////////////////////////////////////
	// DeclarationTransformer
	///////////////////////////////////////////////////////////////

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		return transformer.parseDeclaration(d, properties, values);
	}
}
