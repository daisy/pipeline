package org.daisy.pipeline.webservice.restlet.impl;

import org.daisy.pipeline.webservice.restlet.AdminResource;

import org.restlet.data.Status;
import org.restlet.resource.Get;

public class HaltResource extends AdminResource {
	private long key;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthorized()) {
    		setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    		return;
    	}
		try {
			key =  Long.parseLong((String) getRequestAttributes().get("key"));
		}
		catch(NumberFormatException e) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			    return;
		}
	}
	@Get
	public void getResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthorized()) {
    		setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
    		return;
    	}
		try {
			if (!shutDown(key)) {
				setStatus(Status.CLIENT_ERROR_FORBIDDEN);
				return;
			}
		} catch (Exception e) {
			setStatus(Status.CONNECTOR_ERROR_INTERNAL);
			return;
		}
		setStatus(Status.SUCCESS_NO_CONTENT);
	}
}