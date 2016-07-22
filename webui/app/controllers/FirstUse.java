package controllers;

import java.util.Enumeration;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;

import play.mvc.*;
import play.data.*;
import play.Logger;
import utils.FormHelper;
import models.*;

/**
 * Helps with configuring the Web UI for the first time.
 * 
 * Configure database -> configure administrative account -> set webservice endpoint -> set upload directory -> welcome page!
 * 
 * @author jostein
 */
public class FirstUse extends Controller {
	
	/**
	 * GET /firstuse
	 * @return
	 */
	public static Result getFirstUse() {
		
		User user = null;
		
		if (isFirstUse()) {
			// first determine domain/ip/port
			
			if (Setting.get("absoluteURL") == null) {
				URL whatismyip;
				String protocol = "http";
				String ip = null;
				String dns = null;
				int port = 80;
				
				try {
					whatismyip = new URL("http://checkip.amazonaws.com");
					BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
					ip = in.readLine();
				} catch (Exception e) {}
				
				Enumeration<NetworkInterface> interfaces;
				try {
					interfaces = NetworkInterface.getNetworkInterfaces();
					while (interfaces.hasMoreElements()) {
						NetworkInterface iface = interfaces.nextElement();
						try {
							if(iface.isUp()) {
								Enumeration<InetAddress> addresses = iface.getInetAddresses();
								while (addresses.hasMoreElements()) {
									InetAddress address = addresses.nextElement();
									if (!address.isLinkLocalAddress() && !address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
										if (ip == null || ip.equals(address.getHostAddress())) {
											dns = address.getCanonicalHostName();
											break;
										}
									}
								}
								if (dns != null) {
									break;
								}
							}
						} catch (SocketException e) {
							Logger.error("Unable to get information about network interface.", e);
						}
					}
				} catch (SocketException e) {
					Logger.error("Unable to get information about network interfaces.", e);
				}
				
				try {
					URL requestUrl = new URL(routes.FirstUse.welcome().absoluteURL(request()));
					String requestHostname = requestUrl.getHost();
					assert(requestHostname != null);
					if ((ip == null && dns == null) || requestHostname.equals(ip) || requestHostname.equals(dns)) {
						// Use port and protocol based on the HTTP request. If the request hostname equals the external
						// ip then the Web UI is probably being configured from a different computer,
						// which lets us determine the port and protocol (https vs http) that
						// the Web UI is exposed with. We also need to fall back to this method if the ip could
						// not be determined by looking at the network interfaces, which most likely means that
						// the Web UI is being configured from localhost on a computer without internet access.
						port = requestUrl.getPort();
						protocol = requestUrl.getProtocol();
					}
					
				} catch (MalformedURLException e) {
					Logger.error("Unable to parse absolute URL to Web UI, setting to defaults...", e);
					if (ip == null && dns == null) {
						ip = "localhost";
						dns = "localhost";
						port = 9000;
					}
				}
				
				String absoluteURL = protocol+"://"+(dns == null ? ""+ip : dns)+(port == 80 ? "" : ":"+port);
				Setting.set("absoluteURL", absoluteURL);
			}
			
			// Set default storage directories 
			File uploads = new File(new File(controllers.Application.DP2DATA), "uploads");
			File jobStorage = new File(new File(controllers.Application.DP2DATA), "jobs");
			File templateStorage = new File(new File(controllers.Application.DP2DATA), "templates");
			
			if (Setting.get("uploads") == null) {
				uploads.mkdirs();
				try {
					Setting.set("uploads", uploads.getCanonicalPath());
				} catch (IOException e) {
					Setting.set("uploads", uploads.getAbsolutePath());
				}
			}

			if (Setting.get("jobs") == null) {
				jobStorage.mkdirs();
				try {
					Setting.set("jobs", jobStorage.getCanonicalPath());
				} catch (IOException e) {
					Setting.set("jobs", jobStorage.getAbsolutePath());
				}
			}

			if (Setting.get("templates") == null) {
				templateStorage.mkdirs();
				try {
					Setting.set("templates", templateStorage.getCanonicalPath());
				} catch (IOException e) {
					Setting.set("templates", templateStorage.getAbsolutePath());
				}
			}
			
			// set default WS endpoint
			if (Setting.get("dp2ws.endpoint") == null) {
				Setting.set("dp2ws.endpoint", Application.DEFAULT_DP2_ENDPOINT);
			}
			
			// if no admin user; display "create admin" form
			if (User.find.where().eq("admin", true).findRowCount() == 0) {
				return ok(views.html.FirstUse.createAdmin.render(play.data.Form.form(Administrator.CreateAdminForm.class)));
			}
			
		}
		
		user = User.authenticate(request(), session());
		if (user == null || !user.isAdmin()) {
			return redirect(routes.Login.login());
		}

		return redirect(routes.FirstUse.welcome());
	}
	
	public static Result welcome() {
		if (FirstUse.isFirstUse())
			return redirect(routes.FirstUse.getFirstUse());

		User user = User.authenticate(request(), session());
		if (user == null)
			return redirect(routes.Login.login());

		return ok(views.html.FirstUse.welcome.render());
	}
	
	public static Result postFirstUse() {
		Map<String, String[]> query = request().queryString();
		Map<String, String[]> form = request().body().asFormUrlEncoded();
		
		String formName = form.containsKey("formName") ? form.get("formName")[0] : "";
		
		if ("createAdmin".equals(formName)) {
			if (!isFirstUse())
				return redirect(routes.FirstUse.getFirstUse());
			
			Form<Administrator.CreateAdminForm> filledForm = play.data.Form.form(Administrator.CreateAdminForm.class).bindFromRequest();
			Administrator.CreateAdminForm.validate(filledForm);
			
			if (query.containsKey("validate")) {
				return ok(FormHelper.asJson(filledForm,new String[]{"password","repeatPassword"}));
			
			} else if (filledForm.hasErrors()) {
				return badRequest(views.html.FirstUse.createAdmin.render(filledForm));
			
			} else {
				User admin = new User(filledForm.field("email").valueOr(""), "Administrator", filledForm.field("password").valueOr(""), true);
				admin.save();
				admin.login(session());
				
				// Set some default configuration options
				Setting.set("users.guest.name", "Guest");
				Setting.set("users.guest.allowGuests", "false");
				Setting.set("users.guest.showGuestName", "true");
				Setting.set("users.guest.showEmailBox", "true");
				Setting.set("users.guest.shareJobs", "false");
				Setting.set("users.guest.automaticLogin", "false");
				Setting.set("mail.enable", "false");
				
				return redirect(routes.FirstUse.getFirstUse());
			}
		}
		
		User user = User.authenticate(request(), session());
		if (user == null || !user.isAdmin()) {
			return redirect(routes.Login.login());
		}
		
		return getFirstUse();
	}
	
	/**
	 * Returns true if this is the first time that the Web UI are used (i.e. there are no registered users).
	 * @return
	 */
	public static boolean isFirstUse() {
		return User.findAll().size() == 0 || Setting.get("dp2ws.endpoint") == null || Setting.get("uploads") == null || Setting.get("jobs") == null || Setting.get("templates") == null;
	}
	
}
