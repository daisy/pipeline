package utils;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import play.data.Form;
import play.mvc.Http.Request;

public class FormHelper {
	
	public static Map<String,String> params(Request request) {
		Map<String,String> params = new HashMap<String,String>();
		for (String key : request.queryString().keySet())
			params.put(key, request.queryString().get(key)[0]);
		for (String key : request.body().asFormUrlEncoded().keySet())
			params.put(key, request.body().asFormUrlEncoded().get(key)[0]);
		return params;
	}
	
	public static JsonNode asJson(Form<?> filledForm) {
		return asJson(filledForm, null);
	}
	public static JsonNode asJson(Form<?> filledForm, String[] exclude) {
		if (exclude == null)
			exclude = new String[0];
		Map<String, Object> result = new HashMap<String,Object>();
		
		// data
		Map<String,String> data = new HashMap<String,String>();
		for (String name : filledForm.data().keySet()) {
			boolean excludeField = false;
			for (String e : exclude) {
				if (name.equals(e)) {
					excludeField = true;
					break;
				}
			}
			if (!excludeField) {
				data.put(name, filledForm.data().get(name));
			}
		}
		result.put("data", data);
		
		// errors
		result.put("errors", filledForm.errorsAsJson());
		
		return play.libs.Json.toJson(result);
	}
	
}
