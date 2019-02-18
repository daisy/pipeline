package models;

import play.Logger;
import play.api.libs.Crypto;

import com.avaje.ebean.*;
import controllers.Administrator.CreateAdminForm;
import javax.persistence.*;

import java.util.*;

import play.data.Form;
import play.data.format.*;
import play.data.validation.*;
import play.mvc.Http.Request;
import play.mvc.Http.Session;

@Entity(name="users")
@Table(name="users")
public class User extends Model {
	
	public static class UserSetPassword {
		
		@Constraints.Required
		@Constraints.MinLength(6)
		public String password;

		@Constraints.Required
		@Constraints.MinLength(6)
		public String repeatPassword;

		public static void validate(Form<CreateAdminForm> filledForm) {
			if (!filledForm.field("password").valueOr("").equals("") && !filledForm.field("password").valueOr("").equals(filledForm.field("repeatPassword").value()))
				filledForm.reject("repeatPassword", "Password doesn't match.");
		}
	}
	
	// ---------- Static stuff ----------

	public static final Long LINK_TIMEOUT = 24*3600*1000L;
	
	public static final Long JS_MAX_INT = +9007199254740992L;
	public static final Long JS_MIN_INT = -9007199254740992L;

	// ---------- Instance stuff ----------

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	private Long id; // <= -2: logged in as guest, -1: not logged in, >= 0: logged in as user

	@Constraints.Required
	@Formats.NonEmpty
	@Constraints.Email
	private String email;
	
	@Constraints.Required
	@Constraints.MinLength(1)
	@Constraints.Pattern("[^{}\\[\\]();:'\"<>]+") // Avoid breaking JavaScript code in templates
	private String name;

	@Constraints.Required
	@Constraints.MinLength(6)
	private String password;

	@Constraints.Required
	private boolean admin;

	@Constraints.Required
	private boolean active; // Account is deactivated until the user sets a password

	// Crypto.sign(email+passwordLinkSent.getTime()) = uid sent in link
	private Date passwordLinkSent; // Time that the password link was sent

	/**
	 * Constructor
	 */
	public User(String email, String name, String password, boolean admin) {
		this.email = email.toLowerCase();
		this.name = name;
		if ("".equals(password)) {
			this.password = "";
			this.active = false;
		} else {
			this.password = play.Play.application().injector().instanceOf(Crypto.class).sign(password);
			this.active = true;
		}
		this.admin = admin;
	}
	
	/**
	 * Try setting the password using the provided activation UID.
	 * 
	 * @param uid
	 * @param password
	 */
	public void activate(String uid, String password) {
		if (getActivationUid().equals(uid)) {
			this.active = true;
			this.password = play.Play.application().injector().instanceOf(Crypto.class).sign(password);
		}
	}

	/**
	 * Generates a new activation UID.
	 */
	public void makeNewActivationUid() {
		this.passwordLinkSent = new Date();
	}

	/**
	 * Gets the activation UID.
	 * @return
	 */
	public String getActivationUid() {
		if (this.passwordLinkSent == null || new Date(new Date().getTime() - LINK_TIMEOUT).after(this.passwordLinkSent)) {
			return null;
		}
		return play.Play.application().injector().instanceOf(Crypto.class).sign(this.email+this.passwordLinkSent.getTime()/1000);
	}

	public String toString() {
		return "User(" + email + ")";
	}

	/**
	 * Encrypts and sets the password.
	 */
	public void setPassword(String password) {
		this.password = play.Play.application().injector().instanceOf(Crypto.class).sign(password);
	}
	
	private static Random randomBrowserId = new Random(new Date().getTime());
	public static Long getBrowserId(User user) {
		Long userId = user == null ? null : user.id;
		Long browserId = JS_MIN_INT + (long)(randomBrowserId.nextDouble() * ((JS_MAX_INT - JS_MIN_INT) + 1)); // Random integer in range [JS_MIN_INT,JS_MAX_INT] 
		NotificationConnection.createBrowserIfAbsent(userId, browserId);
		return browserId;
	}
	
	// -- Queries

	@Transient
	public static Model.Finder<String,User> _find = null;
    
    public static Model.Finder<String,User> find() {
        if (User._find == null) {
            User._find = new Model.Finder<String, User>(Setting.ebeanServer(), User.class);
        }
        return User._find;
    }

	/** Retrieve all users. */
	public static List<User> findAll() {
		return find().all();
	}

	/** Retrieve a User from email. */
	public static User findByEmail(String email) {
		List<User> users = find().where().eq("email", email.toLowerCase()).findList();
		for (int u = users.size()-1; u > 0; u--)
			users.get(u).delete();
		if (users.size() == 0)
			return null;
		else
			return users.get(0);
	}

	/** Retrieve a User from id. */
	public static User findById(long id) {
		List<User> users = find().where().eq("id", id).findList();
		for (int u = users.size()-1; u > 0; u--)
			users.get(u).delete();
		if (users.size() == 0)
			return null;
		else
			return users.get(0);
	}
	
	/** Authenticate a user. */
	public static User authenticate(Request request, Session session) {
		User user;
		
		Long id = models.User.parseUserId(session); // login with session variables
		if (id == null && request.queryString().containsKey("guestid") && request.queryString().get("guestid").length > 0) {
			try {
				id = Long.parseLong("-"+request.queryString().get("guestid")[0]); // resume guest session
			} catch (NumberFormatException e) {
				// do nothing
			}
		}
		
		if (id == null) { // no userid; try automatic login
			if ("true".equals(Setting.get("users.guest.automaticLogin")))
				return loginAsGuest(session);
			else
				return null;
			
		} else if (id >= 0) { // normal or admin user
			try {
				user = find().where()
						.eq("id", id)
						.eq("email", session.get("email"))
						.eq("password", session.get("password"))
						.findUnique();
				user.login(session);
				return user;
				
			} catch (NullPointerException e) {
				// Not found or wrong credentials
				return null;
			}
			
		} else if ("true".equals(models.Setting.get("users.guest.allowGuests"))) { // guest user
			user = new User("", models.Setting.get("users.guest.name"), "", false);
			user.id = id;
			user.login(session);
			return user;
			
		} else {
			// trying to log in as guest, but guest login is not allowed
			return null;
		}
	}
	
	private static Random randomGuestUserId = new Random(new Date().getTime());
	public static User loginAsGuest(Session session) {
		if (!"true".equals(models.Setting.get("users.guest.allowGuests")))
			return null;
		
		User guest = new User("", models.Setting.get("users.guest.name"), "", false);
		guest.id = -2-(long)randomGuestUserId.nextInt(2147483639); // <= -2: logged in guest, -1: not logged in, >= 0: logged in user
		guest.login(session);
		return guest;
	}

	public void login(Session session) {
		if (id != null) {
			session.put("userid", id+"");
	    	session.put("name", name);
	    	session.put("email", email);
	    	session.put("password", password);
	    	session.put("admin", admin+"");
		} else {
			session.remove("userid");
			Logger.warn("Could not log in user '"+name+"' ('"+email+"'); userid is null.");
		}
	}

	/** Authenticate a user with an unencrypted password */
	public static User authenticateUnencrypted(String email, String password, Session session) {
		try {
			User user = find().where()
					.eq("email", email.toLowerCase())
					.eq("password", play.Play.application().injector().instanceOf(Crypto.class).sign(password))
					.findUnique();
			user.login(session);
			return user;
			
		} catch (NullPointerException e) {
			// Not found or wrong credentials
			return null;
		}
	}

	/**
	 * Validate a new user.
	 * @param filledForm
	 */
	public static void validateNew(Form<User> filledForm) {
		if (User.findByEmail(filledForm.field("email").valueOr("")) != null)
			filledForm.reject("email", "That e-mail address is already taken");
		
		String adminString = filledForm.field("admin").valueOr("");
		if (!adminString.equals("true") && !adminString.equals("false"))
			filledForm.reject("admin", "The user must either *be* an admin, or *not be* an admin");
		
		String password = filledForm.field("password").valueOr("");
		if ("true".equals(Setting.get("mail.enable"))) {
			// "password" are not set by the administrator when e-mails are enabled
			filledForm.errors().remove("password");
			
		} else {
			if (0 <= password.length() && password.length() < 6) {
				filledForm.reject("password", "The password must be at least 6 characters long");
			}
		}
		
		filledForm.errors().remove("active");
	}
	
	/**
	 * Validate changes for a user.
	 * @param filledForm
	 * @param user
	 */
	public void validateChange(Form<User> filledForm, User user) {
		String formEmail = filledForm.field("email").value();
		if (formEmail != null)
			formEmail = formEmail.toLowerCase();
		if (!this.email.equals(formEmail) && User.findByEmail(formEmail) != null)
			filledForm.reject("email", "That e-mail address is already taken");
		
		String password = filledForm.field("password").valueOr("");
		if (password.length() > 0) {
			// Trying to change the password
			if (password.length() < 6)
				filledForm.reject("password", "The password must be at least 6 characters long");
			
		} else {
			// Not trying to change the password
			if (!(this.admin + "").equals(filledForm.field("admin").valueOr(""))) {
				String adminString = filledForm.field("admin").valueOr("");
				if (!adminString.equals("true") && !adminString.equals("false"))
					filledForm.reject("admin", "The user must either *be* an admin, or *not be* an admin");
				
				if (this.id.equals(user.id)) {
					filledForm.reject("admin", "Only other admins can demote you to a normal user, you cannot do it yourself");
					
				} else if (user.admin) {
					filledForm.errors().remove("password"); // dont throw "error.required" for "password" when an admin edits another user
				}
			}
			
		}
	}
	
	/**
	 * Whether the form contains changes to the user.
	 * @param filledForm
	 * @return
	 */
	public boolean hasChanges(Form<User> filledForm) {
		if (!this.name.equals(filledForm.field("name").valueOr("")))
			return true;
		
		String formEmail = filledForm.field("email").valueOr("");
		if (formEmail != null)
			formEmail = formEmail.toLowerCase();
		if (!this.email.equals(formEmail))
			return true;
		
		if (filledForm.field("password").valueOr("").length() != 0 && !this.password.equals(play.Play.application().injector().instanceOf(Crypto.class).sign(filledForm.field("password").valueOr(""))))
			return true;

		if (!(this.admin + "").equals(filledForm.field("admin").valueOr("")))
			return true;
		
		return false;
	}
	
	public List<Job> getJobs() {
		return Job.find().where().eq("user", id).findList();
	}
	
	@Override
	public void delete() {
		try {
			List<Job> jobs = getJobs();
			for (Job job : jobs)
				job.deleteFromEngineAndWebUi();
			db(Setting.ebeanServer()).delete(this);
		} catch (javax.persistence.OptimisticLockException e) {
			Logger.warn("Could not delete user "+this.id+" ("+this.name+" / "+this.email+")", e);
		}
	}
	
	@Override
	public void save() {
		db(Setting.ebeanServer()).save(this);
		
		// refresh id after save
		if (this.id == null) {
			User user = User.findByEmail(this.email);
			if (user != null) {
				this.id = user.id;
			}
		}
	}
	
	@Override
    public void update() {
    	db(Setting.ebeanServer()).update(this);
    }
    
	@Override
    public void insert() {
    	db(Setting.ebeanServer()).insert(this);
    }
    
	@Override
    public void refresh() {
    	db(Setting.ebeanServer()).refresh(this);
    }
	
	/**
	 * Parses the userid from the session. Useful to avoid having to handle cases (especially in templates) where session("userid") is neither null nor a string representation of a Long.
	 * @param session
	 * @return
	 */
	public static Long parseUserId(Session session) {
		try {
    		return Long.parseLong(session.get("userid"));
    	} catch(NumberFormatException e) {
    		session.remove("userid");
    		return null;
    	}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email == null ? email : email.toLowerCase();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Date getPasswordLinkSent() {
		return passwordLinkSent;
	}

	public void setPasswordLinkSent(Date passwordLinkSent) {
		this.passwordLinkSent = passwordLinkSent;
	}

	public String getPassword() {
		return password;
	}

}