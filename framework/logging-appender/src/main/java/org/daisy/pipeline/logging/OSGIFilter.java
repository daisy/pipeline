package org.daisy.pipeline.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class OSGIFilter extends Filter<ILoggingEvent> {
	private static final String OSGI = "OSGI";

	Level level = null;

	@Override
	public FilterReply decide(ILoggingEvent event) {
		// if it's osgi filter everything below "level"
		if (event.getMarker() != null
				&& (event.getMarker().toString().equals(OSGI) && event.getLevel().toInt() < level.toInt())) {
			return FilterReply.DENY;
		} else {
			return FilterReply.NEUTRAL;
		}
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public void start() {
		if (level == null)
			level = Level.WARN;
		super.start();
	}
}
