package models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Model;

import javax.persistence.*;

import play.Logger;
import play.Play;
import play.data.validation.*;
import utils.ObfuscatedString;

@Entity
public class Setting extends Model {

	@Id
    @Constraints.Required
    private String name;
    
	private String value;
    
	private static final List<String> obfuscatedSettings = Arrays.asList("dp2ws.secret", "mail.password");
	
	@Transient
    private static String _ebeanServer = null;
    
    // -- Queries
    
    @Transient
	public static Model.Finder<String,Setting> _find = null;
    
    public static Model.Finder<String,Setting> find() {
        if (Setting._find == null) {
            Setting._find = new Model.Finder<String, Setting>(ebeanServer(), Setting.class);
        }
        return Setting._find;
    }
    
    @Transient
    private static Map<String,String> cache = new HashMap<String,String>();
    
    /** Get the value of a setting */
    public static String get(String name) {
    	synchronized (cache) {
    		if (cache.containsKey(name))
        		return cache.get(name);
		}
    	
    	Setting setting = find().where().eq("name", name).findUnique();
    	if (setting == null)
    		return null;
    	if (getObfuscatedsettings().contains(name))
    		return ObfuscatedString.unobfuscate(setting.getValue());
    	return setting.getValue();
    }
    
    /** Set the value of a setting. If value is null, the setting is deleted. */
    public static void set(String name, String value) {
    	Setting setting = find().where().eq("name", name).findUnique();
    	if (setting == null) {
    		if (value == null)
    			return;
    		setting = new Setting();
    		setting.setName(name);
    	}
    	if (getObfuscatedsettings().contains(name))
    		setting.setValue(ObfuscatedString.obfuscate(value));
    	else
    		setting.setValue(value);
    	if (value == null)
    		setting.delete();
    	else
    		setting.save();
    	
    	// Cache settings
    	synchronized (cache) {
    		cache.put(name, value);
    	}
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static List<String> getObfuscatedsettings() {
		return obfuscatedSettings;
	}
	
	
	
	
    public static String ebeanServer() {
		if (_ebeanServer == null) {
			for (String ebeanServer : Play.application().configuration().getConfig("db").subKeys()) {
				boolean enabled = Play.application().configuration().getBoolean("db." + ebeanServer + ".enabled", false);
				if (enabled) {
					_ebeanServer = ebeanServer;
				} else {
					Logger.info("Database connection '" + ebeanServer + "' is not enabled (set db." + ebeanServer + ".enabled=true to enable)");
				}
			}
    	}
		if (_ebeanServer == null) {
			Logger.warn("No database connection has been defined. Please configure a database connection in application.conf.");
		}
		if ("default".equals(_ebeanServer)) {
			return null;
		} else {
			return _ebeanServer;
		}
    }
    
	@Override
    public void save() {
    	db(ebeanServer()).save(this);
    }
    
	@Override
    public void update() {
    	db(ebeanServer()).update(this);
    }
    
	@Override
    public void insert() {
    	db(ebeanServer()).insert(this);
    }
    
	@Override
    public void delete() {
    	db(ebeanServer()).delete(this);
    }
    
	@Override
    public void refresh() {
    	db(ebeanServer()).refresh(this);
    }

}