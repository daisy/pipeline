package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;
import org.daisy.pipeline.webservice.xml.ScriptXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlWriterFactory;

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
	private XProcScript script = null;
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
		ScriptRegistry scriptRegistry = webservice().getScriptRegistry();
		XProcScriptService unfilteredScript = scriptRegistry
				.getScript(scriptId);

		if (unfilteredScript != null) {
			script = unfilteredScript.load();
			script = XProcScriptFilter.withoutOutputs(script);
			script = XProcScriptFilter.renameOptions(script);
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
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}

		if (script == null) {
			setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return this.getErrorRepresentation("Script not found");
		}
		setStatus(Status.SUCCESS_OK);
		ScriptXmlWriter writer = XmlWriterFactory.createXmlWriterForScript(script);
		DomRepresentation dom = new DomRepresentation(
				MediaType.APPLICATION_XML,
				writer.withDetails().getXmlDocument());
		logResponse(dom);
		return dom;
	}
}
