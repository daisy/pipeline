import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.inject.Inject;
import javax.xml.transform.Result;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

import org.apache.commons.io.FileUtils;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.junit.OSGiLessConfiguration;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScriptService;
import org.daisy.pipeline.webserviceutils.storage.WebserviceStorage;

import org.junit.Assert;
import org.junit.Test;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.PathUtils;

import org.slf4j.LoggerFactory;

public class FrameworkCoreWithDerbyTest extends AbstractTest {
	
	@Inject
	public JobManagerFactory jobManagerFactory;
	
	@Inject
	public WebserviceStorage webserviceStorage;
	
	@Inject
	public ScriptRegistry scriptRegistry;
	
	@Inject
	public EventBusProvider eventBusProvider;
	
	@Inject
	public JobMonitorFactory jobMonitorFactory;
	
	// Test that progress messages work also with persistent storage
	@Test
	public void testProgressMessages() throws InterruptedException, ExecutionException {
		Logger logger = (Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		FrameworkCoreTest.CollectLogMessages collectLog = new FrameworkCoreTest.CollectLogMessages(logger.getLoggerContext(), Level.ERROR);
		logger.addAppender(collectLog);
		try {
			Job job = newJob("progress-messages",
			                 new XProcInput.Builder().build(),
			                 // can not use StreamResult because framework would not be able to persist and raise error
			                 new XProcOutput.Builder().withOutput("result",
			                                                      () -> {
			                                                          try {
			                                                              File res = File.createTempFile("res", null, PIPELINE_BASE);
			                                                              logger.debug("Storing result to " + res);
			                                                              return new Result() {
			                                                                  public String getSystemId() {
			                                                                      return res.toURI().toString(); }
			                                                                  public void setSystemId(String id) {
			                                                                      throw new UnsupportedOperationException(); }}; }
			                                                          catch (IOException e) {
			                                                              throw new RuntimeException(e); }})
			                                          .build());
			JobMonitor monitor = jobMonitorFactory.newJobMonitor(job.getId(), true);
			final MessageAccessor accessor = monitor.getMessageAccessor();
			Runnable poller = new FrameworkCoreTest.JobPoller(job, Job.Status.SUCCESS, 200, 3000) {
				BigDecimal lastProgress = BigDecimal.ZERO;
				Iterator<BigDecimal> mustSee = FrameworkCoreTest.stream(".125", ".375", ".9").map(d -> new BigDecimal(d)).iterator();
				BigDecimal mustSeeNext = mustSee.next();
				List<BigDecimal> seen = new ArrayList<BigDecimal>();
				@Override
				void performAction(Job.Status status) {
					BigDecimal progress = accessor.getProgress();
					if (progress.compareTo(lastProgress) != 0) {
						Assert.assertTrue("Progress must be monotonic non-decreasing", progress.compareTo(lastProgress) >= 0);
						if (mustSeeNext != null) {
							if (progress.compareTo(mustSeeNext) == 0) {
								seen.clear();
								mustSeeNext = mustSee.hasNext() ? mustSee.next() : null;
							} else {
								seen.add(progress);
								Assert.assertTrue("Expected " + mustSeeNext + " but got " + seen, progress.compareTo(mustSeeNext) < 0);
							}
						}
						lastProgress = progress;
					}
					if (status == expectedStatus) {
						Assert.assertTrue("Expected " + mustSeeNext + " but got " + seen, mustSeeNext == null);
					}
				}
			};
			Iterator<Message> messages = null;
			try {
				// run in new thread and propagate exceptions:
				try {
					FutureTask<Boolean> t = new FutureTask<Boolean>(poller, true);
					t.run();
					t.get();
				} catch (ExecutionException e) {
					if (e.getCause() instanceof AssertionError)
						throw (AssertionError)e.getCause();
					else if (e.getCause() instanceof RuntimeException)
						throw (RuntimeException)e.getCause();
					else
						throw e;
				} finally {
					messages = FrameworkCoreTest.printMessages(accessor.getAll().iterator());
				}
				Iterator<ILoggingEvent> log = collectLog.get();
				Assert.assertFalse(log.hasNext());
			} catch (Throwable e) {
				// print remainging messages
				FrameworkCoreTest.sink(messages);
				throw e;
			}
		} finally {
			logger.detachAppender(collectLog);
		}
	}
	
	Job newJob(String scriptId, XProcInput input, XProcOutput output) {
		Client client = webserviceStorage.getClientStorage().defaultClient();
		JobManager jobManager = jobManagerFactory.createFor(client);
		XProcScriptService script = scriptRegistry.getScript(scriptId);
		Assert.assertNotNull("The " + scriptId + " script should exist", script);
		return jobManager.newJob(BoundXProcScript.from(script.load(), input, output))
		                 .isMapping(true)
		                 .withNiceName("nice")
		                 .build()
		                 .get();
	}
	
	@Override
	public String[] testDependencies() {
		return new String[]{
			"com.google.guava:guava:?",
			"org.daisy.libs:com.xmlcalabash:?",
			"org.daisy.libs:saxon-he:?",
			"org.slf4j:slf4j-api:?",
			"org.daisy.pipeline:common-utils:?",
			"org.daisy.pipeline:framework-core:?",
			"org.daisy.pipeline:saxon-adapter:?",
			"org.daisy.pipeline:xproc-api:?",
			"org.daisy.pipeline:modules-registry:?",
			"org.apache.httpcomponents:httpclient-osgi:?",
			"org.apache.httpcomponents:httpcore-osgi:?",
			"org.daisy.libs:jing:?",
			"org.daisy.pipeline:framework-persistence:?",
			"org.daisy.pipeline:persistence-derby:?",
			"org.daisy.pipeline:logging-appender:?"
		};
	}
	
	static final File PIPELINE_BASE = new File(new File(PathUtils.getBaseDir()), "target/tmp");
	static final File PIPELINE_DATA = new File(PIPELINE_BASE, "data");
	
	@Override
	protected Properties systemProperties() {
		Properties p = new Properties();
		p.setProperty("org.daisy.pipeline.iobase", new File(PIPELINE_DATA, "jobs").getAbsolutePath());
		p.setProperty("org.daisy.pipeline.data", PIPELINE_DATA.getAbsolutePath());
		p.setProperty("org.daisy.pipeline.persistence", "true");
		p.setProperty("derby.stream.error.file", new File(PIPELINE_DATA, "log/derby.log").getAbsolutePath());
		return p;
	}

	@OSGiLessConfiguration
	public void setup() {
		try {
			FileUtils.deleteDirectory(PIPELINE_BASE);
			new File(PIPELINE_DATA, "log").mkdirs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override @Configuration
	public Option[] config() {
		setup();
		return super.config();
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Bundle-Name", "Test module");
		probe.setHeader("Service-Component", "OSGI-INF/script.xml,"
		                                   + "OSGI-INF/java-step.xml,"
		                                   + "OSGI-INF/java-function.xml");
		return probe;
	}
}
