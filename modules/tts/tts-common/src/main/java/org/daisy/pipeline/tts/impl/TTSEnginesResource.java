package org.daisy.pipeline.tts.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.config.DynamicPropertiesExtension;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.ServiceDisabledException;
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

public class TTSEnginesResource extends AuthenticatedResource {

	static final String TTS_REGISTRY_KEY = "tts-registry";

	private static final Logger logger = LoggerFactory.getLogger(TTSEnginesResource.class.getName());
	private static final Processor saxonProcessor = new Processor(false);

	private TTSRegistry ttsRegistry;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
		ttsRegistry = (TTSRegistry)getContext().getAttributes().get(TTS_REGISTRY_KEY);
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
		Collection<TTSService> ttsServices = ttsRegistry.getServices();
		Map<String,String> properties; {
			DynamicPropertiesExtension propsExt = new DynamicPropertiesExtension();
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
			properties = Properties.getSnapshot();
			new ConfigReader(saxonProcessor, configXML, properties, propsExt);
			if (configXML != null)
				logger.debug("TTS configuration XML:\n" + configXML);
			Map<String,String> dynProperties = propsExt.getDynamicProperties();
			if (dynProperties != null && !dynProperties.isEmpty()) {
				properties = new HashMap<>(properties);
				properties.putAll(dynProperties);
			}
		}
		Document enginesDoc; {
			enginesDoc = XmlUtils.createDom("tts-engines");
			Element enginesElem = enginesDoc.getDocumentElement();
			String baseURL = getRequest().getRootRef().toString();
			enginesElem.setAttribute("href", baseURL + TTSEnginesWebServiceExtension.TTS_ENGINES_ROUTE);
			for (TTSService s : ttsServices) {
				Element engineElem = enginesDoc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "tts-engine");
				String name = s.getName();
				engineElem.setAttribute("name", name);
				engineElem.setAttribute("nicename", s.getDisplayName());
				TTSEngine e = null;
				Throwable error = null;
				try {
				    e = s.newEngine(properties);
					engineElem.setAttribute("status", "available");
				} catch (ServiceDisabledException ex) {
					logger.debug(name + " is disabled", ex);
					error = ex;
					engineElem.setAttribute("status", "disabled");
				} catch (Throwable ex) {
					logger.debug(name + " could not be activated", ex);
					error = ex;
					engineElem.setAttribute("status", "error");
				}
				if (e != null) {
					engineElem.setAttribute("voices", baseURL + VoicesWebServiceExtension.VOICES_ROUTE + "?engine=" + name);
					List<String> features = new ArrayList<>();
					if (e.handlesSpeakingRate())
						features.add("speech-rate");
					if (e.handlesPronunciation())
						features.add("phoneme");
					if (!features.isEmpty())
						engineElem.setAttribute("features", String.join(" ", features));
				} else if (error != null) {
					// Clients should use first line as the short message. The short message is
					// followed by the full message after a blank line.
					String shortMessage = error.getMessage();
					error = error.getCause();
					String detailedMessage = error != null ? error.getMessage() : null;
					if (shortMessage.length() > 80) {
						// Use the heuristic that if a message is longer than 80 characters, it is possible
						// that it is too technical for the average user. It also becomes difficult to fit it
						// in the UI. So provide this backup:
						if (detailedMessage == null)
							detailedMessage = shortMessage;
						else {
							detailedMessage = detailedMessage.trim();
							shortMessage = shortMessage.trim();
							if (detailedMessage.startsWith(shortMessage))
								detailedMessage = detailedMessage.substring(shortMessage.length());
							if (detailedMessage.startsWith(":")) {
								detailedMessage = detailedMessage.substring(1);
								detailedMessage = detailedMessage.trim();
							}
							if (detailedMessage.length() > 0) {
								if (!shortMessage.matches(".*\\p{Punct}$"))
									shortMessage += ":";
								detailedMessage = shortMessage + " " + detailedMessage;
							} else
								detailedMessage = shortMessage;
						}
						shortMessage = "Could not connect to the service";
					}
					String message = shortMessage;
					if (detailedMessage != null)
						message += ("\n\n" + detailedMessage);
					engineElem.setAttribute("message", message);
				}
				enginesElem.appendChild(engineElem);
			}
		}
		DomRepresentation dom; {
			try {
				dom = new DomRepresentation(MediaType.APPLICATION_XML, enginesDoc);
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
