package org.daisy.common.xproc.calabash.impl;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRunnable;
import com.xmlcalabash.runtime.XStep;

/**
 * XProcMessageListener that appends messages to a {@link MessageAppender}.
 */
public class MessageListenerImpl implements XProcMessageListener {

	private final boolean autoNameSteps;
	private final MessageAppender rootAppender;
	private final Deque<MessageAppender> messageStack = new ArrayDeque<>();

	/**
	 * @param autoNameSteps Set this property to automatically add a message to all steps with
	 *        progress information but no message (only for debugging)
	 */
	public MessageListenerImpl(MessageAppender rootAppender, boolean autoNameSteps) {
		super();
		this.rootAppender = rootAppender;
		this.autoNameSteps = autoNameSteps;
	}

	private MessageAppender append(MessageBuilder builder, boolean close) {
		MessageAppender current = messageStack.isEmpty() ? rootAppender : messageStack.peek();
		current = current.append(builder);
		if (!close)
			messageStack.push(current);
		else
			current.close();
		return current;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.xmlcalabash.core.XProcMessageListener#error(java.lang.Throwable)
	 */
	@Override
	public void error(Throwable exception) {
		MessageBuilder builder = new MessageBuilder()
				.withLevel(Level.ERROR);
		XprocMessageHelper.errorMessage(exception, builder);
		append(builder, true);
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
	public void error(XProcRunnable step, XProcException error) {
		MessageBuilder builder = new MessageBuilder().withLevel(Level.ERROR);
		builder = XprocMessageHelper.message(step, error.getLocation()[0], error.getMessage(), builder);
		append(builder, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.xmlcalabash.core.XProcMessageListener#fine(com.xmlcalabash.core.
	 * XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void fine(XProcRunnable step, XdmNode location, String message) {
		MessageBuilder builder = new MessageBuilder().withLevel(Level.DEBUG);
		builder = XprocMessageHelper.message(step, location, message, builder);
		append(builder, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#finer(com.xmlcalabash.core.
	 * XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void finer(XProcRunnable step, XdmNode location, String message) {
		MessageBuilder builder = new MessageBuilder().withLevel(Level.TRACE);
		builder = XprocMessageHelper.message(step, location, message, builder);
		append(builder, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#finest(com.xmlcalabash.core
	 * .XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void finest(XProcRunnable step, XdmNode location, String message) {
		MessageBuilder builder = new MessageBuilder().withLevel(Level.TRACE);
		builder = XprocMessageHelper.message(step, location, message, builder);
		append(builder, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.xmlcalabash.core.XProcMessageListener#info(com.xmlcalabash.core.
	 * XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void info(XProcRunnable step, XdmNode location, String message) {
		MessageBuilder builder = new MessageBuilder().withLevel(Level.INFO);
		builder = XprocMessageHelper.message(step, location, message, builder);
		append(builder, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#warning(com.xmlcalabash.core
	 * .XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void warning(XProcRunnable step, XdmNode location, String message) {
		MessageBuilder builder = new MessageBuilder().withLevel(Level.WARNING);
		builder = XprocMessageHelper.message(step, location, message, builder);
		append(builder, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.xmlcalabash.core.XProcMessageListener#warning(java.lang.Throwable)
	 */
	@Override
	public void warning(Throwable exception) {
		MessageBuilder builder = new MessageBuilder().withLevel(Level.WARNING);
		XprocMessageHelper.errorMessage(exception, builder);
		append(builder, true);
	}

	@Override
	public void openStep(XProcRunnable step, XdmNode location, String message, String level, BigDecimal portion) {
		MessageBuilder builder = new MessageBuilder().withProgress(portion);
		if (message == null && autoNameSteps && portion != null && portion.compareTo(BigDecimal.ZERO) > 0) {
			// FIXME: not if there is a an ancestor block with portion 0!
			// how to test this?
			// -> activeBlock.getPortion() returns portion as defined in XPL and no access to parents
			// -> only check parent and change portion to 0 if parent has portion 0 because irrelevant anyway
			if (step instanceof XStep)
				message = ((XStep)step).getStep().getName();
			if (level == null)
				level = "DEBUG";
		}
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
		XprocMessageHelper.message(step, location, message, builder);
		append(builder, false);
	}

	@Override
	public void closeStep() {
		if (messageStack.isEmpty())
			throw new RuntimeException("coding error");
		messageStack.pop().close();
	}

	/**
	 * Close any open message blocks.
	 */
	void clean() {
		while (!messageStack.isEmpty())
			messageStack.pop().close();
	}
}
