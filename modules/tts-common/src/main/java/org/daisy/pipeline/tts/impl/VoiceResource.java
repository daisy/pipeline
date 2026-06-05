package org.daisy.pipeline.tts.impl;

import java.util.Collection;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.LanguageRange;
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

public class VoiceResource extends AuthenticatedResource {

	private static final Logger logger = LoggerFactory.getLogger(VoiceResource.class.getName());

	private VoicesWebServiceExtension provider;
	private String voiceID;

	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
		provider = (VoicesWebServiceExtension)getContext().getAttributes().get(VoicesWebServiceExtension.CONTEXT_ATTRIBUTE_KEY);
		voiceID = (String)getRequestAttributes().get("id");
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
		VoiceManager voiceManager = provider.getRememberedVoiceManager(Properties.getSnapshot());
		if (voiceManager == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation("No /voices call preceeded this call, or settings were changed");
		}
		Voice voice = voiceManager.getVoiceForID(voiceID);
		if (voice == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return getErrorRepresentation("Voice not found");
		}
		Document voiceDoc; {
			voiceDoc = XmlUtils.createDom("voice");
			Element voiceElem = voiceDoc.getDocumentElement();
			addElementData(voiceElem, getRequest().getRootRef().toString(), voice, false);
		}
		DomRepresentation dom; {
			try {
				dom = new DomRepresentation(MediaType.APPLICATION_XML, voiceDoc);
				setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				setStatus(Status.SERVER_ERROR_INTERNAL);
				return getErrorRepresentation(e.getMessage());
			}
		}
		logResponse(dom);
		return dom;
	}

	static void addElementData(Element voiceElem, String baseURL, Voice voice, boolean omitRefs) {
		if (!omitRefs)
			voiceElem.setAttribute("href", baseURL + VoicesWebServiceExtension.VOICE_ROUTE.replaceFirst("\\{id\\}", voice.getID()));
		voiceElem.setAttribute("name", voice.getName());
		voiceElem.setAttribute("engine", voice.getEngine());
		Collection<LanguageRange> locale = voice.getLocale();
		if (!locale.isEmpty()) {
			voiceElem.setAttribute("lang", LanguageRange.toString(voice.getLocale()));
		}
		if (voice.getGender().isPresent()) {
			voiceElem.setAttribute("gender", voice.getGender().get().toString());
		}
		if (!omitRefs)
		voiceElem.setAttribute(
			"preview", baseURL + VoicesWebServiceExtension.VOICE_PREVIEW_ROUTE.replaceFirst("\\{id\\}", voice.getID()));
	}
}
