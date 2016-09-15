package net.kornr.log;

import java.util.HashMap;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.LoggerFactory;

/**
 * The LogbackAdaptor converts the LogEntry objects it receives into calls to the slf4j 
 * loggers (the native interface of logback).
 * 
 * @author Rodrigo Reyes
 *
 */
public class LogbackAdaptor implements LogListener
{
        private static final Marker OSGI_MARKER=MarkerFactory.getMarker("OSGI");
	HashMap<Long, org.slf4j.Logger> m_loggers = new HashMap<Long, org.slf4j.Logger>(); 

	/**
	 * This methods is called by the LogReaderService, and dispatch them to 
	 * a set of Loggers, created with 
	 */
	public void logged(LogEntry log) 
	{
		if ((log.getBundle() == null) || (log.getBundle().getSymbolicName() == null))
		{
			// if there is no name, it's probably the framework emitting a log
			// This should not happen and we don't want to log something anonymous
			return; 
		}

		// Retrieve a Logger object, or create it if none exists.
		Logger logger = m_loggers.get(log.getBundle().getBundleId());
		if (logger == null)
		{
			logger = LoggerFactory.getLogger(log.getBundle().getSymbolicName());
			m_loggers.put(log.getBundle().getBundleId(), logger);
		}

		// If there is an exception available, use it, otherwise just log 
		// the message
		if (log.getException() != null)
		{
			switch(log.getLevel())
			{
			case LogService.LOG_DEBUG:
				logger.debug(OSGI_MARKER,log.getMessage(), log.getException());
				break;
			case LogService.LOG_INFO:
				logger.info(OSGI_MARKER,log.getMessage(), log.getException());
				break;
			case LogService.LOG_WARNING:
				logger.warn(OSGI_MARKER,log.getMessage(), log.getException());
				break;
			case LogService.LOG_ERROR:
				logger.error(OSGI_MARKER,log.getMessage(), log.getException());
				break;
			}
		}
		else
		{
			switch(log.getLevel())
			{
			case LogService.LOG_DEBUG:
				logger.debug(OSGI_MARKER,log.getMessage());
				break;
			case LogService.LOG_INFO:
				logger.info(OSGI_MARKER,log.getMessage());
				break;
			case LogService.LOG_WARNING:
				logger.warn(OSGI_MARKER,log.getMessage());
				break;
			case LogService.LOG_ERROR:
				logger.error(OSGI_MARKER,log.getMessage());
				break;
			}			
		}

	}
}
