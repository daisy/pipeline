package org.daisy.pipeline.tts.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.IllformedLocaleException;
import java.util.Locale;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
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

public class VoicesResource extends AuthenticatedResource {

	private static final Logger logger = LoggerFactory.getLogger(VoicesResource.class.getName());

	private VoicesWebServiceExtension provider;
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
		provider = (VoicesWebServiceExtension)getContext().getAttributes().get(VoicesWebServiceExtension.CONTEXT_ATTRIBUTE_KEY);
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
		XdmNode configXML = null; {
			if (ttsConfig != null)
				try {
					configXML = provider.getConfigXML(ttsConfig.getText());
				} catch (IOException|SaxonApiException e) {
					logger.error("bad request:", e);
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation(e.getMessage());
				}
		}
		VoiceManager voiceManager = null;
		boolean omitRefs = false; {
			synchronized (provider) {
				voiceManager = provider.getVoiceManager(Properties.getSnapshot(), configXML, logger);
				omitRefs = !provider.hasRememberedVoiceManager();
			}
		}
		Iterable<Voice> availableVoices; {
			Locale lang; {
				try {
					lang = langAttr == null ? null : (new Locale.Builder()).setLanguageTag(langAttr).build();
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
				} catch (IllformedLocaleException e) {
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
				VoiceResource.addElementData(voiceElem, baseURL, v, omitRefs);
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
