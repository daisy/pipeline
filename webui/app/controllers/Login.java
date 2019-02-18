package controllers;

import play.mvc.*;
import play.data.*;

import models.*;

public class Login extends Controller {
  
    // -- Authentication
    
	public static class LoginForm {
        public String email;
        public String password;
        
        public String validate() {
            if (User.authenticateUnencrypted(email, password, session()) == null) {
                return "Invalid e-mail address or password";
            }
            return null;
        }
    }

    /**
     * Login page.
     */
    public static Result login() {
    	if (FirstUse.isFirstUse()) {
    		if (User.find().where().eq("admin", true).findRowCount() > 0);
    			// Server mode and admin exists; require login
    		else
    			return redirect(routes.FirstUse.getFirstUse());
    	}
    	
    	User.parseUserId(session());
		return ok(views.html.Login.login.render(play.data.Form.form(LoginForm.class)));
    }
    
    /**
     * Handle login form submission.
     */
    public static Result authenticate() {
        Form<LoginForm> loginForm = play.data.Form.form(LoginForm.class).bindFromRequest();
        
    	User user = User.authenticateUnencrypted(loginForm.field("email").valueOr(""), loginForm.field("password").valueOr(""), session());
        if (loginForm.hasErrors()) {
            return badRequest(views.html.Login.login.render(loginForm));
        } else {
        	user.login(Controller.session());
        	return redirect(routes.Application.index());
        }
    }
    
    /**
     * Handle login form submission for guest logins.
     */
    public static Result authenticateGuest() {
    	if (!"true".equals(models.Setting.get("users.guest.allowGuests"))) {
    		return badRequest(views.html.Login.login.render(play.data.Form.form(LoginForm.class)));
    	}
    	
    	User.loginAsGuest(Controller.session());
    	
    	return redirect(routes.Application.index());
    }
    
    public static Result resetPassword() {
    	String email = request().queryString().containsKey("email") ? request().queryString().get("email")[0] : "";
    	email = email.toLowerCase();
    	User user = User.findByEmail(email);
    	
    	if ("".equals(email)) {
    		flash("error", "You must enter an e-mail address.");
    		return badRequest(views.html.Login.login.render(play.data.Form.form(LoginForm.class)));
    		
    	} else if (user == null) {
    		flash("error", "There is no user using that e-mail address; did you type it correctly?");
    		return badRequest(views.html.Login.login.render(play.data.Form.form(LoginForm.class)));
    		
    	} else {
    		user.makeNewActivationUid();
    		user.save();
			String resetUrl = Application.absoluteURL(routes.Account.showResetPasswordForm(user.getEmail(), user.getActivationUid()).absoluteURL(request()));
			String html = views.html.Account.emailResetPassword.render(resetUrl).body();
			String text = "Go to this link to change your password: "+resetUrl;
			if (Account.sendEmail("Reset your password", html, text, user.getName(), user.getEmail()))
				flash("success", "An e-mail has been sent to "+email+" with further instructions. Please check your e-mail.");
			else
				flash("error", "Was unable to send the e-mail. Please notify the owners of this website so they can fix their e-mail settings.");
    		return ok(views.html.Login.login.render(play.data.Form.form(LoginForm.class)));
    	}
    }

    /**
     * Logout and clean the session.
     */
    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        
        if ("true".equals(Setting.get("users.guest.automaticLogin")))
			User.loginAsGuest(session());
        
        return redirect(routes.Login.login());
    }

}
