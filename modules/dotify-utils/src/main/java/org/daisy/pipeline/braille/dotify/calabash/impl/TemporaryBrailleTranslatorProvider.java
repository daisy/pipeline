package org.daisy.pipeline.braille.dotify.calabash.impl;

import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.util.Function0;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "TemporaryBrailleTranslatorProvider",
	service = {
		BrailleTranslatorProvider.class,
		TemporaryBrailleTranslatorProvider.class
	}
)
public class TemporaryBrailleTranslatorProvider extends AbstractTransformProvider<BrailleTranslator>
                                                implements BrailleTranslatorProvider<BrailleTranslator> {

	@Override
	public Function0<Void> provideTemporarily(BrailleTranslator t) {
		return super.provideTemporarily(t);
	}

	@Override
	protected Iterable<BrailleTranslator> _get(Query query) {
		return AbstractTransformProvider.util.Iterables.<BrailleTranslator>empty();
	}
}
