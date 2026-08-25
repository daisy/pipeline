package org.daisy.pipeline.ocr.impl;

import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.xml.ScriptXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCRScriptResource extends AuthenticatedResource {

	private static Logger logger = LoggerFactory.getLogger(OCRScriptResource.class.getName());

	private Script script = null;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated())
			return;
		OCRScriptsWebServiceExtension registry
			= (OCRScriptsWebServiceExtension)getContext().getAttributes().get(OCRScriptsResource.OCR_SCRIPTS_KEY);
		String scriptId = (String)getRequestAttributes().get("id");
		logger.debug("Script with id: " + scriptId);
		ScriptService<?> scriptService = registry.getScriptRegistry(getClient()).getScript(scriptId);
		if (scriptService != null)
			script = scriptService.load();
	}

	@Get("xml")
	public Representation getResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		if (script == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return getErrorRepresentation("Script not found");
		}
		DomRepresentation dom; {
			try {
				ScriptXmlWriter writer = new ScriptXmlWriter(
					script, getRequest().getRootRef().toString(), OCRScriptsWebServiceExtension.OCR_SCRIPT_ROUTE);
				dom = new DomRepresentation(MediaType.APPLICATION_XML, writer.withDetails().getXmlDocument());
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
