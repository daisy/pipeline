package org.daisy.pipeline.braille.common.saxon.impl;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.braille.common.BrailleCode;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "BrailleCode",
	service = { ExtensionFunctionProvider.class }
)
public class BrailleCodeFunctionProvider extends ReflexiveExtensionFunctionProvider {

	public BrailleCodeFunctionProvider() {
		super();
		addExtensionFunctionDefinitionsFromClass(BrailleCodeFunctions.class, new BrailleCodeFunctions());
	}

	public class BrailleCodeFunctions {

		public String getSystem(BrailleCode code) {
			return code.getSystem();
		}

		public String getGrade(BrailleCode code) {
			return code.getGrade().toString();
		}

		public Optional<String> getSpecialization(BrailleCode code) {
			return code.getSpecialization().map(BrailleCode.Specialization::toString);
		}

		public BrailleCode fromLanguageTag(Locale language) throws IllegalArgumentException {
			return BrailleCode.fromLanguageTag(language);
		}

		/**
		 * Get information about the braille code implementation (e.g. Liblouis table).
		 *
		 * @throws RuntimeException if no implementation can be found of the braille code.
		 */
		public Map<String,String> getImplementationInfo(Object code) {
			if (code instanceof String)
				return getImplementationInfo(BrailleCode.parse((String)code));
			else if (code instanceof BrailleCode)
				return getImplementationInfo((BrailleCode)code);
			else
				throw new IllegalArgumentException("Expected BrailleCode or String");
		}

		private Map<String,String> getImplementationInfo(BrailleCode code) {
			return getImplementation(code).getInfo();
		}

		private BrailleTranslator getImplementation(BrailleCode code) {
			for (BrailleTranslator t : translatorRegistry.get(mutableQuery().add("braille-code", code.toString())))
				return t;
			throw new RuntimeException("No implementation found of braille code '" + code + "'");
		}
	}

	private BrailleTranslatorRegistry translatorRegistry;

	@Reference(
		name = "BrailleTranslatorRegistry",
		unbind = "-",
		service = BrailleTranslatorRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindBrailleTranslatorRegistry(BrailleTranslatorRegistry registry) {
		translatorRegistry = registry.withContext(logger);
		logger.debug("Binding BrailleTranslator registry: {}", registry);
	}

	private static final Logger logger = LoggerFactory.getLogger(BrailleCodeFunctionProvider.class);
}
