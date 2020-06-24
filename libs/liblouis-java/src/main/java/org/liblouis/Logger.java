package org.liblouis;

import java.util.HashMap;
import java.util.Map;

public interface Logger {
	
	public void log(Level level, String message);
	
	public enum Level {
		ALL(-2147483648),
		DEBUG(10000),
		INFO(20000),
		WARN(30000),
		ERROR(40000),
		FATAL(50000),
		OFF(2147483647);
		private final int value;
		private Level(int value) {
			this.value = value;
		}
		int value() {
			return value;
		}
		private static Map<Integer,Level> levels;
		static Level from(int value) {
			if (levels == null) {
				levels = new HashMap<Integer,Level>();
				for (Level l : values())
					levels.put(l.value(), l);
			}
			return levels.get(value);
		}
	}
}
