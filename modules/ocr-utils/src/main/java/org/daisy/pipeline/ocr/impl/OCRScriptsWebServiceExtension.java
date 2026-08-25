package org.daisy.pipeline.ocr.impl;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptServiceProvider;
import org.daisy.pipeline.webservice.restlet.WebServiceExtension;

import org.restlet.routing.Router;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "ocr-scripts-web-service-extension",
	service = { WebServiceExtension.class }
)
public class OCRScriptsWebServiceExtension implements WebServiceExtension {

	static final String OCR_SCRIPTS_ROUTE = "/ocr/scripts";
	static final String OCR_SCRIPT_ROUTE = "/ocr/scripts/{id}";

	private List<Function<Map<String,String>,ScriptServiceProvider>> providers = new ArrayList<>();

	@Reference(
		name = "PDFToWordScriptProvider",
		unbind = "-",
		service = PDFToWordScriptProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setPDFToWordScriptProvider(PDFToWordScriptProvider provider) {
		providers.add(props -> () -> provider.getScripts(props));
	}

	public void attachTo(Router router) {
		router.getContext().getAttributes().put(OCRScriptsResource.OCR_SCRIPTS_KEY, this);
		router.attach(OCR_SCRIPTS_ROUTE, OCRScriptsResource.class);
		router.attach(OCR_SCRIPT_ROUTE, OCRScriptResource.class);
	}

	ScriptRegistry getScriptRegistry(Client client) {
		String clientID = client != null ? client.getId() : null;
		Map<String,String> props = Properties.getProperties(clientID).getSnapshot();
		return new ScriptRegistry(true) {
			@Override
			protected Iterable<ScriptServiceProvider> getScriptProviders() {
				return Iterables.transform(providers, f -> f.apply(props));
			}
		};
	}
}
