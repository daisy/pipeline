package org.daisy.pipeline.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class OSGIFilter extends Filter<ILoggingEvent> {
	private static final String OSGI = "OSGI";

	@Override
	public FilterReply decide(ILoggingEvent event) {
		// if it's osgi filter everything below WARN
		if (event.getMarker() != null
				&& (event.getMarker().toString().equals(OSGI) && event.getLevel().toInt() < Level.WARN_INT)) {
			return FilterReply.DENY;
		} else {
			return FilterReply.NEUTRAL;
		}
	}
}
