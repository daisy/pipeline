import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.file.URLs;
import org.daisy.common.spi.ActivationException;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.calabash.XProcBasedTransformer;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;

public class UppercaseTransform extends AbstractBrailleTranslator implements BrailleTranslator, XProcStepProvider {
	
	private final XProcStepProvider stepProvider;
	
	private UppercaseTransform(XProcStepProvider stepProvider) {
		this.stepProvider = stepProvider;
	}
	
	public FromStyledTextToBraille fromStyledTextToBraille() {
		return fromStyledTextToBraille;
	}
	
	private FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
		public Iterable<CSSStyledText> transform(Iterable<CSSStyledText> styledText, int from, int to) {
			if (from != 0 && to >= 0)
				throw new UnsupportedOperationException();
			List<CSSStyledText> ret = new ArrayList<>();
			for (CSSStyledText t : styledText)
				ret.add(new CSSStyledText(t.getText().toUpperCase()));
			return ret;
		}
	};
	
	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		return stepProvider.newStep(runtime, step, monitor, properties);
	}
	
	private static final Iterable<UppercaseTransform> empty = Optional.<UppercaseTransform>absent().asSet();
	
	@Component(
		name = "UppercaseTransform.Provider",
		service = {
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public static class Provider implements BrailleTranslatorProvider<UppercaseTransform> {
		private Logger logger;
		public Provider() {
		}
		private Provider(Provider from, Logger context) {
			logger = context;
			stepProvider = from.stepProvider;
		}
		public Iterable<UppercaseTransform> get(Query query) {
			if (query.toString().equals("(uppercase)")) {
				Iterable<UppercaseTransform> instance = Optional.of(new UppercaseTransform(stepProvider)).asSet();
				if (logger != null)
					logger.info("Selecting " + instance);
				return instance; }
			else
				return empty;
		}
		public TransformProvider<UppercaseTransform> withContext(Logger context) {
			return new Provider(this, context);
		}
		
		private ModuleRegistry moduleRegistry = null;
		
		@Reference(
			name = "ModuleRegistry",
			unbind = "-",
			service = ModuleRegistry.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindModulesRegistry(ModuleRegistry registry) {
			moduleRegistry = registry;
		}
		
		private XProcStepProvider stepProvider = null;
		
		@Activate
		protected void activate(final Map<?,?> properties) throws RuntimeException {
			try {
				Module m = moduleRegistry.getModuleByClass(UppercaseTransform.class);
				stepProvider = new XProcBasedTransformer(
					URLs.asURI(m.getResource("../uppercase.xpl")),
					null);
			} catch (NoSuchFileException e) {
				String errorMessage = e.getMessage();
				try {
					SPIHelper.failToActivate(errorMessage);
				} catch (NoClassDefFoundError ee) {
					// we are probably in OSGi context
					throw new RuntimeException(errorMessage);
				}
			}
		}
	}

	// FIXME: move to utility class
	// static nested class in order to delay class loading
	private static class SPIHelper {
		private SPIHelper() {}
		public static void failToActivate(String message) throws ActivationException {
			throw new ActivationException(message);
		}
	}
}
