package org.daisy.pipeline.tts.impl;

import org.daisy.common.properties.Properties;

import org.osgi.service.component.annotations.Component;

/**
 * Exposes the "org.daisy.pipeline.tts.default-lexicon" property, used to provide a default value
 * for the "lexicon" option in TTS-enabled scripts.
 */
@Component(
	name = "default-lexicon-property",
	immediate = true
)
public class DefaultLexiconProperty {

	protected DefaultLexiconProperty() {
		Properties.getProperty("org.daisy.pipeline.tts.default-lexicon",
		                       true,
		                       "Default user lexicons",
		                       true,
		                       null);
	}
}
