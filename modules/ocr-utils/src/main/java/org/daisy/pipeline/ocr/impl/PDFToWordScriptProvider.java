package org.daisy.pipeline.ocr.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.ocr.OCRProcessor;
import org.daisy.pipeline.ocr.OCRService;
import org.daisy.pipeline.ocr.OCRService.ServiceDisabledException;
import org.daisy.pipeline.pandoc.Pandoc;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.script.ScriptServiceProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pdf-to-word",
	service = { ScriptServiceProvider.class }
)
public class PDFToWordScriptProvider implements ScriptServiceProvider {

	@Override
	public Iterable<ScriptService<?>> getScripts() {
		List<ScriptService<?>> scripts = new ArrayList<>();
		Map<String,String> props = Properties.getSnapshot();
		for (OCRService s : ocrServices)
			try {
				Property enabled = Properties.getProperty("org.daisy.pipeline.ocr." + s.getName() + ".enabled",
				                                          true,
				                                          "Enable " + s.getDisplayName(),
				                                          false,
				                                          "true");
				String str = enabled.getValue(props);
				if (str != null && "false".equals(str.toLowerCase()) || "0".equals(str))
					throw new ServiceDisabledException(
						"In order to enable the " + s.getName() + " service, set property '" + enabled.getName() + "' to 'true'");
				Collection<OCRProcessor> processors = s.getAvailableProcessors(props);
				if (processors.isEmpty()) {
					logger.debug("Not creating script pdf-to-word-" + s.getName() + " because this OCR service has no processors available");
					continue;
				}
				scripts.add(new PDFToWordScript(s, processors, datatypeRegistry, pandoc));
			} catch (OCRService.ServiceDisabledException e) {
				logger.debug("Not creating script pdf-to-word-" + s.getName() + " because OCR service disabled", e);
			}
		return scripts;
	}

	private final List<OCRService> ocrServices = new ArrayList<>();

	@Reference(
		name = "OCRService",
		unbind = "-",
		service = OCRService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addOCRService(OCRService service) {
		ocrServices.add(service);
	}

	private DatatypeRegistry datatypeRegistry;

	@Reference(
		name = "DatatypeRegistry",
		unbind = "-",
		service = DatatypeRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void bindDatatypeRegistry(DatatypeRegistry registry) {
		datatypeRegistry = registry;
	}

	private Pandoc pandoc;

	@Reference(
		name = "Pandoc",
		unbind = "-",
		service = Pandoc.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void bindPandoc(Pandoc pandoc) {
		this.pandoc = pandoc;
	}

	private static final Logger logger = LoggerFactory.getLogger(PDFToWordScriptProvider.class);
}
