package models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.avaje.ebean.Model;

import javax.persistence.*;

import play.data.validation.*;
import utils.ObfuscatedString;

@Entity
public class Setting extends Model {

	@Id
    @Constraints.Required
    private String name;
    
	private String value;
    
	private static final List<String> obfuscatedSettings = Arrays.asList("dp2ws.secret", "mail.password");
    
    // -- Queries
    
	public static Model.Finder<String,Setting> find = new Model.Finder<String, Setting>(Setting.class);
    
    @Transient
    private static Map<String,String> cache = new HashMap<String,String>();
    
    /** Get the value of a setting */
    public static String get(String name) {
    	synchronized (cache) {
    		if (cache.containsKey(name))
        		return cache.get(name);
		}
    	
    	Setting setting = find.where().eq("name", name).findUnique();
    	if (setting == null)
    		return null;
    	if (getObfuscatedsettings().contains(name))
    		return ObfuscatedString.unobfuscate(setting.getValue());
    	return setting.getValue();
    }
    
    /** Set the value of a setting. If value is null, the setting is deleted. */
    public static void set(String name, String value) {
    	Setting setting = find.where().eq("name", name).findUnique();
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

}