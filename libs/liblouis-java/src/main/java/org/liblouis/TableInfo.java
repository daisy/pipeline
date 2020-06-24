package org.liblouis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TableInfo {
	
	private final String table;
	private final Map<String,String> cache;
	private final Set<String> missingFields;
	
	TableInfo(String table) {
		this.table = table;
		cache = new HashMap<String,String>();
		missingFields = new HashSet<String>();
	}
	
	public String get(String key) {
		if (cache.containsKey(key))
			return cache.get(key);
		if (missingFields.contains(key))
			return null;
		String value = Louis.getLibrary().lou_getTableInfo(table, key);
		if (value != null)
			cache.put(key, value);
		else
			missingFields.add(key);
		return value;
	}
}
