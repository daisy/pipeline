package controllers;

import play.Logger;
import play.mvc.*;

public class Callbacks extends Controller {
	
	/**
	 * POST /callbacks/{type}
	 * Handles callbacks from the Pipeline 2 engine.
	 * @return
	 */
	public static Result postCallback(String type) {
		Logger.debug("Received callback of type '"+type+"': "+request().body().asText());
//		Logger.debug("Received callback of type '"+type+"': "+utils.XML.toString(request().body().asXml()));
		
		return ok();
	}
	
}
