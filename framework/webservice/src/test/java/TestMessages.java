import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;

import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.job.JobStatus;
import org.daisy.pipeline.webservice.jaxb.job.MessageLevel;
import org.daisy.pipeline.webservice.jaxb.job.Messages;
import org.daisy.pipeline.webservice.jaxb.job.Messages.Message;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.request.Priority;

import org.junit.Assert;
import org.junit.Test;

public class TestMessages extends Base {
	
	@Test
	public void testProgressMessages() throws Exception {
		Optional<JobRequest> req = newJobRequest(client(), Priority.LOW, "mock-messages-script", getResource("hello.xml").toURI().toString());
		Assert.assertTrue("The request is present", req.isPresent());
		Job job = client().sendJob(req.get());
		deleteAfterTest(job);
		Callable<Job> poller = new JobPoller(client(), job.getId(), JobStatus.SUCCESS, 500, 20000) {
			BigDecimal lastProgress = BigDecimal.ZERO;
			Iterator<BigDecimal> mustSee = stream(".25", ".375", ".5", ".55", ".675", ".8", ".9", "1")
			                               .map(BigDecimal::new).iterator();
			BigDecimal mustSeeNext = mustSee.next();
			List<BigDecimal> seen = new ArrayList<BigDecimal>();
			@Override
			void performAction(Job job) {
				Optional<BigDecimal> progress = getProgress(job);
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
		Messages messagesElem = assertPresent("messages element must exist", getMessages(job));
		assertEquals("total progress does not match", new BigDecimal("1"), messagesElem.getProgress());
		Iterator<Message> messages = getMessage(messagesElem).iterator();
		assertMessage(next(messages), 0, MessageLevel.INFO, "a", new BigDecimal(".5"), BigDecimal.ONE,
			msgs -> {
				assertMessage(next(msgs), 2, MessageLevel.INFO, "b", new BigDecimal(".5"), BigDecimal.ONE, null);
				assertMessage(next(msgs), 3, MessageLevel.INFO, "c", new BigDecimal(".5"), BigDecimal.ONE,
					m -> {
						assertMessage(next(m), 4, MessageLevel.INFO, "d", new BigDecimal(".5"), BigDecimal.ONE, null);
						assertMessage(next(m), 5, MessageLevel.INFO, "e", new BigDecimal(".5"), BigDecimal.ONE, null);
						Assert.assertFalse(m.hasNext()); });
				Assert.assertFalse(msgs.hasNext()); });
		assertMessage(next(messages), 8, MessageLevel.INFO, "f", new BigDecimal(".125"), BigDecimal.ONE, null);
		assertMessage(next(messages), 9, MessageLevel.INFO, "g", new BigDecimal(".125"), BigDecimal.ONE, null);
		assertMessage(next(messages), 10, MessageLevel.INFO, "h", new BigDecimal(".1"), BigDecimal.ONE, null);
		Assert.assertFalse(messages.hasNext());
	}

	static void assertMessage(Optional<? extends Message> message,
	                          int expectedSequence, MessageLevel expectedLevel, String expectedText,
	                          BigDecimal expectedPortion, BigDecimal expectedProgress,
	                          Consumer<Iterator<? extends Message>> assertChildMessages) {
		Message m = assertPresent("message does not exist", message);
		Assert.assertEquals("message sequence number does not match", expectedSequence, m.getSequence());
		Assert.assertEquals("message level does not match", expectedLevel, m.getLevel());
		Assert.assertEquals("message text does not match", expectedText, m.getContent());
		assertEquals("message portion does not match", expectedPortion, m.getPortion());
		assertEquals("message progress does not match", expectedProgress, m.getProgress());
		if (assertChildMessages != null) {
			assertChildMessages.accept(getMessage(m).iterator());
		} else {
			Assert.assertTrue("message must not have children", !getMessage(m).iterator().hasNext());
		}
	}

	static <T> Optional<T> next(Iterator<T> iterator) {
		if (iterator.hasNext())
			return Optional.<T>of(iterator.next());
		else
			return Optional.<T>absent();
	}

	static <T> Stream<T> stream(T... array) {
		return Arrays.<T>stream(array);
	}

	static <T> T assertPresent(String message, Optional<T> optional) {
		Assert.assertTrue(message, optional.isPresent());
		return optional.get();
	}

	static void assertEquals(String message, BigDecimal expected, BigDecimal actual) {
		if (expected == null
		    ? actual != null
		    : expected.compareTo(actual) != 0)
			Assert.fail(message + " expected:<" + String.valueOf(expected) + "> but was:<" + String.valueOf(actual) + ">");
	}

	static Optional<Messages> getMessages(Job job) {
		return Optional.fromNullable(
			Iterables.getOnlyElement(
				Iterables.filter(
					job.getNicenameOrBatchIdOrScript(),
					Messages.class),
				null));
	}

	static List<Message> getMessage(Messages job) {
		return job.getMessageOrMessage();
	}

	static List<Message> getMessage(Message message) {
		return message.getMessageOrMessage();
	}

	static Optional<BigDecimal> getProgress(Job job) {
		Optional<Messages> messages = getMessages(job);
		return messages.transform(m -> m.getProgress());
	}
}
