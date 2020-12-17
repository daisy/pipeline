package org.daisy.maven.xproc.pipeline;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.ProgressMessage;
import org.daisy.common.properties.Properties;
import org.daisy.maven.xproc.pipeline.logging.FlattenedProgressMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MessageEventListener {
	
	final MessageAccessor messages;
	
	MessageEventListener(MessageAccessor messages) {
		this.messages = messages;
		messages.listen(this::update);
	}
	
	void close() {
		messages.unlisten(this::update);
	}
	
	// notify of message updates
	private void update(Integer sequenceNumber) {
		if (sequenceNumber != null) {
			flattenMessages(messages.createFilter()
			                        .filterLevels(levels)
			                        .inRange(sequenceNumber,sequenceNumber)
			                        .getMessages().iterator(),
			                sequenceNumber,
			                0,
			                null,
			                messages.getProgress());
		}
	}
	
	private void flattenMessages(Iterator<? extends Message> messages, int firstSeq, int depth, Integer parent, BigDecimal progress) {
		while (messages.hasNext()) {
			Message m = messages.next();
			if (m.getSequence() >= firstSeq && m.getText() != null)
				logMessage(new FlattenedProgressMessage(m.getText(), m.getSequence(), parent, depth, progress), m.getLevel());
			if (m instanceof ProgressMessage)
				flattenMessages(((ProgressMessage)m).iterator(), firstSeq, depth + 1, m.getSequence(), progress);
		}
	}
	
	private void logMessage(Object m, Message.Level level) {
		if (level == null)
			level = Message.Level.INFO;
		switch (level) {
		case TRACE:
			logger.trace("{}", m);
			break;
		case DEBUG:
			logger.debug("{}", m);
			break;
		case INFO:
			logger.info("{}", m);
			break;
		case WARNING:
			logger.warn("{}", m);
			break;
		case ERROR:
			logger.error("{}", m);
			break; }
	}
	
	private static final Logger logger = LoggerFactory.getLogger(MessageEventListener.class);
	private static final Set<Level> levels = new HashSet<>();
	
	static {
		if (logger.isErrorEnabled())
			levels.add(Level.ERROR);
		if (logger.isWarnEnabled())
			levels.add(Level.WARNING);
		if (logger.isInfoEnabled())
			levels.add(Level.INFO);
		if (logger.isDebugEnabled())
			levels.add(Level.DEBUG);
		if (logger.isTraceEnabled())
			levels.add(Level.TRACE);
		
		// If the org.daisy.pipeline.log.level property was not set explicitly, set it to the same
		// value as the SLF4J level of org.daisy.maven.xproc.pipeline.MessageEventListener
		if (Properties.getProperty("org.daisy.pipeline.log.level") == null) {
			if (logger.isTraceEnabled())
				System.getProperties().setProperty("org.daisy.pipeline.log.level", "TRACE");
			else if (logger.isDebugEnabled())
				System.getProperties().setProperty("org.daisy.pipeline.log.level", "DEBUG");
			else if (logger.isInfoEnabled())
				System.getProperties().setProperty("org.daisy.pipeline.log.level", "INFO");
			else if (logger.isWarnEnabled())
				System.getProperties().setProperty("org.daisy.pipeline.log.level", "WARN");
			else if (logger.isErrorEnabled())
				System.getProperties().setProperty("org.daisy.pipeline.log.level", "ERROR");
		}
	}
}
