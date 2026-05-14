package org.daisy.pipeline.nlp;

import java.util.Collection;
import java.util.Locale;

public interface LangDetector {

	Locale findLang(Locale likelyLang, Collection<String> text);

	/**
	 * Light training phase.
	 */
	void train();

	/**
	 * Allocate resources for further detections.
	 */
	void enable();

	/**
	 * Release the resources allocated by enable(). It has no effect on the
	 * trained parameters.
	 */
	void disable();
}
