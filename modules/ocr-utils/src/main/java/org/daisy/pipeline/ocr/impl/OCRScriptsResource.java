package org.daisy.pipeline.ocr.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.xml.ScriptsXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCRScriptsResource extends AuthenticatedResource {

	private static final Logger logger = LoggerFactory.getLogger(OCRScriptsResource.class.getName());

	static final String OCR_SCRIPTS_KEY = "ocr-scripts";

	private List<Script> scripts;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated())
			return;
		OCRScriptsWebServiceExtension registry
			= (OCRScriptsWebServiceExtension)getContext().getAttributes().get(OCR_SCRIPTS_KEY);
		scripts = new ArrayList<>();
		for (ScriptService<?> script : registry.getScriptRegistry(getClient()).getScripts()) {
			try {
				scripts.add(script.load());
			} catch (Throwable e) {
				// skip script instead of failing to respond to request
				logger.debug("Failed to load script", e);
			}
		}
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
		DomRepresentation dom; {
			try {
				ScriptsXmlWriter writer = new ScriptsXmlWriter(
					scripts, getRequest().getRootRef().toString(), OCRScriptsWebServiceExtension.OCR_SCRIPTS_ROUTE);
				dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.getXmlDocument());
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
