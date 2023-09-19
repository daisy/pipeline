package org.daisy.pipeline.tts.impl;

import java.util.Collections;
import java.util.Locale;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class VoicesResource extends AuthenticatedResource {

	static final String TTS_REGISTRY_KEY = "tts-registry";

	private static final Logger logger = LoggerFactory.getLogger(VoicesResource.class.getName());

	private VoiceManager voiceManager;
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
		TTSRegistry ttsRegistry = (TTSRegistry)getContext().getAttributes().get(TTS_REGISTRY_KEY);
		engineAttr = getQuery().getFirstValue("engine");
		nameAttr = getQuery().getFirstValue("name");
		langAttr = getQuery().getFirstValue("lang");
		genderAttr = getQuery().getFirstValue("gender");
		voiceManager = new VoiceManager(
			ttsRegistry.getWorkingEngines(Collections.EMPTY_MAP, null, null),
			Collections.EMPTY_LIST);
	}

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Get("xml")
	public Representation getResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
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
