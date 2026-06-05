package org.daisy.pipeline.tts.impl;

import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.webservice.restlet.WebServiceExtension;

import org.restlet.routing.Router;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "tts-engines-web-service-extension",
	service = { WebServiceExtension.class }
)
public class TTSEnginesWebServiceExtension implements WebServiceExtension {

	static final String TTS_ENGINES_ROUTE = "/tts-engines";

	private TTSRegistry ttsRegistry;

	@Reference(
		name = "TTSRegistry",
		unbind = "-",
		service = TTSRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setTTSRegistry(TTSRegistry registry) {
		ttsRegistry = registry;
	}

	public void attachTo(Router router) {
		router.getContext().getAttributes().put(TTSEnginesResource.TTS_REGISTRY_KEY, ttsRegistry);
		router.attach(TTS_ENGINES_ROUTE, TTSEnginesResource.class);
	}
}
