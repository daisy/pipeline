package org.daisy.braille.css;

import java.util.Locale;

import cz.vutbr.web.css.PrettyOutput;

public interface LanguageRange extends PrettyOutput {

	public boolean matches(Locale language);

}
