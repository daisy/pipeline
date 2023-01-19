import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobStatus;
import org.daisy.pipeline.webservice.jaxb.job.Messages;
import org.daisy.pipeline.webservice.jaxb.request.Callback;
import org.daisy.pipeline.webservice.jaxb.request.CallbackType;
import org.daisy.pipeline.webservice.jaxb.request.Input;
import org.daisy.pipeline.webservice.jaxb.request.Item;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.request.ObjectFactory;
import org.daisy.pipeline.webservice.jaxb.request.Script;
import org.daisy.pipeline.webservice.jaxb.request.Priority;

import org.junit.Assert;
import org.junit.Test;

public class TestPushNotifications extends Base {
	
	private static final PipelineClient client = newClient(TestClientJobs.CREDS_DEF.clientId, TestClientJobs.CREDS_DEF.secret);
	
	@Override
	protected PipelineClient client() {
		return client;
	}
	
	@Override
	protected Properties systemProperties() {
		Properties p = super.systemProperties();
		// client authentication is required for push notifications
		p.setProperty("org.daisy.pipeline.ws.authentication", "true");
		p.setProperty("org.daisy.pipeline.ws.authentication.key", TestClientJobs.CREDS_DEF.clientId);
		p.setProperty("org.daisy.pipeline.ws.authentication.secret", TestClientJobs.CREDS_DEF.secret);
		return p;
	}
	
	@Test
	public void testPushNotifications() throws Exception {
		AbstractCallback testStatusAndMessages = new AbstractCallback() {
			JobStatus lastStatus = null;
			BigDecimal lastProgress = BigDecimal.ZERO;
			// Note that we do not see "1" because the last step has no message or progress so we
			// are not notified when it is opened or closed. If we would be notified when the last
			// step is closed, we would see "1.0" and not again ".9" because by the time the
			// notification is sent the job has already finished.
			Iterator<BigDecimal> mustSee = stream(".25", ".375", ".5", ".55", ".675", ".8", ".9").map(BigDecimal::new).iterator();
			BigDecimal mustSeeNext = mustSee.next();
			List<BigDecimal> seen = new ArrayList<BigDecimal>();
			@Override
			void handleStatus(JobStatus status) {
				lastStatus = status;
			}
			@Override
			void handleMessages(Messages messages) {
				BigDecimal progress = messages.getProgress();
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
			}
			@Override
			void finalTest() {
				Assert.assertEquals(JobStatus.SUCCESS, lastStatus);
				Assert.assertTrue("Expected " + mustSeeNext + " but got " + seen, mustSeeNext == null);
			}
		};
		HttpServer server; {
			server = HttpServer.create(new InetSocketAddress(8080), 0);
			server.createContext("/notify", testStatusAndMessages);
			server.setExecutor(null);
			server.start();
		}
		try {
			JobRequest req; {
				ObjectFactory oFactory = new ObjectFactory();
				req = oFactory.createJobRequest();
				Script script = oFactory.createScript(); {
					Optional<String> href = getScriptHref("mock-messages-script");
					Assert.assertTrue(href.isPresent());
					script.setHref(href.get());
				}
				req.getScriptOrNicenameOrPriority().add(script);
				Input input = oFactory.createInput(); {
					Item source = oFactory.createItem();
					source.setValue(getResource("hello.xml").toURI().toString());
					input.getItem().add(source);
					input.setName("source");
				}
				req.getScriptOrNicenameOrPriority().add(input);
				req.getScriptOrNicenameOrPriority().add(oFactory.createNicename("NICE_NAME"));
				req.getScriptOrNicenameOrPriority().add(oFactory.createPriority(Priority.LOW));
				Callback callback = oFactory.createCallback(); {
					callback.setType(CallbackType.MESSAGES);
					callback.setHref("http://localhost:8080/notify");
					callback.setFrequency("1");
				}
				req.getScriptOrNicenameOrPriority().add(callback);
				callback = oFactory.createCallback(); {
					callback.setType(CallbackType.STATUS);
					callback.setHref("http://localhost:8080/notify");
					callback.setFrequency("1");
				}
				req.getScriptOrNicenameOrPriority().add(callback);
			}
			Job job = client().sendJob(req);
			deleteAfterTest(job);
			waitForStatus(JobStatus.SUCCESS, job, 20000);
			// wait until all updates have been pushed
			Thread.sleep(1000);
			testStatusAndMessages.finalTest();
		} finally {
			server.stop(1);
		}
	}
	
	public static abstract class AbstractCallback implements HttpHandler {
		abstract void handleStatus(JobStatus status);
		abstract void handleMessages(Messages messages);
		abstract void finalTest();
		@Override
		public void handle(HttpExchange t) throws IOException {
			Job job; {
				try {
					job = (Job)JAXBContext.newInstance(Job.class).createUnmarshaller().unmarshal(t.getRequestBody());
				} catch (JAXBException e) {
					throw new RuntimeException(e);
				}
			}
			handleStatus(job.getStatus());
			Optional<Messages> messages = getMessages(job);
			if (messages.isPresent())
				handleMessages(messages.get());
			String response = "got it";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
	
	static Optional<Messages> getMessages(Job job) {
		return Optional.fromNullable(
			Iterables.getOnlyElement(
				Iterables.filter(
					job.getNicenameOrBatchIdOrScript(),
					Messages.class),
				null));
	}
	
	static <T> Stream<T> stream(T... array) {
		return Arrays.<T>stream(array);
	}
}
