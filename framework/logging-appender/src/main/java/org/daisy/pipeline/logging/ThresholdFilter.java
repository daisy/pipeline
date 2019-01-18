package org.daisy.pipeline.logging;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import ch.qos.logback.classic.Level;
import static ch.qos.logback.classic.Level.toLevel;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class ThresholdFilter extends Filter<ILoggingEvent> {
	
	private Level rootLevel;
	private Map<String,Level> loggerLevels;
	private Map<String,Level> cache;
	
	public void setRootLevel(Level rootLevel) {
		this.rootLevel = rootLevel;
	}
	
	public void setLoggerLevels(String loggerLevels) {
		this.loggerLevels = new TreeMap<String,Level>();
		try {
			Properties p = new Properties();
			p.load(new StringReader(loggerLevels));
			for (String logger : p.stringPropertyNames())
				this.loggerLevels.put(logger, toLevel(p.getProperty(logger))); }
		catch (IOException e) {
			throw new RuntimeException(e); }
	}
	
	@Override
	public void start() {
		if (rootLevel == null)
			rootLevel = Level.ALL;
		cache = new TreeMap<String,Level>();
		super.start();
	}

	@Override
	public FilterReply decide(ILoggingEvent event) {
		if (!isStarted())
			return FilterReply.NEUTRAL;
		String logger = event.getLoggerName();
		Level threshold = cache.get(logger);
		if (threshold == null) {
			threshold = rootLevel; {
				if (loggerLevels != null)
					for (String l : loggerLevels.keySet())
						if (logger.startsWith(l)) {
							threshold = loggerLevels.get(l);
							break; }}
			cache.put(logger, threshold); }
		Level level = event.getLevel();
		if (level.isGreaterOrEqual(threshold))
			return FilterReply.NEUTRAL;
		else
			return FilterReply.DENY;
	}
}
