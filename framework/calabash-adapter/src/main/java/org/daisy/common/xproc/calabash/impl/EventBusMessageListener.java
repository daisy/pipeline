package org.daisy.common.xproc.calabash.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.event.ProgressMessage;
import org.daisy.pipeline.event.ProgressMessageBuilder;

import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRunnable;


/**
 * Wrapps the org.daisy.common.messaging.MessageListener to a
 * XProcMessageListener to be plugged in calabash
 */
public class EventBusMessageListener implements XProcMessageListener {

	private static boolean LOG_DEBUG = Boolean.parseBoolean(
		org.daisy.pipeline.properties.Properties.getProperty("org.daisy.pipeline.calabash.logDebug","false"));
	
	/** The listener. */
	EventBusProvider eventBus;
	private final String jobId;

	public EventBusMessageListener(EventBusProvider eventBus, String jobId) {
		super();
		this.eventBus = eventBus;
		this.jobId = jobId;
	}

	private ProgressMessage post(ProgressMessageBuilder builder) {
		ProgressMessage activeBlock = ProgressMessage.getActiveBlock(jobId, null);
		return activeBlock != null
			? activeBlock.post(builder)
			: eventBus.post(builder);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.xmlcalabash.core.XProcMessageListener#error(java.lang.Throwable)
	 */
	@Override
	public void error(Throwable exception) {
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.ERROR);
		XprocMessageHelper.errorMessage(exception, builder);
		post(builder).close();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#error(com.xmlcalabash.core.
	 * XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String,
	 * net.sf.saxon.s9api.QName)
	 */
	@Override
	public void error(XProcRunnable step, XdmNode node, String message,
			QName qName) {
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.ERROR);
		builder = XprocMessageHelper.message(step, node, message, builder);
		post(builder).close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.xmlcalabash.core.XProcMessageListener#fine(com.xmlcalabash.core.
	 * XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void fine(XProcRunnable step, XdmNode node, String message) {
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.DEBUG);
                builder = XprocMessageHelper.message(step, node, message, builder);
                if (LOG_DEBUG){
                        post(builder).close();
                }
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#finer(com.xmlcalabash.core.
	 * XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void finer(XProcRunnable step, XdmNode node, String message) {
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.TRACE);
		builder = XprocMessageHelper.message(step, node, message, builder);
                if (LOG_DEBUG){
                        post(builder).close();
                }

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#finest(com.xmlcalabash.core
	 * .XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void finest(XProcRunnable step, XdmNode node, String message) {
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.TRACE);
		builder = XprocMessageHelper.message(step, node, message, builder);
                if (LOG_DEBUG){
                        post(builder).close();
                }

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.xmlcalabash.core.XProcMessageListener#info(com.xmlcalabash.core.
	 * XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void info(XProcRunnable step, XdmNode node, String message) {
          
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.INFO);
		builder = XprocMessageHelper.message(step, node, message, builder);
		post(builder).close();
                

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#warning(com.xmlcalabash.core
	 * .XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void warning(XProcRunnable step, XdmNode node, String message) {
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.WARNING);
		builder = XprocMessageHelper.message(step, node, message, builder);
		post(builder).close();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#warning(java.lang.Throwable)
	 */
	@Override
	public void warning(Throwable exception) {
		ProgressMessageBuilder builder = createMessageBuilder()
				.withLevel(Level.WARNING);
		XprocMessageHelper.errorMessage(exception, builder);
		post(builder).close();

	}

	@Override
	public void openStep(XProcRunnable step, XdmNode node, String message, String level, BigDecimal portion) {
		ProgressMessageBuilder builder = createMessageBuilder().withProgress(portion);
		if (level == null || level.equals("INFO")) {
			builder.withLevel(Level.INFO);
		} else if (level.equals("ERROR")) {
			builder.withLevel(Level.ERROR);
		} else if (level.equals("WARN")) {
			builder.withLevel(Level.WARNING);
		} else if (level.equals("DEBUG")) {
			builder.withLevel(Level.DEBUG);
		} else if (level.equals("TRACE")) {
			builder.withLevel(Level.TRACE);
		} else {
			builder.withLevel(Level.INFO);
			message = "Message with invalid level '" + level + "': " + message;
		}
		XprocMessageHelper.message(step, node, message, builder);
		post(builder);
	}

	@Override
	public void closeStep() {
		ProgressMessage m = ProgressMessage.getActiveBlock(jobId, null);
		if (m == null)
			throw new RuntimeException("coding error");
		else
			m.close();
	}

	private ProgressMessageBuilder createMessageBuilder() {
		return new ProgressMessageBuilder().withJobId(jobId);
	}
}
