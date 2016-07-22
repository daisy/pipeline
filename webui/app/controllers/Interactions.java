package controllers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

public class Interactions extends Controller {

	public static Result interactions() {
		if (FirstUse.isFirstUse()) {
			return badRequest();
		}

		User user = User.authenticate(request(), session());
		if (user == null) {
			return forbidden();
		}
		
		// Generate a unique id
		String uuid=session("uuid");
		if (uuid==null) {
		    uuid=java.util.UUID.randomUUID().toString();
		    session("uuid", uuid);
		}
		
		File logFile = new File(new File(new File(new File(controllers.Application.DP2DATA), "logs"), "interactions"), uuid);
		logFile.getParentFile().mkdirs();
		
		String interactions = request().body().asJson()+"\n";
		try {
			logFile.createNewFile();
		    Files.write(logFile.toPath(), interactions.getBytes(), StandardOpenOption.APPEND);
		} catch (Exception e) {
		    // ignore; too bad
			Logger.warn("Unable to log interactions to: "+logFile, e);
			Logger.warn("interactions: "+interactions);
		}
		
		return ok();
	}
	
}
