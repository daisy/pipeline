package org.daisy.pipeline.tts.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.config.DynamicPropertiesExtension;
import org.daisy.pipeline.tts.config.VoiceConfigExtension;
import org.daisy.pipeline.tts.TTSLog;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.VoiceManager;
import org.daisy.pipeline.webservice.restlet.WebServiceExtension;

import org.restlet.routing.Router;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;

import org.xml.sax.InputSource;

@Component(
	name = "voices-web-service-extension",
	service = { WebServiceExtension.class }
)
public class VoicesWebServiceExtension implements WebServiceExtension {

	static final String VOICES_ROUTE = "/voices";
	static final String VOICE_ROUTE = "/voices/{id}";
	static final String VOICE_PREVIEW_ROUTE = "/voices/{id}/preview";

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

	private AudioServices audioServices;

	@Reference(
		name = "AudioServices",
		unbind = "-",
		service = AudioServices.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setAudioServices(AudioServices services) {
		audioServices = services;
	}

	static final String CONTEXT_ATTRIBUTE_KEY = "voices-web-service-extension";

	public void attachTo(Router router) {
		router.getContext().getAttributes().put(CONTEXT_ATTRIBUTE_KEY, this);
		router.attach(VOICES_ROUTE, VoicesResource.class);
		router.attach(VOICE_ROUTE, VoiceResource.class);
		router.attach(VOICE_PREVIEW_ROUTE, VoicePreviewResource.class);
	}

	private VoiceManager rememberVoiceManager = null;
	private Map<String,String> rememberProperties = null;

	boolean hasRememberedVoiceManager() {
		return rememberVoiceManager != null;
	}

	VoiceManager getRememberedVoiceManager(Map<String,String> properties) {
		if (rememberProperties == null)
			return null;
		if (rememberProperties.equals(properties))
			return rememberVoiceManager;
		else
			return null;
	}

	VoiceManager getVoiceManager(Map<String,String> properties, XdmNode configXML, Logger logger) {
		VoiceConfigExtension voiceConfigExt = new VoiceConfigExtension();
		DynamicPropertiesExtension propsExt = new DynamicPropertiesExtension();
		new ConfigReader(saxonProcessor, configXML, properties, voiceConfigExt, propsExt);
		if (configXML != null) {
			logger.debug("Voice configuration XML:\n" + configXML);
			logger.debug("Parsed voice configuration:\n" + voiceConfigExt.getVoiceDeclarations());
		}
		Map<String,String> dynProperties = propsExt.getDynamicProperties();
		synchronized (this) {
			if (dynProperties != null && !dynProperties.isEmpty()) {
				properties = new HashMap<>(properties);
				properties.putAll(dynProperties);
				// forget everything if any dynamic properties are present
				rememberProperties = null;
				rememberVoiceManager = null;
			} else if (rememberProperties != null && rememberProperties.equals(properties))
				return rememberVoiceManager;
			else
				rememberProperties = properties;
		}
		VoiceManager m = new VoiceManager(
			ttsRegistry.getWorkingEngines(properties, new TTSLog(logger), logger),
			voiceConfigExt.getVoiceDeclarations());
		if (rememberProperties != null)
			rememberVoiceManager = m;
		return m;
	}

	AudioEncoder getAudioEncoder(AudioFileFormat.Type audioFileType) {
		return audioServices.newEncoder(audioFileType, Collections.emptyMap()).orElse(null);
	}

	private static final Processor saxonProcessor = new Processor(false);

	XdmNode getConfigXML(String ttsConfig) throws IOException, SaxonApiException {
		if (ttsConfig == null)
			return null;
		return saxonProcessor.newDocumentBuilder().build(
			new SAXSource(new InputSource(new StringReader(ttsConfig))));
	}

}
