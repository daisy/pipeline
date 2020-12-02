package org.daisy.pipeline.logging;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.sift.MDCBasedDiscriminator;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.joran.event.SaxEvent;
import ch.qos.logback.core.joran.spi.RuleStore;
import ch.qos.logback.core.sift.AppenderFactoryBase;
import ch.qos.logback.core.sift.SiftingJoranConfiguratorBase;
import ch.qos.logback.core.FileAppender;

import org.daisy.pipeline.job.JobURIUtils;

/**
 * Append to the detailed job log.
 *
 * Configure like this:
 *
 * &lt;appender name="JOB_FILE" class="org.daisy.pipeline.logging.JobLogFileAppender"&gt;
 *   &lt;encoder&gt;
 *     &lt;Pattern&gt;%date [%-5level] %logger{36} - %msg%n&lt;/Pattern&gt;
 *   &lt;/encoder&gt;
 * &lt;/appender&gt;
 */
public class JobLogFileAppender extends SiftingAppender {

	private MDCBasedDiscriminator jobDiscriminator;
	private Encoder<ILoggingEvent> encoder;
	private final static String DEFAULT_ENCODER_PATTERN = "%date [%-5level] %logger{36} - %msg%n";

	public void setEncoder(Encoder<ILoggingEvent> encoder) {
		this.encoder = encoder;
	}

	@Override
	public void start() {
		if (encoder != null) {
			if (!(encoder instanceof PatternLayoutEncoder))
				throw new RuntimeException(
					"encoder is expected to be of type PatternLayoutEncoder but got " + encoder.getClass());
		}
		jobDiscriminator = new MDCBasedDiscriminator();
		jobDiscriminator.setKey("jobid");
		jobDiscriminator.setDefaultValue("default");
		jobDiscriminator.start();
		setDiscriminator(jobDiscriminator);
		Map<String,Appender<ILoggingEvent>> appenders = new HashMap<>();
		setAppenderFactory(
			new AppenderFactoryBase<ILoggingEvent>(
				// simulate an empty <sift/> element
				Arrays.asList(new SaxEvent[]{null, null})) {
				public SiftingJoranConfiguratorBase<ILoggingEvent> getSiftingJoranConfigurator(
						String jobId) {
					return new SiftingJoranConfiguratorBase<ILoggingEvent>() {
						protected void addInstanceRules(RuleStore rs) {}
						public Appender<ILoggingEvent> getAppender() {
							if (appenders.containsKey(jobId))
								return appenders.get(jobId);
							else {
								FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>() {{
										this.name = "FILE-" + jobId;
										// FIXME: We know that the log file is stored in this location because
										// AbstractJobContext also calls JobURIUtils.getLogFile(). Still, it would be
										// nicer to use JobContext.getLogFile(). The problem with this however is that
										// we can not bind the JobManager OSGi service because we're in a fragment (not
										// a bundle).
										this.fileName = JobURIUtils.getLogFile(jobId).getAbsolutePath();
										this.append = false;
										this.encoder = new PatternLayoutEncoder() {{
											if (JobLogFileAppender.this.encoder != null) {
												PatternLayoutEncoder enc = (PatternLayoutEncoder)JobLogFileAppender.this.encoder;
												setPattern(enc.getPattern());
												outputPatternAsHeader = enc.isOutputPatternAsHeader();
												setCharset(enc.getCharset());
												setImmediateFlush(enc.isImmediateFlush());
											} else
												setPattern(DEFAULT_ENCODER_PATTERN);
										}};
									}
										@Override
										public void start() {
											encoder.start();
											super.start();
										}
										@Override
										public void stop() {
											encoder.stop();
											super.stop();
											appenders.remove(jobId);
										}
										@Override
										public void setContext(Context context) {
											encoder.setContext(context);
											super.setContext(context);
										}
									};
								fileAppender.setContext(getContext());
								fileAppender.start();
								appenders.put(jobId, fileAppender);
								return fileAppender;
							}
						}
					};
				}
			}
		);
		super.start();
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (!isStarted()) {
			return;
		}
		String jobId = jobDiscriminator.getDiscriminatingValue(event);
		if (!"default".equals(jobId)) {
			super.append(event);
			// Force closing the logging file when we finish the job
			if (event.getMarker() == ClassicConstants.FINALIZE_SESSION_MARKER) {
				super.stop();
			}
		}
	}
}
