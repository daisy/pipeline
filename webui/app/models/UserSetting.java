package models;

import java.util.HashMap;
import java.util.Map;

import com.avaje.ebean.*;

import javax.persistence.*;

import play.data.validation.*;

@Entity(name="usersetting")
@Table(name="usersetting")
public class UserSetting extends Model {
	
	@Id
	@Constraints.Required
	public String id;

	@Constraints.Required
	@Column(name="user_id") public Long user;

	@Constraints.Required
	public String name;

	public String value;

	// -- Queries
	public static Model.Finder<String,UserSetting> find = new Model.Finder<String, UserSetting>(UserSetting.class);

	@Transient
	private static Map<Long,Map<String,String>> cache;
	static {
		cache = new HashMap<Long,Map<String,String>>();
	}

	/** Get the value of a setting */
	public static String get(Long user, String name) {
		if (user < -2) user = -2L;
		if (cache(user, name) != null) {
			return cache(user, name);
		}

		UserSetting setting = find.where().eq("user", user).eq("name", name).findUnique();
		if (setting == null)
			return null;
		return setting.value;
	}

	/** Set the value of a setting. If value is null, the setting is deleted. */
	public static void set(Long user, String name, String value) {
		if (user < -2) user = -2L;
		UserSetting setting = find.where().eq("user", user).eq("name", name).findUnique();
		if (setting == null) {
			if (value == null)
				return;
			setting = new UserSetting();
			setting.user = user;
			setting.name = name;
		}
		setting.value = value;
		if (value == null)
			setting.delete();
		else
			setting.save();

		// Cache settings
		cache(user, name, value);
	}

	private static String cache(Long user, String name) {
		synchronized (cache) {
			if (!cache.containsKey(user)) return null;
			return cache.get(user).get(name);
		}
	}

	private static void cache(Long user, String name, String value) {
		synchronized (cache) {
			if (!cache.containsKey(user)) cache.put(user, new HashMap<String,String>());
			cache.get(user).put(name, value);
		}
	}

}