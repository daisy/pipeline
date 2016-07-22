package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.daisy.pipeline.client.http.WS;
import org.daisy.pipeline.client.http.WSInterface;
import org.daisy.pipeline.client.utils.Files;

import controllers.Assets.Asset;
import models.Notification;
import models.NotificationConnection;
import models.Setting;
import models.User;
import play.*;
import play.mvc.*;

public class Application extends Controller {
	
	public static final boolean debug = "DEBUG".equals(Configuration.root().getString("logger.application"));
	
	public static final String DEFAULT_DP2_ENDPOINT = "http://localhost:8181/ws";
	public static final String DP2DATA;
	public static final String DP2DATA_ENGINE;
	static {
		String os = System.getProperty("os.name");
		String home = System.getProperty("user.home");
		
		// get data directory for webui
		String dp2data = System.getenv("DP2DATA");
		String dp2dataEngine = System.getenv("DP2DATA_ENGINE");
		try {
			if (dp2data == null || "".equals(dp2data)) {
				File dp2dataDir = null;
				File dp2dataEngineDir = null;
				if (os.startsWith("Windows")) {
					dp2dataDir = new File(new File(System.getenv("APPDATA")), "DAISY Pipeline 2 Web UI");
					dp2dataEngineDir = new File(new File(System.getenv("APPDATA")), "DAISY Pipeline 2");
					
				} else if (os.startsWith("Mac OS X")) {
					dp2dataDir = new File(home + "/Library/Application Support/DAISY Pipeline 2 Web UI");
					dp2dataEngineDir = new File(home + "/Library/Application Support/DAISY Pipeline 2");
					
				} else { // Linux etc.
					// will be /var/opt/daisy-pipeline-webui when installed as a deb/rpm, set through DP2DATA env. variable
					dp2dataDir = new File(home + "/.daisy-pipeline/webui");
					dp2dataEngineDir = new File(home + "/.daisy-pipeline");
				}
				if (dp2dataDir.exists()) {
					dp2dataDir.delete();
				}
				dp2dataDir.mkdirs();
				dp2data = dp2dataDir.getCanonicalPath();
				dp2dataEngine = dp2dataEngineDir.getCanonicalPath();
			}
			
		} catch (IOException e) {
			Logger.error("Could not get canonical path for "+dp2data, e);
		}
		DP2DATA = dp2data;
		DP2DATA_ENGINE = dp2dataEngine;
	}
	
	public static WSInterface ws = new WS();
	
	private static org.daisy.pipeline.client.models.Alive alive = null;
	
	public static final String version;
	static {
		File versionFile = Play.application().getFile("conf/version.properties");
		if (versionFile == null || !versionFile.isFile()) {
			version = "dev";
		} else {
			Properties versionProperties = new Properties();
			try {
				versionProperties.load(new FileInputStream(versionFile));
			} catch (IOException e) {
				Logger.error("Unable to read version.properties", e);
			}
			String v = versionProperties.getProperty("version");
			if (v == null) {
				version = "dev";
			} else {
				version = v;
			}
		}
	}
	
	public static Result index() {
		if (FirstUse.isFirstUse())
    		return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());
		
		String landingPage = Setting.get("appearance.landingPage");
		if ("welcome".equals(landingPage)) return redirect(routes.FirstUse.welcome());
		if ("scripts".equals(landingPage)) return redirect(routes.Jobs.newJob());
		if ("jobs".equals(landingPage) && !(user.getId() <= -2 && !"true".equals(Setting.get("users.guest.shareJobs")))) return redirect(routes.Jobs.getJobs());
		if ("about".equals(landingPage)) return redirect(routes.Application.about());
		if ("admin".equals(landingPage) && user.isAdmin()) return redirect(routes.Administrator.getSettings());
		if ("account".equals(landingPage) && user.getId() >= 0) return redirect(routes.Account.overview());
		
		return redirect(routes.Jobs.newJob());
	}
	
	public static Result about() {
		if (FirstUse.isFirstUse())
    		return redirect(routes.FirstUse.getFirstUse());
		
		File about = new File(new File(DP2DATA, "conf"), "about.html");
		if (about.exists()) {
			return ok(views.html.about.render(Files.read(about)));
		} else {
			return ok(views.html.about.render(null));
		}
	}
	
	public static Result theme(String filename) {
		if ("".equals(themeName())) {
			return redirect(routes.Assets.versioned(new Asset(filename)));
			
		} else {
			String theme = Application.themeName();
			File file = new File("themes/"+theme+"/"+filename);
			if (file.exists()) {
				try {
					if (filename.endsWith("css"))
						response().setContentType("text/css");
					else if (filename.endsWith("png"))
						response().setContentType("image/png");
					else if (filename.endsWith("jpg") || filename.endsWith("jpeg"))
						response().setContentType("image/jpeg");
					else if (filename.endsWith("gif"))
						response().setContentType("image/gif");
					else if (filename.endsWith("js"))
						response().setContentType("application/javascript");
					
					return ok(new FileInputStream(file));
					
				} catch (FileNotFoundException e) {
					Logger.error("Could not open file input stream for '"+filename+"' in theme '"+theme+"'.", e);
					return redirect(routes.Assets.versioned(new Asset(filename)));
				}
			} else {
				return redirect(routes.Assets.versioned(new Asset(filename)));
			}
		}
	}
	
	public static Result error(int status, String name, String description, String message) {
		return status(status, views.html.error.render(status, name, description, message));
	}
	
	public static Result redirect(String path, String file) {
		return movedPermanently(path+file);
	}
	
	public static String themeName = null;

	public static String themeName() {
		if (themeName == null)
			themeName = Setting.get("appearance.theme");
		return themeName;
	}
	
	public static String titleLink() {
		String titleLink = Setting.get("appearance.titleLink");
		if ("welcome".equals(titleLink)) titleLink = routes.FirstUse.welcome().toString();
		else if ("scripts".equals(titleLink)) titleLink = routes.Jobs.newJob().toString();
		else if ("jobs".equals(titleLink)) titleLink = routes.Jobs.getJobs().toString();
		else if ("about".equals(titleLink)) titleLink = routes.Application.about().toString();
		else if ("admin".equals(titleLink)) titleLink = routes.Administrator.getSettings().toString();
		else if ("account".equals(titleLink)) titleLink = routes.Account.overview().toString();
		return titleLink;
	}
	
	public static String absoluteURL(String url) {
		String absoluteURL = Setting.get("absoluteURL"); // for instance "http://localhost:9000" (protocol+host)
		if (absoluteURL == null) {
			return null;
		}
		
		if (url.matches("[^/]+:/.*")) {
			// absolute
			url = url.replaceFirst("[^/]+:/+[^/]+", absoluteURL);
			return url;
			
		} else {
			// relative
			if (!url.startsWith("/")) {
				absoluteURL += "/";
			}
			return absoluteURL+url;
		}
	}

	public static boolean pipeline2EngineAvailable() {
		return Application.alive != null && !Application.alive.error;
	}
	
	public static org.daisy.pipeline.client.models.Alive getAlive() {
		return alive;
	}
	
	public static void setAlive(org.daisy.pipeline.client.models.Alive alive) {
		Application.alive = alive;
		NotificationConnection.pushAll(new Notification("dp2.engine", alive));
	}
}
