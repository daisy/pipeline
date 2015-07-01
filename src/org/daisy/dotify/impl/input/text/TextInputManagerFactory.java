package org.daisy.dotify.impl.input.text;

import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.cr.InputManager;
import org.daisy.dotify.api.cr.InputManagerFactory;

import aQute.bnd.annotation.component.Component;

@Component
public class TextInputManagerFactory implements InputManagerFactory {
	private final Set<String> locales;
	private final Set<String> formats;

	public TextInputManagerFactory() {
		this.locales = new HashSet<String>();
		this.locales.add("sv-SE");
		this.locales.add("en-US");
		this.formats = new HashSet<String>();
		this.formats.add("text");
		this.formats.add("txt");
	}

	public boolean supportsSpecification(String locale, String fileFormat) {
		return formats.contains(fileFormat);
	}

	public InputManager newInputManager(String locale, String fileFormat) {
		return new TextInputManager(locale);
	}

	public Set<String> listSupportedLocales() {
		return new HashSet<String>(locales);
	}

	public Set<String> listSupportedFileFormats() {
		return new HashSet<String>(formats);
	}

}
