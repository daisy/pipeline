package org.daisy.braille.css;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.domassign.DeclarationTransformer;

public abstract class BrailleCSSExtension extends DeclarationTransformer implements SupportedCSS {

	protected BrailleCSSExtension(SupportedCSS supportedProperties) {
		super(supportedProperties);
	}

	public abstract String getPrefix();

	@Override
	protected final Map<String,Method> parsingMethods() {
		return null;
	}

	@Override
	public boolean parseDeclaration(Declaration d, Map<String,CSSProperty> properties, Map<String,Term<?>> values) {
		throw new IllegalStateException("parseDeclaration() method must be overridden");
	}

	public boolean parseContentTerm(Term<?> term, TermList list) {
		return false;
	}

	///////////////////////////////////////////////////////////////
	// SupportedCSS
	///////////////////////////////////////////////////////////////

	@Override
	public boolean isSupportedMedia(String media) {
		return css.isSupportedMedia(media);
	}
	@Override
	public final boolean isSupportedCSSProperty(String property) {
		return css.isSupportedCSSProperty(property);
	}
	@Override
	public final CSSProperty getDefaultProperty(String property) {
		return css.getDefaultProperty(property);
	}
	@Override
	public final Term<?> getDefaultValue(String property) {
		return css.getDefaultValue(property);
	}
	@Override
	public final int getTotalProperties() {
		return css.getTotalProperties();
	}
	@Override
	public final Set<String> getDefinedPropertyNames() {
		return css.getDefinedPropertyNames();
	}
	@Override
	public String getRandomPropertyName() {
		return css.getRandomPropertyName();
	}
	@Override
	public int getOrdinal(String propertyName) {
		return css.getOrdinal(propertyName);
	}
	@Override
	public String getPropertyName(int o) {
		return css.getPropertyName(o);
	}
}
