package org.daisy.pipeline.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.sift.MDCBasedDiscriminator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.event.ProgressMessage;
import org.daisy.pipeline.event.ProgressMessageBuilder;

/**
 * Configure like this:
 *
 * &lt;appender name="EVENTBUS" class="org.daisy.pipeline.logging.EventBusAppender"&gt;
 *   &lt;filter class="org.daisy.pipeline.logging.ThresholdFilter"&gt;
 *     &lt;rootLevel&gt;INFO&lt;/rootLevel&gt;
 *     &lt;loggerLevels&gt;
 *       cz.vutbr.web=WARN
 *       org.daisy.common.xproc.calabash.steps.Message=WARN
 *       com.xmlcalabash.runtime.XAtomicStep=WARN
 *     &lt;/loggerLevels&gt;
 *   &lt;/filter&gt;
 * &lt;/appender&gt;
 */
public class EventBusAppender extends AppenderBase<ILoggingEvent> {
	
	// Note that EventBusProvider.asLogger can not be used here because the appender
	// is run in a separate thread. Using MDCBasedDiscriminator instead.
	private MDCBasedDiscriminator jobDiscriminator;
	private MDCBasedDiscriminator threadDiscriminator;
	
	@Override
	public void start() {
		jobDiscriminator = new MDCBasedDiscriminator();
		jobDiscriminator.setKey("jobid");
		jobDiscriminator.setDefaultValue("default");
		threadDiscriminator = new MDCBasedDiscriminator();
		threadDiscriminator.setKey("jobthread");
		threadDiscriminator.setDefaultValue("default");
		super.start();
	}
	
	protected void append(ILoggingEvent event) {
		if (!isStarted())
			return;
		String jobId = jobDiscriminator.getDiscriminatingValue(event);
		if (!"default".equals(jobId)) {
			String threadId = threadDiscriminator.getDiscriminatingValue(event);
			ProgressMessage activeBlock = ProgressMessage.getActiveBlock(jobId, threadId);
			if (activeBlock != null) {
				Level level = event.getLevel();
				activeBlock.post(
					new ProgressMessageBuilder()
						.withJobId(jobId)
						.withLevel(messageLevelFromLogbackLevel(level))
						.withText(event.getFormattedMessage())
				).close();
			}
		}
	}
	
	private static Message.Level messageLevelFromLogbackLevel(Level level) {
		switch(level.toInt()) {
		case Level.TRACE_INT:
			return Message.Level.TRACE;
		case Level.DEBUG_INT:
			return Message.Level.DEBUG;
		case Level.INFO_INT:
			return Message.Level.INFO;
		case Level.WARN_INT:
			return Message.Level.WARNING;
		case Level.ERROR_INT:
			return Message.Level.ERROR;
		case Level.ALL_INT:
		case Level.OFF_INT:
		default:
			return null; }
	}
}
