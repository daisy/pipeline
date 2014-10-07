package org.daisy.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class DefaultCommandParserResult implements CommandParserResult {
	private final HashMap<String, String> optional;
	private final ArrayList<String> unnamed;
	
	static class Builder {
		private final HashMap<String, String> optional;
		private final ArrayList<String> unnamed;

		Builder() {
			optional = new HashMap<String, String>();
			unnamed = new ArrayList<String>();
		}
		
		Builder addOptional(String key, String value) {
			optional.put(key, value);
			return this;
		}
		
		Builder addRequired(String value) {
			unnamed.add(value);
			return this;
		}
		
		CommandParserResult build() {
			return new DefaultCommandParserResult(this);
		}
	}
	
	private DefaultCommandParserResult(Builder builder) {
		this.optional = builder.optional;
		this.unnamed = builder.unnamed;
	}

	@SuppressWarnings("unchecked")
	public List<String> getRequired() {
		return (List<String>)unnamed.clone();
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getOptional() {
		return (Map<String, String>)optional.clone();
	}

	public Map<String, String> toMap(String prefix) {
		HashMap<String, String> ret = new HashMap<String, String>();
		int i = 0;
		for (String s : unnamed) {
			ret.put(prefix+i, s);
			i++;
		}
		for (String key : optional.keySet()) {
			ret.put(key, optional.get(key));
		}
		return ret;
	}

}
