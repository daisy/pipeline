import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.apache.commons.io.FileUtils;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.junit.OSGiLessConfiguration;

import org.daisy.pipeline.webservice.jaxb.clients.Client;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobStatus;
import org.daisy.pipeline.webservice.jaxb.request.Input;
import org.daisy.pipeline.webservice.jaxb.request.Item;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.request.Priority;
import org.daisy.pipeline.webservice.jaxb.request.ObjectFactory;
import org.daisy.pipeline.webservice.jaxb.request.Script;
import org.daisy.pipeline.webservice.jaxb.script.Scripts;

import org.junit.After;
import org.junit.Before;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.PathUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// FIXME: If a server is already running (e.g. a remnent of a previous test) there is an error in
// the test.log file ("Shutting down the framework because of:Address already in use") but this is
// not visible in the maven log.

public abstract class Base extends AbstractTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[]{
			"org.daisy.pipeline:clientlib-java-jaxb:?",
			"org.daisy.pipeline:webservice-jaxb:?",
			"commons-codec:commons-codec:?",
			"commons-fileupload:commons-fileupload:?",
			"commons-io:commons-io:?",
			// for some reason logging-activator (now in common-utils) needs to start before restlet but after jersey-client (clientlib-java-jaxb)
			"org.daisy.pipeline:common-utils:?",
			"org.restlet.osgi:org.restlet:?",
			"org.restlet.osgi:org.restlet.ext.fileupload:?",
			"org.restlet.osgi:org.restlet.ext.xml:?",
			"org.daisy.pipeline:framework-core:?",
			"org.daisy.pipeline:xproc-api:?",
			"org.daisy.pipeline:calabash-adapter:?",
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		setupClass();
		return options(
			composite(super.config()),
			
			// for webservice-jaxb
			bootDelegationPackage("com.sun.xml.internal.bind"),
			
			// for TestPushNotifications
			systemPackage("com.sun.net.httpserver")
		);
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/mock-script.xml,"
		                                   + "OSGI-INF/sleep-step.xml");
		return probe;
	}
	
	private static final File PIPELINE_BASE = new File(new File(PathUtils.getBaseDir()), "target/tmp");
	protected static final File PIPELINE_DATA = new File(PIPELINE_BASE, "data");
	
	@OSGiLessConfiguration
	public void setupClass() {
		try {
			FileUtils.deleteDirectory(PIPELINE_BASE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	protected Properties systemProperties() {
		Properties p = new Properties();
		p.setProperty("org.daisy.pipeline.data", PIPELINE_DATA.getAbsolutePath());
		p.setProperty("org.daisy.pipeline.ws.authentication", "false");
		p.setProperty("org.daisy.pipeline.ws.localfs", "true");
		p.setProperty("org.daisy.pipeline.persistence", "false");
		return p;
	}
	
	private static final String DEFAULT_WS_URL = "http://localhost:8181/ws";
	private static final String DEFAULT_SCRIPT = "mock-script";
	private static final PipelineClient DEFAULT_CLIENT = newClient();
	
	protected static final Logger logger = LoggerFactory.getLogger(Base.class);
	
	protected PipelineClient client() {
		return DEFAULT_CLIENT;
	}
	
	private List<Job> jobsToDelete;
	private List<Client> clientsToDelete;
	
	@Before
	public void setupTest() throws Exception {
		jobsToDelete = Lists.newLinkedList();
		clientsToDelete = Lists.newLinkedList();
	}
	
	protected void deleteAfterTest(Object o) {
		if (o instanceof Job)
			jobsToDelete.add((Job)o);
		else if (o instanceof Client)
			clientsToDelete.add((Client)o);
		else
			throw new RuntimeException();
	}
	
	@After
	public void cleanUp() {
		logger.info("There are {} jobs to delete", jobsToDelete.size());
		for (Job j : jobsToDelete) {
			try {
				logger.info("Deleting job {}", j.getId());
				client().delete(j.getId());
			} catch (Exception e) {
				throw new RuntimeException("Error while deleting job", e);
			}
		}
		logger.info("There are {} jobs after the test", client().jobs().getJob().size());
		logger.info("There are {} clients to delete", clientsToDelete.size());
		for (Client c : clientsToDelete) {
			try {
				logger.info("Deleting client {}", c.getId());
				client().deleteClient(c.getId());
			} catch (Exception e) {
				throw new RuntimeException("Error while deleting client", e);
			}
		}
	}
	
	protected static PipelineClient newClient() {
		return new PipelineClient("http://localhost:8181/ws");
	}
	
	protected static PipelineClient newClient(String id, String secret) {
		return new PipelineClient(DEFAULT_WS_URL, id, secret);
	}
	
	protected Job waitForStatus(JobStatus status, Job job, long timeout) throws Exception {
		logger.info("Waiting for status {}", status);
		job = new JobPoller(client(), job.getId(), status, 500, timeout).call();
		logger.info("After status {}", job.getStatus().value());
		return job;
	}
	
	protected Optional<JobRequest> newJobRequest() {
		return newJobRequest(client());
	}
	
	protected static Optional<JobRequest> newJobRequest(PipelineClient client) {
		return newJobRequest(client, Priority.LOW);
	}
	
	protected Optional<JobRequest> newJobRequest(Priority priority) {
		return newJobRequest(client(), priority);
	}
	
	protected static Optional<JobRequest> newJobRequest(PipelineClient client, Priority priority) {
		return newJobRequest(client, priority, DEFAULT_SCRIPT, getResource("hello.xml").toURI().toString());
	}
	
	protected Optional<JobRequest> newJobRequest(Priority priority, String sourcePath) {
		return newJobRequest(client(), priority, DEFAULT_SCRIPT, sourcePath);
	}
	
	protected static Optional<JobRequest> newJobRequest(PipelineClient client, Priority priority, String scriptId, String sourcePath) {
		ObjectFactory reqFactory = new ObjectFactory();
		JobRequest req = reqFactory.createJobRequest();
		Script script = reqFactory.createScript();
		Optional<String> href = getScriptHref(client, scriptId);
		if (!href.isPresent()) {
			return Optional.absent();
		}
		script.setHref(href.get());
		Item source = reqFactory.createItem();
		source.setValue(sourcePath);
		Input input = reqFactory.createInput();
		input.getItem().add(source);
		input.setName("source");
		req.getScriptOrNicenameOrPriority().add(script);
		req.getScriptOrNicenameOrPriority().add(reqFactory.createNicename("NICE_NAME"));
		req.getScriptOrNicenameOrPriority().add(input);
		req.getScriptOrNicenameOrPriority().add(reqFactory.createPriority(priority));
		return Optional.of(req);
	}
	
	protected Optional<String> getScriptHref(String name) {
		return getScriptHref(client(), name);
	}
	
	private static Optional<String> getScriptHref(PipelineClient client, String name) {
		Scripts scripts = client.scripts();
		for (org.daisy.pipeline.webservice.jaxb.script.Script s : scripts.getScript()) {
			if (s.getId().equals(name)){
				return Optional.of(s.getHref());
			}
		}
		return Optional.absent();
	}
	
	protected static File jobPath(String jobId) {
		return new File(new File(PIPELINE_DATA, "jobs"), jobId);
	}
	
	protected static File logPath(String jobId) {
		return new File(new File(new File(PIPELINE_DATA, "jobs"), jobId), jobId + ".log");
	}
	
	protected static File getResource(String path) {
		return new File(new File(PathUtils.getBaseDir(), "src/test/resources"), path);
	}
	
	protected static InputStream getResourceAsStream(String path) throws IOException {
		return new File(new File(PathUtils.getBaseDir(), "src/test/resources"), path).toURI().toURL().openStream();
	}
	
	static class JobPoller implements Callable<Job> {
		final PipelineClient client;
		final String jobId;
		final JobStatus expectedStatus;
		final long interval;
		final long timeout;
		JobPoller(PipelineClient client, String jobId, JobStatus expectedStatus, long interval, long timeout) {
			this.client = client;
			this.jobId = jobId;
			this.expectedStatus = expectedStatus;
			this.interval = interval;
			this.timeout = timeout;
		}
		void performAction(Job job) {}
		public Job call() throws Exception {
			long waited = 0L;
			while (true) {
				Job job = client.job(jobId);
				JobStatus status = job.getStatus();
				if (status == expectedStatus) {
					performAction(job);
					return job;
				} else if (status == JobStatus.ERROR) {
					throw new RuntimeException("Job errored while waiting for another status");
				}
				try {
					performAction(job);
					Thread.sleep(interval);
					waited += interval;
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				if (waited > timeout) {
					throw new RuntimeException("waitForStatus " + expectedStatus + " timed out (last status was " + status + ")");
				}
			}
		}
	}
}
