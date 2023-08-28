package org.daisy.pipeline.webservice.restlet.impl;

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

// TODO: Auto-generated Javadoc
/**
 * The Class ScriptResource.
 */
public class ScriptResource extends AuthenticatedResource {
	/** The script. */
	private Script script = null;
	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(ScriptResource.class.getName());

	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#doInit()
	 */
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}

		String scriptId = null;
		scriptId = (String) getRequestAttributes().get("id");

		logger.debug("Script with id :"+scriptId);
		ScriptService<?> scriptService = getScriptRegistry().getScript(scriptId);

		if (scriptService != null) {
			script = scriptService.load();
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

		if (script == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Script not found");
		}
		setStatus(Status.SUCCESS_OK);
		ScriptXmlWriter writer = new ScriptXmlWriter(script, getRequest().getRootRef().toString());
		DomRepresentation dom = new DomRepresentation(
				MediaType.APPLICATION_XML,
				writer.withDetails().getXmlDocument());
		logResponse(dom);
		return dom;
	}
}