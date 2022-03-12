package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.List;

import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import org.daisy.pipeline.braille.common.TransformProvider.util.Memoize;
import org.daisy.pipeline.braille.common.util.Strings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;

@Component(
	name = "braille-translator-registry",
	service = { BrailleTranslatorRegistry.class }
)
public class BrailleTranslatorRegistry extends Memoize<BrailleTranslator>
                                       implements BrailleTranslatorProvider<BrailleTranslator> {

	public BrailleTranslatorRegistry() {
		super();
	}

	private final List<TransformProvider<BrailleTranslator>> providers = new ArrayList<>();
	private final TransformProvider<BrailleTranslator> dispatch = dispatch(providers);

	@Reference(
		name = "BrailleTranslatorProvider",
		unbind = "-",
		service = BrailleTranslatorProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addProvider(BrailleTranslatorProvider p) {
		providers.add(p);
	}

	public Iterable<BrailleTranslator> _get(Query q) {
		return dispatch.get(q);
	}

	public TransformProvider<BrailleTranslator> _withContext(Logger context) {
		return dispatch.withContext(context);
	}

	@Override
	public String toString() {
		return "memoize(dispatch( " + Strings.join(providers, ", ") + " ))";
	}
}
