package org.daisy.pipeline.gui.utils;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings {
    
    private static Preferences userPrefs, 
                                sysPrefs;
    
    enum Types {
        STRING,
        BOOLEAN;
    }
    
    public enum InputTypes {
        NONE,
        DIRECTORY_SEQUENCE,
        CHECKBOX;
    }
    
    public enum Prefs {
        LAST_IN_DIR(PrefCategories.JOB_OPTIONS, Types.STRING, InputTypes.NONE, "Last Input Directory", "", null, null),
        DEF_IN_DIR_ENABLED(PrefCategories.JOB_OPTIONS, Types.BOOLEAN, InputTypes.CHECKBOX, "Toggle Default Input Directory", "false", "Enable use of default input directory.", null),
        DEF_IN_DIR(PrefCategories.JOB_OPTIONS, Types.STRING, InputTypes.DIRECTORY_SEQUENCE, "Default Input Directory", "", "Initial directory path used for Job Option file browsers in input fields.", Prefs.DEF_IN_DIR_ENABLED),
        LAST_OUT_DIR(PrefCategories.JOB_OPTIONS, Types.STRING, InputTypes.NONE, "Last Output Directory", "", null, null),
        DEF_OUT_DIR_ENABLED(PrefCategories.JOB_OPTIONS, Types.BOOLEAN, InputTypes.CHECKBOX, "Toggle Default Output Directory", "false", "Enable use of default output directory.", null),
        DEF_OUT_DIR(PrefCategories.JOB_OPTIONS, Types.STRING, InputTypes.DIRECTORY_SEQUENCE, "Default Output Directory", "", "Initial directory path used for Job Option file browsers in output fields.", Prefs.DEF_OUT_DIR_ENABLED);
        
    /*-------------------------------------------------------------------------------------------------------*/
        
        PrefCategories category;
        String key, def, tooltip;
        Types type;
        InputTypes inputType;
        Prefs enablePref;
        
        
        Prefs(PrefCategories category, Types type, InputTypes inputType, String key, String def, String tooltip, Prefs enablePref) {
            this.category = category;
            this.type = type;
            this.inputType = inputType;
            this.key = key;
            this.def = def;
            if (!inputType.equals(InputTypes.NONE) && tooltip == null)
                throw new NullPointerException("argument tooltip cannot be null for Prefs with inputTypes other than NONE.");
            this.tooltip = tooltip;
            this.enablePref = enablePref;
        }
        
        /*-------------------------------------------------*/
        
        public PrefCategories category() {return category;}
        public String key() {return key;}
        public InputTypes inputType() {return inputType;}
        public String tooltip() {return tooltip;}
        
        public String defString() {
            return def;
        }
        public boolean defBoolean() {
            return Boolean.parseBoolean(def);
        }
        public Prefs enablePref() {return enablePref;}
        public boolean hasEnablePref() {return enablePref != null;}
    }
    
    public enum PrefCategories {
        JOB_OPTIONS("Job Options", false);
        
        String val;
        boolean isSystem;
        PrefCategories(String val, boolean isSystem) {
            this.val = val;
            this.isSystem = isSystem;
        }
        public String val() {return val;}
        public boolean isSystem() {return isSystem;}
    }

    
/*-------------------------------------------------------------------------------------------------------*/
    
    public static void init() {
        userPrefs = Preferences.userRoot().node("daisy/pipeline");
        sysPrefs = Preferences.systemRoot().node("daisy/pipeline");
        buildDefaults();
    }
    
    
    private static void buildDefaults() {
        try {
            // Create Category nodes if they don't exist
            for (PrefCategories category: PrefCategories.values()) {
                if (category.isSystem && !sysPrefs.nodeExists(category.val))
                        sysPrefs.node(category.val);
                else if (!category.isSystem && !userPrefs.nodeExists(category.val))
                        userPrefs.node(category.val);
            }
            
            // Add default prefs if they don't exist
            for (Prefs pref: Prefs.values())
                switch (pref.type) {
                    case STRING:
                        if (getString(pref).equals(pref.defString()))
                            putString(pref, pref.defString());
                        break;
                    case BOOLEAN:
                        if (getBoolean(pref) == pref.defBoolean())
                            putBoolean(pref, pref.defBoolean());
                        break;
                }
            
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
    
    
/*-------------------------------------------------------------------------------------------------------*/
    
    public static String getString(Prefs pref) {
        if (pref.category.isSystem)
            return sysPrefs.node(pref.category.val).get(pref.key, pref.defString());
        else
            return userPrefs.node(pref.category.val).get(pref.key, pref.defString());
    }
    
    public static boolean getBoolean(Prefs pref) {
        if (pref.category.isSystem)
            return sysPrefs.node(pref.category.val).getBoolean(pref.key, pref.defBoolean());
        else
            return userPrefs.node(pref.category.val).getBoolean(pref.key, pref.defBoolean());
    }
    
    public static void putString(Prefs pref, String newValue) {
        if (pref.category.isSystem)
            sysPrefs.node(pref.category.val).put(pref.key, newValue);
        else
            userPrefs.node(pref.category.val).put(pref.key, newValue);
    }
    
    public static void putBoolean(Prefs pref, Boolean newValue) {
        if (pref.category.isSystem)
            sysPrefs.node(pref.category.val).putBoolean(pref.key, newValue);
        else
            userPrefs.node(pref.category.val).putBoolean(pref.key, newValue);
    }
    
    public static void toggleBoolean(Prefs pref) {
        putBoolean(pref, !getBoolean(pref));
    }

    
/*-------------------------------------------------------------------------------------------------------*/
    // Unit Testing
    public static void main(String[] args) {
        Settings.init();
        Settings.putString(Prefs.LAST_OUT_DIR, "test1");
        Settings.putString(Prefs.DEF_OUT_DIR, "test");
        Settings.putString(Prefs.DEF_OUT_DIR, "test2");
        String test1 = Settings.getString(Prefs.LAST_OUT_DIR),
                test2 = Settings.getString(Prefs.DEF_OUT_DIR);
        
        assert test1.equals("test1"): "Expected: \"test1\", Output: \"" + test1 + "\""; 
        assert test2.equals("test2"): "Expected: \"test2\", Output: \"" + test2 + "\""; 
    }
}
