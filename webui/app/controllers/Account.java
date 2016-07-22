package controllers;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import models.Setting;
import models.User;
import models.User.UserSetPassword;
import play.Logger;
import play.data.Form;
import play.mvc.*;

public class Account extends Controller {
	
	final static Form<User> editDetailsForm = play.data.Form.form(User.class);
	final static Form<UserSetPassword> resetPasswordForm = play.data.Form.form(UserSetPassword.class);
	final static Form<UserSetPassword> activateAccountForm = play.data.Form.form(UserSetPassword.class);
	
	/**
	 * GET /account
	 * Show information about the user, and a form letting the user change their details.
	 * @return
	 */
	public static Result overview() {
		if (FirstUse.isFirstUse())
    		return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null || user.getId() < 0)
			return redirect(routes.Login.login());
    	
		return ok(views.html.Account.overview.render(play.data.Form.form(User.class)));
	}
	
	/**
	 * POST /account
	 * Called when the GET /account form is submitted.
	 * @return
	 */
	public static Result changeDetails() {
		if (FirstUse.isFirstUse())
    		return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.authenticate(request(), session());
		if (user == null || user.getId() < 0)
			return redirect(routes.Login.login());
		
		Form<User> filledForm = editDetailsForm.bindFromRequest();
		
		boolean changedName = false;
		boolean changedEmail = false;
		boolean changedPassword = false;
		
		if (!user.getName().equals(filledForm.field("name").valueOr(""))) {
			// Changed name
			changedName = true;
		}
		
		if (!user.getEmail().equals(filledForm.field("email").valueOr(""))) {
			// Changed email
			changedEmail = true;
			
			if (User.findByEmail(filledForm.field("email").valueOr("")) != null)
	            filledForm.reject("email", "That e-mail address is already taken");
		}
    	
		if (!filledForm.field("newPassword").valueOr("").equals("")) {
			// Changed password
			changedPassword = true;
			
			if (filledForm.field("newPassword") != null && filledForm.field("newPassword").valueOr("").length() < 6) {
				filledForm.reject("newPassword", "The password must be at least 6 characters long.");
			
			} else if (!filledForm.field("newPassword").valueOr("").equals(filledForm.field("repeatPassword").valueOr(""))) {
				filledForm.reject("repeatPassword", "Passwords don't match.");
			
			} else if (filledForm.field("password").valueOr("").equals("")) {
				filledForm.reject("password", "You must enter your existing password, just so that we're extra sure that you are you.");
				
			} else {
				User oldUser = User.authenticateUnencrypted(user.getEmail(), filledForm.field("password").valueOr(""), session());
				if (oldUser == null)
					filledForm.reject("password", "The password you entered is wrong, please correct it and try again.");
			}
			
		} else if (filledForm.errors().containsKey("password")) {
			filledForm.errors().get("password").clear(); // No need to check the old password if the user isn't trying to set a new password
		}
		
		if (!changedName && !changedEmail && !changedPassword) {
			flash("success", "You did not submit any changes. No changes were made.");
			return redirect(routes.Account.overview());
			
		} else if (filledForm.hasErrors()) {
        	return badRequest(views.html.Account.overview.render(filledForm));
        	
        } else {
        	if (changedName)
        		user.setName(filledForm.field("name").valueOr(""));
        	if (changedEmail)
        		user.setEmail(filledForm.field("email").valueOr(""));
        	if (changedPassword)
        		user.setPassword(filledForm.field("newPassword").valueOr(""));
        	user.save();
        	user.login(session());
        	flash("success", "Your changes were saved successfully!");
        	return redirect(routes.Account.overview());
        }
	}
	
	/**
	 * GET /account/resetpassword
	 * 
	 * Show a form letting the user set a password without having one already.
	 * 
	 * @param email
	 * @param resetUid
	 * @return
	 */
	public static Result showResetPasswordForm(String email, String resetUid) {
		if (FirstUse.isFirstUse())
    		return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.findByEmail(email);
		if (user == null || user.getId() < 0)
			return redirect(routes.Login.login());
		
		if (resetUid == null || !resetUid.equals(user.getActivationUid())) {
			return forbidden();
		}
		
		return ok(views.html.Account.resetPassword.render(play.data.Form.form(UserSetPassword.class), email, resetUid, user.isActive()));
	}
	
	/**
	 * POST /account/resetpassword
	 * 
	 * Called when a user tries to set a password through the GET /account/resetpassword form.
	 * 
	 * @param email
	 * @param resetUid
	 * @return
	 */
	public static Result resetPassword(String email, String resetUid) {
		if (FirstUse.isFirstUse())
    		return redirect(routes.FirstUse.getFirstUse());
		
		User user = User.findByEmail(email);
		if (user == null || user.getId() < 0)
			return redirect(routes.Login.login());
		
		if (resetUid == null || !resetUid.equals(user.getActivationUid()))
			return redirect(routes.Login.login());
		
		Form<UserSetPassword> filledForm = resetPasswordForm.bindFromRequest();
		
		// @Constraints.MinLength(6) doesn't seem to work for some reason, so checking it manually instead
		if (filledForm.field("password").valueOr("").length() < 6)
			filledForm.reject("password", "The password must be at least 6 characters long.");
		
		if (!filledForm.field("password").valueOr("").equals("") && !filledForm.field("password").valueOr("").equals(filledForm.field("repeatPassword").value()))
    		filledForm.reject("repeatPassword", "Password doesn't match.");
        
        if (filledForm.hasErrors()) {
        	return badRequest(views.html.Account.resetPassword.render(filledForm, email, resetUid, user.isActive()));
        	
        } else {
        	user.setPassword(filledForm.field("password").valueOr(""));
        	user.setActive(true);
        	user.setPasswordLinkSent(null);
        	user.save();
        	user.login(session());
        	return redirect(routes.Application.index());
        }
	}
	
	/**
	 * GET /account/activate
	 * Alias for showResetPasswordForm(email, resetUid), so that the URL looks nicer.
	 * @param email
	 * @param resetUid
	 * @return
	 */
	public static Result showActivateForm(String email, String activateUid) {
		return showResetPasswordForm(email, activateUid);
	}
	
	/**
	 * POST /account/activate
	 * Alias for resetPassword(email, resetUid), so that the URL looks nicer.
	 * @param email
	 * @param resetUid
	 * @return
	 */
	public static Result activate(String email, String activateUid) {
		return resetPassword(email, activateUid);
	}
	
	public static boolean sendEmail(String subject, String html, String text, String recipientName, String recipientEmail) {
		try {
			String host = Setting.get("mail.smtp.host");
			String port = Setting.get("mail.smtp.port");
			String from = Setting.get("mail.from.email");
			String fromName = Setting.get("mail.from.name");
			String ssl = Setting.get("mail.smtp.ssl");
			String username = Setting.get("mail.username");
			if (host == null || port == null || from == null || fromName == null || ssl == null || username == null) {
				Logger.error("Either host, port, e-mail, sender name, username or ssl missing.", new Exception("E-mail misconfigured"));
				return false;
			}
			
			HtmlEmail email = new HtmlEmail();
			email.setHostName(host);
			email.setDebug(Application.debug);
			email.setFrom(from, fromName);
			email.setSubject("[DAISY Pipeline 2] "+subject);
			email.setHtmlMsg(html);
			email.setTextMsg(text);
			email.addTo(recipientEmail, recipientName);
			
			String prefix = "true".equals(ssl) ? "mail.smtps" : "mail.smtp";
			email.setSSL("true".equals(ssl));
			email.setTLS("true".equals(ssl));
			
			// AUTH
			if (Setting.get("mail.username").length() > 0) {
				String password = Setting.get("mail.password")+"";
				
				email.setAuthenticator(new DefaultAuthenticator(username, password));
				email.getMailSession().getProperties().put(prefix+".auth", "true");
			} else {
				email.getMailSession().getProperties().put(prefix+".auth", "false");
			}
			
			email.getMailSession().getProperties().put(prefix+".starttls.enable", Setting.get("mail.smtp.ssl"));
			
			email.getMailSession().getProperties().put("mail.debug", Application.debug+"");
			email.getMailSession().getProperties().put(prefix+".debug", Application.debug+"");
			
			email.getMailSession().getProperties().put(prefix+".user", username);
			email.getMailSession().getProperties().put(prefix+".host", host);
			email.getMailSession().getProperties().put(prefix+".port", port);
			if ("true".equals(ssl)) {
				email.getMailSession().getProperties().put(prefix+".socketFactory.port", port);
				email.getMailSession().getProperties().put(prefix+".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
				email.getMailSession().getProperties().put(prefix+".socketFactory.fallback", "false");
			}
			email.send();
			return true;
		} catch (EmailException e) {
			Logger.error("EmailException occured while trying to send an e-mail!", e);
			return false;
		}
	}
	
}
