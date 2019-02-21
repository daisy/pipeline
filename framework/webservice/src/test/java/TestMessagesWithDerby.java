import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Optional;

import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobStatus;
import org.daisy.pipeline.webservice.jaxb.job.Messages;
import org.daisy.pipeline.webservice.jaxb.job.Messages.Message;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.request.Priority;

import org.junit.Assert;
import org.junit.Test;

public class TestMessagesWithDerby extends Base {
	
	// Test that progress messages work also with persistent storage
	@Test
	public void testProgressMessages() throws Exception {
		Optional<JobRequest> req = newJobRequest(client(), Priority.LOW, "mock-messages-script", getResource("hello.xml").toURI().toString());
		Assert.assertTrue("The request is present", req.isPresent());
		Job job = client().sendJob(req.get());
		deleteAfterTest(job);
		Callable<Job> poller = new JobPoller(client(), job.getId(), JobStatus.SUCCESS, 500, 10000) {
			BigDecimal lastProgress = BigDecimal.ZERO;
			Iterator<BigDecimal> mustSee = TestMessages.stream(".25", ".375", ".5", ".55", ".675", ".8", "1")
			                                           .map(d -> new BigDecimal(d)).iterator();
			BigDecimal mustSeeNext = mustSee.next();
			List<BigDecimal> seen = new ArrayList<BigDecimal>();
			@Override
			void performAction(Job job) {
				Optional<BigDecimal> progress = TestMessages.getProgress(job);
				if (progress.isPresent() && progress.get().compareTo(lastProgress) != 0) {
					Assert.assertTrue("Progress must be monotonic non-decreasing", progress.get().compareTo(lastProgress) >= 0);
					if (mustSeeNext != null) {
						if (progress.get().compareTo(mustSeeNext) == 0) {
							seen.clear();
							mustSeeNext = mustSee.hasNext() ? mustSee.next() : null;
						} else {
							seen.add(progress.get());
							Assert.assertTrue("Expected " + mustSeeNext + " but got " + seen, progress.get().compareTo(mustSeeNext) < 0);
						}
					}
					lastProgress = progress.get();
				}
				if (job.getStatus() == expectedStatus) {
					Assert.assertTrue("Expected " + mustSeeNext + " but got " + seen, mustSeeNext == null);
				}
			}
		};
		FutureTask<Job> t = new FutureTask<Job>(poller);
		t.run();
		job = t.get();
		// test that messages are nested
		Iterator<Message> messages = TestMessages.getMessage(
			TestMessages.assertPresent("messages element must exist", TestMessages.getMessages(job))).iterator();
		Message m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		TestMessages.assertPresent("message does not have children", TestMessages.next(TestMessages.getMessage(m).iterator()));
		// test that after cache has expired, messages are still available but flattened
		for (int i = 0; i < 6; i++) {
			Thread.sleep(1000);
			System.err.print(".");
			System.err.flush(); }
		System.err.println();
		job = client().job(job.getId());
		Messages messagesElem = TestMessages.assertPresent("messages element must exist", TestMessages.getMessages(job));
		TestMessages.assertEquals("total progress does not match", new BigDecimal("1"), messagesElem.getProgress());
		messages = TestMessages.getMessage(messagesElem).iterator();
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "a", m.getContent());
		Assert.assertFalse("message should not have children", TestMessages.next(TestMessages.getMessage(m).iterator()).isPresent());
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "b", m.getContent());
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "c", m.getContent());
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "d", m.getContent());
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "e", m.getContent());
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "f", m.getContent());
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "g", m.getContent());
		m = TestMessages.assertPresent("message does not exist", TestMessages.next(messages));
		Assert.assertEquals("message text does not match", "h", m.getContent());
		Assert.assertFalse(TestMessages.next(messages).isPresent());
	}
	
	@Override
	protected void setupClass() {
		super.setupClass();
		new File(PIPELINE_DATA, "log").mkdirs();
	}
	
	@Override
	protected Properties systemProperties() {
		Properties p = super.systemProperties();
		p.setProperty("org.daisy.pipeline.data", PIPELINE_DATA.getAbsolutePath());
		p.setProperty("derby.stream.error.file", new File(PIPELINE_DATA, "log/derby.log").getAbsolutePath());
		p.setProperty("org.daisy.pipeline.messaging.cache.buffer", "5");
		return p;
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[]{
			"org.daisy.pipeline:clientlib-java-jaxb:?",
			"org.daisy.pipeline:webservice-jaxb:?",
			"commons-codec:commons-codec:?",
			"commons-fileupload:commons-fileupload:?",
			"commons-io:commons-io:?",
			"org.daisy.libs:servlet-api:?",
			"org.daisy.pipeline:logging-activator:?",
			"org.restlet.osgi:org.restlet:?",
			"org.restlet.osgi:org.restlet.ext.fileupload:?",
			"org.restlet.osgi:org.restlet.ext.xml:?",
			"org.daisy.pipeline:common-utils:?",
			"org.daisy.pipeline:framework-core:?",
			"org.daisy.pipeline:xproc-api:?",
			"org.daisy.pipeline:webservice-utils:?",
			"org.daisy.pipeline:framework-persistence:?",
			"org.daisy.pipeline:persistence-derby:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline:push-notifier:?",
		};
	}
}
