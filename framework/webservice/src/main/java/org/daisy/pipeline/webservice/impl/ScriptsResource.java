package org.daisy.pipeline.webservice.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;
import org.daisy.pipeline.webservice.xml.ScriptsXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

// TODO: Auto-generated Javadoc
/**
 * The Class ScriptsResource.
 */
public class ScriptsResource extends AuthenticatedResource {
	/** The scripts. */
	List<XProcScript> scripts = null;

	/* (non-Javadoc)
	 * @see org.restlet.resource.Resource#doInit()
	 */
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
		ScriptRegistry scriptRegistry = webservice().getScriptRegistry();
		Iterable<XProcScriptService> unfilteredScripts = scriptRegistry.getScripts();
		Iterator<XProcScriptService> it = unfilteredScripts.iterator();
		scripts = new ArrayList<XProcScript>();
		while (it.hasNext()) {
			scripts.add(it.next().load());
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
