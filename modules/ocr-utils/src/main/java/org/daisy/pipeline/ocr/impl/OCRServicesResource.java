package org.daisy.pipeline.ocr.impl;

import java.util.Collection;
import java.util.Map;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.ocr.OCRService;
import org.daisy.pipeline.ocr.OCRService.ServiceDisabledException;
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

public class OCRServicesResource extends AuthenticatedResource {

	private static final Logger logger = LoggerFactory.getLogger(OCRServicesResource.class.getName());

	static final String OCR_SERVICES_KEY = "ocr-services";

	private Collection<OCRService> services;

	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
		services = (Collection<OCRService>)getContext().getAttributes().get(OCR_SERVICES_KEY);
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
		Document servicesDoc; {
			servicesDoc = XmlUtils.createDom("ocr-services");
			Element servicesElem = servicesDoc.getDocumentElement();
			String baseURL = getRequest().getRootRef().toString();
			servicesElem.setAttribute("href", baseURL + OCRServicesWebServiceExtension.OCR_SERVICES_ROUTE);
			for (OCRService s : services) {
				Element serviceElem = servicesDoc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "ocr-service");
				String name = s.getName();
				serviceElem.setAttribute("name", name);
				serviceElem.setAttribute("nicename", s.getDisplayName());
				Throwable error = null;
				Map<String,String> properties = Properties.getSnapshot();
				try {
					Property enabled = Properties.getProperty("org.daisy.pipeline.ocr." + name + ".enabled",
					                                          true,
					                                          "Enable " + s.getDisplayName(),
					                                          false,
					                                          "true");
					String str = enabled.getValue(properties);
					if (str != null && "false".equals(str.toLowerCase()) || "0".equals(str))
						throw new ServiceDisabledException(
							"In order to enable the " + name + " service, set property '" + enabled.getName() + "' to 'true'");
					if (s.getAvailableProcessors(properties).isEmpty())
						throw new Exception("No processors available");
					serviceElem.setAttribute("status", "available");
				} catch (ServiceDisabledException ex) {
					logger.debug(name + " is disabled", ex);
					error = ex;
					serviceElem.setAttribute("status", "disabled");
				} catch (Throwable ex) {
					logger.debug(name + " could not be activated", ex);
					error = ex;
					serviceElem.setAttribute("status", "error");
				}
				if (error != null) {
					// Clients should use first line as the short message. The short message is
					// followed by the full message after a blank line.
					String shortMessage = error.getMessage();
					error = error.getCause();
					String detailedMessage = error != null ? error.getMessage() : null;
					if (detailedMessage != null && shortMessage.endsWith(detailedMessage))
						detailedMessage = null;
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
					serviceElem.setAttribute("message", message);
				}
				servicesElem.appendChild(serviceElem);
			}
		}
		DomRepresentation dom; {
			try {
				dom = new DomRepresentation(MediaType.APPLICATION_XML, servicesDoc);
				setStatus(Status.SUCCESS_OK);
			} catch (Exception e) {
				setStatus(Status.SERVER_ERROR_INTERNAL);
				return getErrorRepresentation(e);
			}
		}
		logResponse(dom);
		return dom;
	}
}
