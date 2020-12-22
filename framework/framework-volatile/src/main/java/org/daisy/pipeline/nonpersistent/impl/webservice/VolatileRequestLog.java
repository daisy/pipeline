package org.daisy.pipeline.nonpersistent.impl.webservice;

import org.daisy.pipeline.clients.RequestLog;
import org.daisy.pipeline.clients.RequestLogEntry;
/**
 * In dbless mode no request log is stored whatsoever */
public class VolatileRequestLog implements RequestLog {

	@Override
	public boolean contains(RequestLogEntry entry) {
		return false;
	}

	@Override
	public void add(RequestLogEntry entry) {
	}

}
