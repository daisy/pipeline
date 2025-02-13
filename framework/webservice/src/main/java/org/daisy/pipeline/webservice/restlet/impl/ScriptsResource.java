package org.daisy.pipeline.webservice.restlet.impl;

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

// TODO: Auto-generated Javadoc
/**
 * The Class ScriptsResource.
 */
public class ScriptsResource extends AuthenticatedResource {

	private static Logger logger = LoggerFactory.getLogger(ScriptsResource.class.getName());

	List<Script> scripts = null;

	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#doInit()
	 */
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
		ScriptRegistry scriptRegistry = getScriptRegistry();
		scripts = new ArrayList<Script>();
		for (ScriptService<?> script : scriptRegistry.getScripts()) {
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
		
		this.setStatus(Status.SUCCESS_OK);
		ScriptsXmlWriter writer = new ScriptsXmlWriter(scripts, getRequest().getRootRef().toString());
		DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML,
				writer.getXmlDocument());
		logResponse(dom);
		return dom;

	}
}