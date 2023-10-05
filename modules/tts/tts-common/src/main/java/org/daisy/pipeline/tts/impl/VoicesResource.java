package org.daisy.pipeline.tts.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Locale;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.config.VoiceConfigExtension;
import org.daisy.pipeline.tts.TTSLog;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import static org.daisy.pipeline.tts.VoiceInfo.NO_DEFINITE_LANG;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import org.daisy.pipeline.tts.VoiceManager;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

public class VoicesResource extends AuthenticatedResource {

	static final String TTS_REGISTRY_KEY = "tts-registry";

	private static final Logger logger = LoggerFactory.getLogger(VoicesResource.class.getName());
	private static final Processor saxonProcessor = new Processor(false);

	private TTSRegistry ttsRegistry;
	private String engineAttr;
	private String nameAttr;
	private String langAttr;
	private String genderAttr;

	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
		ttsRegistry = (TTSRegistry)getContext().getAttributes().get(TTS_REGISTRY_KEY);
		engineAttr = getQuery().getFirstValue("engine");
		nameAttr = getQuery().getFirstValue("name");
		langAttr = getQuery().getFirstValue("lang");
		genderAttr = getQuery().getFirstValue("gender");
	}

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Get("xml")
	public Representation getResource() {
		return getResource(null);
	}

	/**
	 * @param ttsConfig
	 */
	@Post("xml")
	public Representation getResource(Representation ttsConfig) {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		VoiceManager voiceManager; {
			VoiceConfigExtension voiceConfigExt = new VoiceConfigExtension();
			XdmNode configXML = null; {
				if (ttsConfig != null) {
					try {
						configXML = saxonProcessor.newDocumentBuilder().build(
							new SAXSource(new InputSource(new StringReader(ttsConfig.getText()))));
					} catch (IOException|SaxonApiException e) {
						logger.error("bad request:", e);
						setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
						return getErrorRepresentation(e.getMessage());
					}
				}
			}
			ConfigReader cr = new ConfigReader(saxonProcessor, configXML, voiceConfigExt);
			if (configXML != null) {
				logger.debug("Voice configuration XML:\n" + configXML);
				logger.debug("Parsed voice configuration:\n" + voiceConfigExt.getVoiceDeclarations());
			}
			voiceManager = new VoiceManager(
				ttsRegistry.getWorkingEngines(cr.getAllProperties(), new TTSLog(logger), logger),
				voiceConfigExt.getVoiceDeclarations());
		}
		Iterable<Voice> availableVoices; {
			Locale lang; {
				try {
					lang = langAttr == null ? null : VoiceInfo.tagToLocale(langAttr);
					Gender gender = Gender.of(genderAttr);
					if (gender != null || genderAttr == null) {
						String voiceEngine = engineAttr;
						String voiceName = nameAttr;
						logger.debug("Getting voices for query " + getQuery());
						availableVoices = voiceManager.findAvailableVoices(voiceEngine, voiceName, lang, gender);
					} else {
						logger.error("Could not parse gender '" + genderAttr + "'");
						availableVoices = Collections.EMPTY_LIST;
					}
				} catch (UnknownLanguage e) {
					logger.error(e.getMessage());
					availableVoices = Collections.EMPTY_LIST;
				}
			}
		}
		Document voicesDoc; {
			voicesDoc = XmlUtils.createDom("voices");
			Element voicesElem = voicesDoc.getDocumentElement();
			String baseURL = getRequest().getRootRef().toString();
			voicesElem.setAttribute("href", baseURL + VoicesWebServiceExtension.VOICES_ROUTE);
			for (Voice v : availableVoices) {
				Element voiceElem = voicesDoc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "voice");
				voiceElem.setAttribute("name", v.getName());
				voiceElem.setAttribute("engine", v.getEngine());
				if (v.getLocale().isPresent()) {
					Locale lang = v.getLocale().get();
					voiceElem.setAttribute("lang", lang == NO_DEFINITE_LANG ? "*" : lang.toLanguageTag());
				}
				if (v.getGender().isPresent()) {
					Gender gender = v.getGender().get();
					voiceElem.setAttribute("gender", gender.toString());
				}
				voicesElem.appendChild(voiceElem);
			}
		}
		DomRepresentation dom; {
			try {
				dom = new DomRepresentation(MediaType.APPLICATION_XML, voicesDoc);
				setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				setStatus(Status.SERVER_ERROR_INTERNAL);
				return getErrorRepresentation(e.getMessage());
			}
		}
		logResponse(dom);
		return dom;
	}
}
