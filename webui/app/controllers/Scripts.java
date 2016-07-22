package controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.DataType;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.models.datatypes.EnumType;
import org.daisy.pipeline.client.models.datatypes.EnumType.Value;
import org.daisy.pipeline.client.models.datatypes.RegexType;

import models.User;
import models.UserSetting;
import play.Logger;
import play.mvc.*;
import utils.Pair;

public class Scripts extends Controller {

	public static Result getScriptsJson() {
		if (FirstUse.isFirstUse())
			return unauthorized("unauthorized");

		User user = User.authenticate(request(), session());
		if (user == null)
			return unauthorized("unauthorized");
		
		List<Script> scripts = get();
		if (scripts == null) {
			scripts = new ArrayList<Script>();
		}
		for (int i = scripts.size()-1; i >= 0; i--) {
			if ("false".equals(UserSetting.get(-2L, "scriptEnabled-"+scripts.get(i).getId()))) {
				scripts.remove(i);
			}
		}
		
		JsonNode scriptsJson = play.libs.Json.toJson(scripts);
		return ok(scriptsJson);
	}
	
	public static Result getScriptJson(String id) {
		if (FirstUse.isFirstUse())
			return unauthorized("unauthorized");

		User user = User.authenticate(request(), session());
		if (user == null)
			return unauthorized("unauthorized");
		
		if ("false".equals(UserSetting.get(-2L, "scriptEnabled-"+id))) {
			return forbidden();
		}
		
		Script script = get(id);
		
		if (script != null) {
			JsonNode scriptJson = play.libs.Json.toJson(script);
			return ok(scriptJson);
		} else {
			return internalServerError("An error occured while trying to retrieve the script '"+id+"'.");
		}
	}
	
	public static String chooseWidget(Argument arg) {
		if (arg.getDataType() != null) {
			DataType dataType = Application.ws.getDataType(arg.getDataType());
			if (dataType instanceof EnumType) {
				return "enum";
			}
			if (dataType instanceof RegexType) {
				return "regex";
			}
		}
		return arg.getType();
	}
	
	public static JsonNode getDataTypeJson(Argument argument) {
		Logger.debug("getDataTypeJson("+argument.getName()+")");
		List<Map<String,String>> values = new ArrayList<Map<String,String>>();
		Logger.debug("Getting datatype: "+argument.getDataType());
		EnumType enumType = (org.daisy.pipeline.client.models.datatypes.EnumType)(Application.ws.getDataType(argument.getDataType()));
		if (enumType != null) {
			for (Value enumValue : enumType.values) {
				Map<String,String> value = new HashMap<String,String>();
				value.put("name", enumValue.name);
				value.put("nicename", enumValue.getNicename());
				value.put("description", (String)enumValue.getDescription()); // cast to string because getDescription wrongly declares Object as return type
				values.add(value);
			}
		}
		JsonNode json = play.libs.Json.toJson(values);
		return json;
	}

	public static class ScriptForm {

		public Map<String,List<String>> errors;

		public String guestEmail;

		public ScriptForm(Long userId, Script script, Map<String, String[]> params) {

			// Parse all arguments
			for (String param : params.keySet()) {
				if (param == null || param.startsWith("_")) continue; // skip arguments starting with an underscore
				Argument argument = script.getArgument(param);
				if (argument == null) {
					Logger.debug("'"+param+"' is not an argument for the script '"+script.getId()+"'; ignoring it");
					continue;
				}
				
				argument.clear();
				for (String value : params.get(param)) {
					String type = argument.getType();
					if ("".equals(value) && ("anyDirURI".equals(type) || "anyFileURI".equals(type) || "anyURI".equals(type))) {
						continue;
					}
					argument.add(value);
				}
			}

			if (userId < 0 && params.containsKey("_guest-email"))
				this.guestEmail = params.get("_guest-email")[0];

			this.errors = new HashMap<String, List<String>>();
		}

		public void validate() {
			if (guestEmail != null && !"".equals(guestEmail) && !guestEmail.matches("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$")) {
				addError("_guest-email", "Please enter a valid e-mail address.");
			}

			// TODO: validate arguments (consider implementing validation in pipeline-clientlib-java)
		}

		public boolean hasErrors() {
			return errors.size() > 0;
		}

		public void addError(String field, String error) {
			if (!errors.containsKey(field))
				errors.put(field, new ArrayList<String>());
			errors.get(field).add(error);
		}
	}
	
	private static Map<String, Pair<Script, Date>> scriptCache = new HashMap<String, Pair<Script, Date>>();
	private static List<Script> scriptList = new ArrayList<Script>();
	private static Date scriptListCacheLastUpdate = new Date();
	public static Script get(String scriptId) { return get(false, scriptId); }
	public static Script get(boolean forceUpdate, String scriptId) {
		Pair<Script, Date> scriptAndDate = scriptCache.get(scriptId);
		if (forceUpdate || scriptAndDate == null || scriptAndDate.b.before(new Date(new Date().getTime() - 1000*60))) {
			// not in cache or cache more than 1 minute old
			Script script = Application.ws.getScript(scriptId);
			if (script == null) {
				scriptCache.remove(script);
			} else {
				scriptCache.put(scriptId, new Pair<Script, Date>(script, new Date()));
			}
			return script;
			
		} else {
			return scriptAndDate.a;
		}
	}
	public static List<Script> get() { return get(false); }
	public static List<Script> get(boolean forceUpdate) {
		if (forceUpdate || scriptList == null || scriptList.isEmpty() || scriptListCacheLastUpdate.before(new Date(new Date().getTime() - 1000*60))) {
			// no scripts in cache or cache more than 1 minute old
			scriptList = Application.ws.getScripts();
			if (scriptList == null) {
				scriptList = new ArrayList<Script>();
			}
			scriptListCacheLastUpdate = new Date();
		}
		return scriptList;
	}

}
