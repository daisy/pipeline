package org.daisy.pipeline.nlp;

import java.util.Collection;
import java.util.Locale;

public class DummyLangDetector implements LangDetector {

	@Override
	public Locale findLang(Locale likelyLang, Collection<String> text) {
		return likelyLang;
	}

	@Override
	public void train() {
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

}
