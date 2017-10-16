package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.JobMessages;
import org.daisy.pipeline.client.models.Job.Status;
import org.daisy.pipeline.client.models.JobMessages.Progress;
import org.daisy.pipeline.client.models.Message;
import org.junit.Test;

public class ProgressTest {
	
	private int sequence = 0;
	private void addMessage(Job job, Long time, String text) {
		addMessage(job, time, text, Message.Level.DEBUG);
	}
	private void addMessage(Job job, Long time, String text, Message.Level level) {
		Message m = new Message();
		m.timeStamp = time;
		m.text = text;
		m.level = level;
		m.sequence = sequence++;
		
		List<Message> messages = job.getMessages();
		if (messages == null) {
			messages = new ArrayList<Message>();
		}
		messages.add(m);
		job.setMessages(messages);
	}
	
	private String getProgressPath(Job job) {
		job.getProgressFrom(); // to ensure progress stack is updated
		String path = "";
		for (Progress progress : job.getProgressStack()) {
			path += "/";
			path += progress.getName();
			if (progress.active) {
				path += "(active)";
			}
		}
		return path;
	}
	
	final double delta = 0.01;

	@Test
	public void testProgressForStatesAndNoMessages() {
		
		Job job = new Job();
		
		job.setStatus(null);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.IDLE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.RUNNING);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.DONE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.ERROR);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.VALIDATION_FAIL);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
	}

	@Test
	public void testProgressForASimulatedJob() {
		Job job = new Job();
		
		// job queued
		job.setStatus(Status.IDLE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.RUNNING);
		addMessage(job, 0L, "[progress 0-100] ...");
		assertEquals("[progress 0-100] ...", job.getMessages().get(job.getMessages().size()-1).text);
		assertEquals("...", job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		assertEquals(95.02, job.getProgressEstimate(60000L), delta);
		
		addMessage(job, 5000L, "[progress 5-100] ...");
		assertEquals("[progress 5-100] ...", job.getMessages().get(job.getMessages().size()-1).text);
		assertEquals("...", job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(5.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(5.0, job.getProgressEstimate(5000L), delta);
		
		addMessage(job, 10000L, "[progress 10-20] ...");
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(20.0, job.getProgressTo(), delta);
		assertEquals(10.0, job.getProgressEstimate(10000L), delta);
		assertEquals(19.5, job.getProgressEstimate(20000L), delta);
		
		addMessage(job, 20000L, "[progress 20-70] ...");
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		assertEquals(20.0, job.getProgressEstimate(20000L), delta);
		
		addMessage(job, 70000L, "[progress 70-100] ...");
		assertEquals(70.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(70.0, job.getProgressEstimate(70000L), delta);
		assertEquals(98.5, job.getProgressEstimate(100000L), delta);
		
		job.setStatus(Status.DONE);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(Status.ERROR);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(Status.VALIDATION_FAIL);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(null);
		assertEquals(0.0, job.getProgressEstimate(), delta);
	}
	
	@Test
	public void testProgressNamesAndSubProgresses() {
		
		// Step type as name
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		addMessage(job, 0L, "[progress 0]");
		
		addMessage(job, 5000L, "[progress 10-30 px:a-to-b.convert] Step type as name");
		assertEquals("Step type as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(10.0, job.getProgressEstimate(5000L), delta);
		
		addMessage(job, 10000L, "[progress px:a-to-b.convert 50-100 px:a-to-b.store] Step type as name");
		assertEquals("Step type as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(20.0, job.getProgressEstimate(10000L), delta);
		
		addMessage(job, 15000L, "[progress px:a-to-b.store 50-100 px:a-to-b.foo] Step type as name");
		assertEquals("Step type as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(25.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(25.0, job.getProgressEstimate(15000L), delta);
		
		addMessage(job, 18000L, "[progress unknown-name 50-75] Step with unknown name");
		assertEquals("Step with unknown name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(3, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(25.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(29.75, job.getProgressEstimate(18000L), delta);
		
		// URI as name
		
		job = new Job();
		job.setStatus(Status.RUNNING);
		addMessage(job, 0L, "[progress 0]");
		
		addMessage(job, 5000L, "[progress 10-30 http://example.com/a-to-b.convert.xpl] URI as name");
		assertEquals("URI as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(10.0, job.getProgressEstimate(5000L), delta);
		
		addMessage(job, 10000L, "[progress http://example.com/a-to-b.convert.xpl 50-100 http://example.com/a-to-b.store.xsl] URI as name");
		assertEquals("URI as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(20.0, job.getProgressEstimate(10000L), delta);
		
		addMessage(job, 15000L, "[progress http://example.com/a-to-b.store.xsl 50-100 http://example.com/a-to-b.foo.xsl] URI as name");
		assertEquals("URI as name",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(25.0, job.getProgressFrom(), delta);
		assertEquals(30.0, job.getProgressTo(), delta);
		assertEquals(25.0, job.getProgressEstimate(15000L), delta);
		
		addMessage(job, 20000L, "[progress http://example.com/a-to-b.store.xsl 75-75 http://example.com/a-to-b.foo.xsl] Progress with from=to");
		assertEquals("Progress with from=to",job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(27.5, job.getProgressFrom(), delta);
		assertEquals(27.5, job.getProgressTo(), delta);
		assertEquals(27.5, job.getProgressEstimate(10000L), delta);
		assertEquals(27.5, job.getProgressEstimate(20000L), delta);
		assertEquals(27.5, job.getProgressEstimate(30000L), delta);

	}
	
	@Test
	public void testCumulativeProgress() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		
		addMessage(job, 1000L, "[progress 1]");
		assertEquals(0.0, job.getProgressEstimate(1000L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(1.0, job.getProgressTo(), delta);
		
		addMessage(job, 2000L, "[progress 1]");
		assertEquals(1.0, job.getProgressEstimate(2000L), delta);
		assertEquals(1.0, job.getProgressFrom(), delta);
		assertEquals(2.0, job.getProgressTo(), delta);
		
		addMessage(job, 3000L, "[progress 2]");
		assertEquals(2.0, job.getProgressEstimate(3000L), delta);
		assertEquals(2.0, job.getProgressFrom(), delta);
		assertEquals(4.0, job.getProgressTo(), delta);
		
		addMessage(job, 4000L, "[progress 4]");
		assertEquals(4.0, job.getProgressEstimate(4000L), delta);
		assertEquals(7.8, job.getProgressEstimate(7000L), delta);
		assertEquals(4.0, job.getProgressFrom(), delta);
		assertEquals(8.0, job.getProgressTo(), delta);
		
		addMessage(job, 5000L, "[progress 2]");
		assertEquals(8.0, job.getProgressEstimate(5000L), delta);
		assertEquals(8.0, job.getProgressFrom(), delta);
		assertEquals(10.0, job.getProgressTo(), delta);
		assertEquals("", job.getProgressStack().peek().getName());
		
		addMessage(job, 6000L, "[progress 60 ranged-substep]");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(10.0, job.getProgressEstimate(6000L), delta);
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		assertEquals("ranged-substep", job.getProgressStack().peek().getName());
		
		addMessage(job, 7000L, "[progress ranged-substep 0-10]");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(10.0, job.getProgressEstimate(7000L), delta);
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(16.0, job.getProgressTo(), delta);
		assertEquals("ranged-substep", job.getProgressStack().peek().getName());
		
		addMessage(job, 8000L, "[progress ranged-substep 10-50]");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(16.0, job.getProgressEstimate(8000L), delta);
		assertEquals(16.0, job.getProgressFrom(), delta);
		assertEquals(40.0, job.getProgressTo(), delta);
		assertEquals("ranged-substep", job.getProgressStack().peek().getName());
		
		addMessage(job, 9000L, "[progress ranged-substep 50-100 cumulative-substep]");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(40.0, job.getProgressEstimate(9000L), delta);
		assertEquals(40.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		assertEquals("cumulative-substep", job.getProgressStack().peek().getName());
		
		addMessage(job, 10000L, "[progress cumulative-substep 10]");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(40.0, job.getProgressEstimate(10000L), delta);
		assertEquals(40.0, job.getProgressFrom(), delta);
		assertEquals(43.0, job.getProgressTo(), delta);
		assertEquals("cumulative-substep", job.getProgressStack().peek().getName());
		
		addMessage(job, 11000L, "[progress cumulative-substep 30]");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(43.0, job.getProgressEstimate(11000L), delta);
		assertEquals(43.0, job.getProgressFrom(), delta);
		assertEquals(52.0, job.getProgressTo(), delta);
		assertEquals("cumulative-substep", job.getProgressStack().peek().getName());
		
		addMessage(job, 12000L, "[progress cumulative-substep 60]");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(52.0, job.getProgressEstimate(12000L), delta);
		assertEquals(52.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		assertEquals("cumulative-substep", job.getProgressStack().peek().getName());

	}
	
	@Test
	public void testFractionedProgress() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		
		addMessage(job, 1000L, "[progress 1/50]");
		assertEquals(0.0, job.getProgressEstimate(1000L), delta);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(2.0, job.getProgressTo(), delta);
		
		addMessage(job, 2000L, "[progress 1/50]");
		assertEquals(2.0, job.getProgressEstimate(2000L), delta);
		assertEquals(2.0, job.getProgressFrom(), delta);
		assertEquals(4.0, job.getProgressTo(), delta);
		
		// cumulative combined with fractioned
		addMessage(job, 3000L, "[progress 2]");
		assertEquals(4.0, job.getProgressEstimate(3000L), delta);
		assertEquals(4.0, job.getProgressFrom(), delta);
		assertEquals(6.0, job.getProgressTo(), delta);
		
		addMessage(job, 4000L, "[progress 2/50]");
		assertEquals(6.0, job.getProgressEstimate(4000L), delta);
		assertEquals(9.8, job.getProgressEstimate(6000L), delta);
		assertEquals(6.0, job.getProgressFrom(), delta);
		assertEquals(10.0, job.getProgressTo(), delta);
		
		addMessage(job, 5000L, "[progress 5/50]");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(10.0, job.getProgressEstimate(5000L), delta);
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(20.0, job.getProgressTo(), delta);
		
		addMessage(job, 6000L, "[progress 25 substep]");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(20.0, job.getProgressEstimate(6000L), delta);
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(45.0, job.getProgressTo(), delta);
		
		addMessage(job, 7000L, "[progress substep 10]");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(20.0, job.getProgressEstimate(7000L), delta);
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(22.5, job.getProgressTo(), delta);
		
		addMessage(job, 8000L, "[progress substep 40]");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(22.5, job.getProgressEstimate(8000L), delta);
		assertEquals(22.5, job.getProgressFrom(), delta);
		assertEquals(32.5, job.getProgressTo(), delta);
		
		addMessage(job, 9000L, "[progress substep 50]");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		assertEquals(32.5, job.getProgressEstimate(9000L), delta);
		assertEquals(32.5, job.getProgressFrom(), delta);
		assertEquals(45.0, job.getProgressTo(), delta);

	}
	
	@Test
	public void testMessageDepth() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		addMessage(job, 3500L, "First message without progress");
		addMessage(job, 1000L, "[progress 1/50]");
		addMessage(job, 2000L, "[progress 1/50] Progress message with text");
		addMessage(job, 3000L, "[progress 2]");
		addMessage(job, 3500L, "Message without progress");
		addMessage(job, 4000L, "[progress 2/50]    ");
		addMessage(job, 5000L, "[progress 5/50]");
		addMessage(job, 6000L, "[progress 25 substep]");
		addMessage(job, 3500L, "Another message without progress");
		addMessage(job, 7000L, "[progress substep 0]");
		addMessage(job, 3500L, "Message without progress");
		addMessage(job, 3500L, "Message without progress");
		addMessage(job, 7000L, "[progress unknown-name 0] Message with unknown name");
		addMessage(job, 7000L, "[progress substep 10]");
		addMessage(job, 3500L, "Yet another message without progress");
		addMessage(job, 8000L, "[progress substep 40 subsubstep]");
		addMessage(job, 8000L, "[progress subsubstep 100] Subsubstep");
		addMessage(job, 9000L, "[progress substep 50] Another progress message with text");
		addMessage(job, 1000L, "[progress unknown-name 50]");
		addMessage(job, 1000L, "[progress unknown-name 100]");
		addMessage(job, 1000L, "[progress other-unknown-name 100]");
		addMessage(job, 1000L, "[progress substep 100 deep]");
		addMessage(job, 1000L, "[progress deep 100 very-deep]");
		addMessage(job, 1000L, "[progress very-deep 100 very-very-deep]");
		addMessage(job, 1000L, "[progress very-very-deep 100 very-very-very-deep]");
		
		assertEquals(25, job.getMessages().size());
		
		assertEquals(true, job.getMessages().get(0).getText().length() > 0);
		assertEquals(false, job.getMessages().get(1).getText().length() > 0);
		assertEquals(true, job.getMessages().get(2).getText().length() > 0);
		assertEquals(false, job.getMessages().get(3).getText().length() > 0);
		assertEquals(true, job.getMessages().get(4).getText().length() > 0);
		assertEquals(false, job.getMessages().get(5).getText().length() > 0);
		assertEquals(false, job.getMessages().get(6).getText().length() > 0);
		assertEquals(false, job.getMessages().get(7).getText().length() > 0);
		assertEquals(true, job.getMessages().get(8).getText().length() > 0);
		assertEquals(false, job.getMessages().get(9).getText().length() > 0);
		assertEquals(true, job.getMessages().get(10).getText().length() > 0);
		assertEquals(true, job.getMessages().get(11).getText().length() > 0);
		assertEquals(true, job.getMessages().get(12).getText().length() > 0);
		assertEquals(false, job.getMessages().get(13).getText().length() > 0);
		assertEquals(true, job.getMessages().get(14).getText().length() > 0);
		assertEquals(false, job.getMessages().get(15).getText().length() > 0);
		assertEquals(true, job.getMessages().get(16).getText().length() > 0);
		assertEquals(true, job.getMessages().get(17).getText().length() > 0);
		assertEquals(false, job.getMessages().get(18).getText().length() > 0);
		assertEquals(false, job.getMessages().get(19).getText().length() > 0);
		assertEquals(false, job.getMessages().get(20).getText().length() > 0);
		assertEquals(false, job.getMessages().get(21).getText().length() > 0);
		assertEquals(false, job.getMessages().get(22).getText().length() > 0);
		assertEquals(false, job.getMessages().get(23).getText().length() > 0);
		assertEquals(false, job.getMessages().get(24).getText().length() > 0);
		
		assertEquals(0, job.getMessages().get(0).depth.intValue());
		assertEquals(0, job.getMessages().get(1).depth.intValue());
		assertEquals(0, job.getMessages().get(2).depth.intValue());
		assertEquals(0, job.getMessages().get(3).depth.intValue());
		assertEquals(1, job.getMessages().get(4).depth.intValue());
		assertEquals(0, job.getMessages().get(5).depth.intValue());
		assertEquals(0, job.getMessages().get(6).depth.intValue());
		assertEquals(0, job.getMessages().get(7).depth.intValue());
		assertEquals(1, job.getMessages().get(8).depth.intValue());
		assertEquals(1, job.getMessages().get(9).depth.intValue());
		assertEquals(2, job.getMessages().get(10).depth.intValue());
		assertEquals(2, job.getMessages().get(11).depth.intValue());
		assertEquals(2, job.getMessages().get(12).depth.intValue());
		assertEquals(1, job.getMessages().get(13).depth.intValue());
		assertEquals(2, job.getMessages().get(14).depth.intValue());
		assertEquals(1, job.getMessages().get(15).depth.intValue());
		assertEquals(2, job.getMessages().get(16).depth.intValue());
		assertEquals(1, job.getMessages().get(17).depth.intValue());
		assertEquals(2, job.getMessages().get(18).depth.intValue());
		assertEquals(2, job.getMessages().get(19).depth.intValue());
		assertEquals(2, job.getMessages().get(20).depth.intValue());
		assertEquals(1, job.getMessages().get(21).depth.intValue());
		assertEquals(2, job.getMessages().get(22).depth.intValue());
		assertEquals(3, job.getMessages().get(23).depth.intValue());
		assertEquals(4, job.getMessages().get(24).depth.intValue());
		
		
		// filter on depth
		
		assertEquals(7, job.getMessages(0).size());
		assertEquals(0, job.getMessages(0).get(0).depth.intValue());
		assertEquals(0, job.getMessages(0).get(1).depth.intValue());
		assertEquals(0, job.getMessages(0).get(2).depth.intValue());
		assertEquals(0, job.getMessages(0).get(3).depth.intValue());
		assertEquals(0, job.getMessages(0).get(4).depth.intValue());
		assertEquals(0, job.getMessages(0).get(5).depth.intValue());
		assertEquals(0, job.getMessages(0).get(6).depth.intValue());
		
		assertEquals(14, job.getMessages(1).size());
		assertEquals(0, job.getMessages(1).get(0).depth.intValue());
		assertEquals(0, job.getMessages(1).get(1).depth.intValue());
		assertEquals(0, job.getMessages(1).get(2).depth.intValue());
		assertEquals(0, job.getMessages(1).get(3).depth.intValue());
		assertEquals(1, job.getMessages(1).get(4).depth.intValue());
		assertEquals(0, job.getMessages(1).get(5).depth.intValue());
		assertEquals(0, job.getMessages(1).get(6).depth.intValue());
		assertEquals(0, job.getMessages(1).get(7).depth.intValue());
		assertEquals(1, job.getMessages(1).get(8).depth.intValue());
		assertEquals(1, job.getMessages(1).get(9).depth.intValue());
		assertEquals(1, job.getMessages(1).get(10).depth.intValue());
		assertEquals(1, job.getMessages(1).get(11).depth.intValue());
		assertEquals(1, job.getMessages(1).get(12).depth.intValue());
		
		assertEquals(23, job.getMessages(2).size());
		assertEquals(24, job.getMessages(3).size());
		assertEquals(25, job.getMessages(4).size());
		assertEquals(25, job.getMessages(5).size());
	}
	
	@Test
	public void testGlobbing() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		
		// no globbing
		
		addMessage(job, 0L, "[progress 1 org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run] main");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress other 1 org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run] ignore");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run 1] sub");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		
		// globbing everything

		addMessage(job, 0L, "[progress 1 *] main");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress other 1 org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run] sub");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress something 1] ignore");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run 1] subsub");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		
		addMessage(job, 0L, "[progress something 1] ignore");
		assertEquals(3, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		
		// globing at the start
		
		addMessage(job, 0L, "[progress 1 *.PxTransformStep.run] main");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress other 1 org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run] ignore");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run 1] sub");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		
		addMessage(job, 0L, "[progress something 1] ignore");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		
		// globing at the end
		
		addMessage(job, 0L, "[progress 1 org.daisy.*] main");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress other 1 org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run] ignore");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run 1] sub");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		
		addMessage(job, 0L, "[progress something 1] ignore");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		
		// globbing at the start and end

		addMessage(job, 0L, "[progress 1 *.PxTransformStep*] main");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress other 1 org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run] ignore");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run 1] sub");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		
		addMessage(job, 0L, "[progress something 1] ignore");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		
		// multiple globs
		
		addMessage(job, 0L, "[progress 1 *braille*calabash*Step.run] main");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress other 1 org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run] ignore");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep.run 1] sub");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		
		addMessage(job, 0L, "[progress something 1] ignore");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		
		
		// glob with '?'
		
		addMessage(job, 0L, "[progress 1 a?c] main");
		assertEquals(0, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress abbc 1] ignore");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

		addMessage(job, 0L, "[progress abc 1] sub");
		assertEquals(1, job.getMessages().get(job.getMessages().size()-1).depth.intValue());
		
		addMessage(job, 0L, "[progress something 1] ignore");
		assertEquals(2, job.getMessages().get(job.getMessages().size()-1).depth.intValue());

	}
	
	@Test
	public void testProgressPath() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		addMessage(job, 0L, "[progress 1 px:delete-parameters] Collecting parameters");
		assertEquals("/(active)/px:delete-parameters", getProgressPath(job));

		addMessage(job, 0L, "[progress 1 px:tempdir] Creating temporary directory");
		assertEquals("/(active)/px:tempdir", getProgressPath(job));

		addMessage(job, 0L, "[progress 92 px:dtbook-to-pef.convert] Converting from DTBook to PEF");
		assertEquals("/(active)/px:dtbook-to-pef.convert", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 px:merge-parameters]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:merge-parameters", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 px:dtbook-load] Loading DTBook");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dtbook-load", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 dtbook-to-metadata.xsl] Extracting metadata from DTBook");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/dtbook-to-metadata.xsl", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 generate-toc.xsl] Generating table of contents");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/generate-toc.xsl", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 6 px:apply-stylesheets] Inlining CSS");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:apply-stylesheets", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 4 px:dtbook-to-pef.convert.viewport-math] Transforming MathML");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dtbook-to-pef.convert.viewport-math", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 84 *]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/.*", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dotify-transform 27 px:transform]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/px:transform", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dotify-transform 70 pxi:css-to-obfl]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 p:xslt]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/p:xslt", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 css:parse-properties] Make css:display, css:render-table-by and css:table-header-policy attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:parse-properties", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 2 css:render-table-by] Layout tables as lists.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:render-table-by", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 pxi:recursive-parse-stylesheet-and-make-pseudo-elements] Recursively parse stylesheet and make pseudo elements");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:recursive-parse-stylesheet-and-make-pseudo-elements", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 5 for-each.parse-properties-and-eval-string-set]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/for-each.parse-properties-and-eval-string-set", getProgressPath(job));

		addMessage(job, 0L, "[progress for-each.parse-properties-and-eval-string-set 1/1 for-each.parse-properties-and-eval-string-set.iteration]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/for-each.parse-properties-and-eval-string-set(active)/for-each.parse-properties-and-eval-string-set.iteration", getProgressPath(job));

		addMessage(job, 0L, "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:parse-properties]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/for-each.parse-properties-and-eval-string-set(active)/for-each.parse-properties-and-eval-string-set.iteration(active)/css:parse-properties", getProgressPath(job));

		addMessage(job, 0L, "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:eval-string-set]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/for-each.parse-properties-and-eval-string-set(active)/for-each.parse-properties-and-eval-string-set.iteration(active)/css:eval-string-set", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 2 css:parse-content]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:parse-content", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.foreach-parse-properties] Make css:flow attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.foreach-parse-properties", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.foreach-parse-properties 1/1 css:parse-properties]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.foreach-parse-properties(active)/css:parse-properties", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 css:flow-into] Extract named flows based on css:flow attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:flow-into", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 css:label-targets] Make css:id attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:label-targets", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 css:eval-target-content] Evaluate css:content elements.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:eval-target-content", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.for-each-parse-preserve-table-box]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box 1/1 pxi:css-to-obfl.for-each-parse-preserve-table-box.part]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box.part", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties] Make css:white-space, css:display, css:list-style-type, css:page-break-before and css:page-break-after attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box.part(active)/css:parse-properties", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:preserve-white-space] Make css:white-space elements from css:white-space attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box.part(active)/css:preserve-white-space", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-table-grid] Create table grid structures from HTML/DTBook tables.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box.part(active)/css:make-table-grid", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-boxes] Make css:box elements based on css:display and css:list-style-type attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box.part(active)/css:make-boxes", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box(active)/pxi:css-to-obfl.for-each-parse-preserve-table-box.part(active)/css:parse-properties", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 8 css:eval-counter] Evaluate css:counter elements.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:eval-counter", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 2 css:flow-from] Evaluate css:flow elements.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:flow-from", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 css:eval-target-text] Evaluate css:text elements.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/css:eval-target-text", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.for-each-anonymous-inline-boxes] Wrap/unwrap with inline css:box elements.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-anonymous-inline-boxes", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.for-each-anonymous-inline-boxes 1/1 css:make-anonyous-inline-boxes]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.for-each-anonymous-inline-boxes(active)/css:make-anonyous-inline-boxes", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.split-sections] Split flows into sections.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.split-sections 1/1 pxi:css-to-obfl.split-sections.section]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.split-sections.section 50 css:parse-counter-set] Make css:counter-set-page attributes.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section(active)/css:parse-counter-set", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl.split-sections.section 50 css:split] Page and volume split.");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section(active)/css:split", getProgressPath(job));

		addMessage(job, 0L, "[progress css:split 10 p:add-attribute]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section(active)/css:split(active)/p:add-attribute", getProgressPath(job));

		addMessage(job, 0L, "[progress css:split 10 p:add-attribute]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section(active)/css:split(active)/p:add-attribute", getProgressPath(job));

		addMessage(job, 0L, "[progress css:split 75 split-into-sections.xsl]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section(active)/css:split(active)/split-into-sections.xsl", getProgressPath(job));

		addMessage(job, 0L, "[progress split-into-sections.xsl 1/2] Found 2 sections; splitting section 1 of 2");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section(active)/css:split(active)/split-into-sections.xsl(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress split-into-sections.xsl 1/2] Splitting section 2 of 2 sections");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dotify-transform(active)/pxi:css-to-obfl(active)/pxi:css-to-obfl.split-sections(active)/pxi:css-to-obfl.split-sections.section(active)/css:split(active)/split-into-sections.xsl(active)", getProgressPath(job));
		
		
		// test that without the '*' glob, most of the progress messages here should be ignored
		
		job = new Job();
		job.setStatus(Status.RUNNING);
		
		addMessage(job, 0L, "[progress 1 px:delete-parameters] Collecting parameters");
		assertEquals("/(active)/px:delete-parameters", getProgressPath(job));

		addMessage(job, 0L, "[progress 1 px:tempdir] Creating temporary directory");
		assertEquals("/(active)/px:tempdir", getProgressPath(job));

		addMessage(job, 0L, "[progress 92 px:dtbook-to-pef.convert] Converting from DTBook to PEF");
		assertEquals("/(active)/px:dtbook-to-pef.convert", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 px:merge-parameters]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:merge-parameters", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 px:dtbook-load] Loading DTBook");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dtbook-load", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 dtbook-to-metadata.xsl] Extracting metadata from DTBook");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/dtbook-to-metadata.xsl", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 1 generate-toc.xsl] Generating table of contents");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/generate-toc.xsl", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 6 px:apply-stylesheets] Inlining CSS");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:apply-stylesheets", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 4 px:dtbook-to-pef.convert.viewport-math] Transforming MathML");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)/px:dtbook-to-pef.convert.viewport-math", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dtbook-to-pef.convert 84]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dotify-transform 27 px:transform]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress px:dotify-transform 70 pxi:css-to-obfl]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 1 p:xslt]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress pxi:css-to-obfl 5 for-each.parse-properties-and-eval-string-set]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress for-each.parse-properties-and-eval-string-set 1/1 for-each.parse-properties-and-eval-string-set.iteration]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:parse-properties]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));

		addMessage(job, 0L, "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:eval-string-set]");
		assertEquals("/(active)/px:dtbook-to-pef.convert(active)", getProgressPath(job));
		
	}
	
	@Test
	public void testInferredMessageSeverity() {
		
		Job job = new Job();
		job.setStatus(Status.RUNNING);
		
		addMessage(job, 1000L, "[progress 1 substep]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress substep 100 deep]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress deep 100 very-deep]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress substep 100 deep]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress deep 100 very-deep]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress very-deep 100 very-very-deep]", Message.Level.WARNING);
		addMessage(job, 1000L, "[progress substep 100 deep]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress deep 100 very-deep]", Message.Level.ERROR);
		
		assertEquals(Message.Level.INFO, job.getMessages().get(0).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(1).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(2).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(3).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(4).level);
		assertEquals(Message.Level.WARNING, job.getMessages().get(5).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(6).level);
		assertEquals(Message.Level.ERROR, job.getMessages().get(7).level);

		assertEquals(Message.Level.ERROR, job.getMessages().get(0).inferredLevel);
		assertEquals(Message.Level.INFO, job.getMessages().get(1).inferredLevel);
		assertEquals(Message.Level.INFO, job.getMessages().get(2).inferredLevel);
		assertEquals(Message.Level.WARNING, job.getMessages().get(3).inferredLevel);
		assertEquals(Message.Level.WARNING, job.getMessages().get(4).inferredLevel);
		assertEquals(Message.Level.WARNING, job.getMessages().get(5).inferredLevel);
		assertEquals(Message.Level.ERROR, job.getMessages().get(6).inferredLevel);
		assertEquals(Message.Level.ERROR, job.getMessages().get(7).inferredLevel);
		
		
		job = new Job();
		job.setStatus(Status.RUNNING);
		
		addMessage(job, 1000L, "[progress 92 px:dtbook-to-pef.convert] Converting from DTBook to PEF", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 1 px:dtbook-load] Loading DTBook", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 6 px:apply-stylesheets] Inlining CSS", Message.Level.INFO);
		addMessage(job, 1000L, "stylesheets: http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/css/default.css bundle://95.0:1/css/default.scss", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 4 px:dtbook-to-pef.convert.viewport-math] Transforming MathML", Message.Level.INFO);
		addMessage(job, 1000L, "Transforming from XML with inline CSS to PEF", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in namespace http://www.daisy.org/z3986/2005/dtbook/, but none of the template rules match elements in this namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 css:parse-properties]", Message.Level.INFO);
		
		assertEquals(Message.Level.INFO, job.getMessages().get(0).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(1).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(2).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(3).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(4).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(5).level);
		assertEquals(Message.Level.ERROR, job.getMessages().get(6).level);
		assertEquals(Message.Level.INFO, job.getMessages().get(7).level);

		assertEquals(Message.Level.ERROR, job.getMessages().get(0).inferredLevel);
		assertEquals(Message.Level.INFO, job.getMessages().get(1).inferredLevel);
		assertEquals(Message.Level.INFO, job.getMessages().get(2).inferredLevel);
		assertEquals(Message.Level.INFO, job.getMessages().get(3).inferredLevel);
		assertEquals(Message.Level.ERROR, job.getMessages().get(4).inferredLevel);
		assertEquals(Message.Level.INFO, job.getMessages().get(5).inferredLevel);
		assertEquals(Message.Level.ERROR, job.getMessages().get(6).inferredLevel);
		assertEquals(Message.Level.INFO, job.getMessages().get(7).inferredLevel);
		
		
		// regression test
		
		job = new Job();
		job.setStatus(Status.RUNNING);
		
		this.sequence = 0;
		addMessage(job, 1000L, "[progress 1 px:delete-parameters] Collecting parameters", Message.Level.INFO);
		addMessage(job, 1000L, "[progress 1 px:tempdir] Creating temporary directory", Message.Level.INFO);
		addMessage(job, 1000L, "[progress 92 px:dtbook-to-pef.convert] Converting from DTBook to PEF", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 1 px:merge-parameters]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 1 px:dtbook-load] Loading DTBook", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 1 dtbook-to-metadata.xsl] Extracting metadata from DTBook", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 1 generate-toc.xsl] Generating table of contents", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 6 px:apply-stylesheets] Inlining CSS", Message.Level.INFO);
		addMessage(job, 1000L, "stylesheets: http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/css/default.css bundle://95.0:1/css/default.scss", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 4 px:dtbook-to-pef.convert.viewport-math] Transforming MathML", Message.Level.INFO);
		addMessage(job, 1000L, "Transforming from XML with inline CSS to PEF", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 84 *]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dotify-transform 27 px:transform]", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in namespace http://www.daisy.org/z3986/2005/dtbook/, but none of the template rules match elements in this namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress px:dotify-transform 70 pxi:css-to-obfl]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 p:xslt]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 css:parse-properties] Make css:display, css:render-table-by and css:table-header-policy attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 2 css:render-table-by] Layout tables as lists.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 pxi:recursive-parse-stylesheet-and-make-pseudo-elements] Recursively parse stylesheet and make pseudo elements", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 5 for-each.parse-properties-and-eval-string-set]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress for-each.parse-properties-and-eval-string-set 1/1 for-each.parse-properties-and-eval-string-set.iteration]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:parse-properties]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:eval-string-set]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 2 css:parse-content]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.foreach-parse-properties] Make css:flow attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.foreach-parse-properties 1/1 css:parse-properties]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 css:flow-into] Extract named flows based on css:flow attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 css:label-targets] Make css:id attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 css:eval-target-content] Evaluate css:content elements.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.for-each-parse-preserve-table-box]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box 1/1 pxi:css-to-obfl.for-each-parse-preserve-table-box.part]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties] Make css:white-space, css:display, css:list-style-type, css:page-break-before and css:page-break-after attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:preserve-white-space] Make css:white-space elements from css:white-space attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-table-grid] Create table grid structures from HTML/DTBook tables.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-boxes] Make css:box elements based on css:display and css:list-style-type attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 8 css:eval-counter] Evaluate css:counter elements.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 2 css:flow-from] Evaluate css:flow elements.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 css:eval-target-text] Evaluate css:text elements.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.for-each-anonymous-inline-boxes] Wrap/unwrap with inline css:box elements.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-anonymous-inline-boxes 1/1 css:make-anonyous-inline-boxes]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.split-sections] Split flows into sections.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.split-sections 1/1 pxi:css-to-obfl.split-sections.section]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.split-sections.section 50 css:parse-counter-set] Make css:counter-set-page attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.split-sections.section 50 css:split] Page and volume split.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress css:split 10 p:add-attribute]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress css:split 10 p:add-attribute]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress css:split 75 split-into-sections.xsl]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress css:split 5 p:filter]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.pages-and-volumes] Move css:page, css:counter-set-page and css:volume attributes to css:_ root element.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.pages-and-volumes 1/2]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.pages-and-volumes 1/2]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 14 css:shift-string-set] Move css:string-set attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 pxi:shift-obfl-marker] Move css:_obfl-marker attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1 css:shift-id] Move css:id attributes to css:box elements.", Message.Level.INFO);
		addMessage(job, 1000L, "Creating an attribute here will fail if previous instructions create any children", Message.Level.ERROR);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 8 pxi:css-to-obfl.for-each-black-box]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-black-box 1/4 p:unwrap] Unwrap css:_ elements.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-black-box 1/4 css:make-anonymous-black-boxes] Wrap inline css:box elements in block css:box elements where necessary.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-black-box 1/4 p:unwrap] Unwrap css:_ elements.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-black-box 1/4 css:make-anonymous-black-boxes] Wrap inline css:box elements in block css:box elements where necessary.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 3 pxi:css-to-obfl.for-each-margin-attributes]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes 1/1 pxi:css-to-obfl.for-each-margin-attributes.item]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:parse-properties] Make css:margin-*, css:border-*, css:border-bottom and css:text-indent attributes.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:adjust-boxes] Adjust boxes.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:new-definition] New definition.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes.item 10 p:delete] Remove text nodes from block boxes with no line boxes.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 pxi:propagate-page-break] Resolve css:page-break-before=avoid and css:page-break-after=always.", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes.item 2 p:delete] Move css:page-break-after=avoid to last descendant block", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl.for-each-margin-attributes.item 8]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 26 css-to-obfl.xsl]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 10] add <marker class=\"foo/prev\"/>", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pxi:css-to-obfl 1] move table-of-contents elements to the right place", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dotify-transform 1 pxi:obfl-normalize-space]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dotify-transform 2 dotify:obfl-to-pef]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 1 pef:add-metadata] Adding metadata to PEF", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.convert 1 p:add-attribute] Adding language attribute to PEF", Message.Level.INFO);
		addMessage(job, 1000L, "[progress 6 px:dtbook-to-pef.store] Storing PEF", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.store 90 pef:store]", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pef:store 1 p:store] Storing PEF as 'file:/some/path/data/jobs/4b613329-3841-401f-8018-d20b5a9a8120/output/pef-output-dir/557889.bok227s.en.pef'", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pef:store 17] Not storing as BRF", Message.Level.INFO);
		addMessage(job, 1000L, "[progress pef:store 82] Not including HTML preview", Message.Level.INFO);
		addMessage(job, 1000L, "[progress px:dtbook-to-pef.store 10 p:sink]", Message.Level.INFO);
		
		assertEquals(Message.Level.INFO, job.getMessages().get(0).level); // "[progress 1 px:delete-parameters] Collecting parameters"
		assertEquals(Message.Level.INFO, job.getMessages().get(1).level); // "[progress 1 px:tempdir] Creating temporary directory"
		assertEquals(Message.Level.INFO, job.getMessages().get(2).level); // "[progress 92 px:dtbook-to-pef.convert] Converting from DTBook to PEF"
		assertEquals(Message.Level.INFO, job.getMessages().get(3).level); // "[progress px:dtbook-to-pef.convert 1 px:merge-parameters]"
		assertEquals(Message.Level.INFO, job.getMessages().get(4).level); // "[progress px:dtbook-to-pef.convert 1 px:dtbook-load] Loading DTBook"
		assertEquals(Message.Level.INFO, job.getMessages().get(5).level); // "[progress px:dtbook-to-pef.convert 1 dtbook-to-metadata.xsl] Extracting metadata from DTBook"
		assertEquals(Message.Level.INFO, job.getMessages().get(6).level); // "[progress px:dtbook-to-pef.convert 1 generate-toc.xsl] Generating table of contents"
		assertEquals(Message.Level.INFO, job.getMessages().get(7).level); // "[progress px:dtbook-to-pef.convert 6 px:apply-stylesheets] Inlining CSS"
		assertEquals(Message.Level.INFO, job.getMessages().get(8).level); // "stylesheets: http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/css/default.css bundle://95.0:1/css/default.scss"
		assertEquals(Message.Level.INFO, job.getMessages().get(9).level); // "[progress px:dtbook-to-pef.convert 4 px:dtbook-to-pef.convert.viewport-math] Transforming MathML"
		assertEquals(Message.Level.INFO, job.getMessages().get(10).level); // "Transforming from XML with inline CSS to PEF"
		assertEquals(Message.Level.INFO, job.getMessages().get(11).level); // "[progress px:dtbook-to-pef.convert 84 *]"
		assertEquals(Message.Level.INFO, job.getMessages().get(12).level); // "[progress px:dotify-transform 27 px:transform]"
		assertEquals(Message.Level.ERROR, job.getMessages().get(13).level); // "err:SXXP0005:The source document is in namespace http://www.daisy.org/z3986/2005/dtbook/, but none of the template rules match elements in this namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(14).level); // "[progress px:dotify-transform 70 pxi:css-to-obfl]"
		assertEquals(Message.Level.INFO, job.getMessages().get(15).level); // "[progress pxi:css-to-obfl 1 p:xslt]"
		assertEquals(Message.Level.INFO, job.getMessages().get(16).level); // "[progress pxi:css-to-obfl 1 css:parse-properties] Make css:display, css:render-table-by and css:table-header-policy attributes."
		assertEquals(Message.Level.INFO, job.getMessages().get(17).level); // "[progress pxi:css-to-obfl 2 css:render-table-by] Layout tables as lists."
		assertEquals(Message.Level.INFO, job.getMessages().get(18).level); // "[progress pxi:css-to-obfl 1 pxi:recursive-parse-stylesheet-and-make-pseudo-elements] Recursively parse stylesheet and make pseudo elements"
		assertEquals(Message.Level.ERROR, job.getMessages().get(19).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.ERROR, job.getMessages().get(20).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(21).level); // "[progress pxi:css-to-obfl 5 for-each.parse-properties-and-eval-string-set]"
		assertEquals(Message.Level.INFO, job.getMessages().get(22).level); // "[progress for-each.parse-properties-and-eval-string-set 1/1 for-each.parse-properties-and-eval-string-set.iteration]"
		assertEquals(Message.Level.INFO, job.getMessages().get(23).level); // "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:parse-properties]"
		assertEquals(Message.Level.INFO, job.getMessages().get(24).level); // "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:eval-string-set]"
		assertEquals(Message.Level.INFO, job.getMessages().get(25).level); // "[progress pxi:css-to-obfl 2 css:parse-content]"
		assertEquals(Message.Level.INFO, job.getMessages().get(26).level); // "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.foreach-parse-properties] Make css:flow attributes."
		assertEquals(Message.Level.INFO, job.getMessages().get(27).level); // "[progress pxi:css-to-obfl.foreach-parse-properties 1/1 css:parse-properties]"
		assertEquals(Message.Level.INFO, job.getMessages().get(28).level); // "[progress pxi:css-to-obfl 1 css:flow-into] Extract named flows based on css:flow attributes."
		assertEquals(Message.Level.ERROR, job.getMessages().get(29).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(30).level); // "[progress pxi:css-to-obfl 1 css:label-targets] Make css:id attributes."
		assertEquals(Message.Level.ERROR, job.getMessages().get(31).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(32).level); // "[progress pxi:css-to-obfl 1 css:eval-target-content] Evaluate css:content elements."
		assertEquals(Message.Level.ERROR, job.getMessages().get(33).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(34).level); // "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.for-each-parse-preserve-table-box]"
		assertEquals(Message.Level.INFO, job.getMessages().get(35).level); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box 1/1 pxi:css-to-obfl.for-each-parse-preserve-table-box.part]"
		assertEquals(Message.Level.INFO, job.getMessages().get(36).level); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties] Make css:white-space, css:display, css:list-style-type, css:page-break-before and css:page-break-after attributes."
		assertEquals(Message.Level.INFO, job.getMessages().get(37).level); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:preserve-white-space] Make css:white-space elements from css:white-space attributes."
		assertEquals(Message.Level.ERROR, job.getMessages().get(38).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(39).level); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-table-grid] Create table grid structures from HTML/DTBook tables."
		assertEquals(Message.Level.ERROR, job.getMessages().get(40).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(41).level); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-boxes] Make css:box elements based on css:display and css:list-style-type attributes."
		assertEquals(Message.Level.ERROR, job.getMessages().get(42).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(43).level); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties]"
		assertEquals(Message.Level.INFO, job.getMessages().get(44).level); // "[progress pxi:css-to-obfl 8 css:eval-counter] Evaluate css:counter elements."
		assertEquals(Message.Level.INFO, job.getMessages().get(45).level); // "[progress pxi:css-to-obfl 2 css:flow-from] Evaluate css:flow elements."
		assertEquals(Message.Level.INFO, job.getMessages().get(46).level); // "[progress pxi:css-to-obfl 1 css:eval-target-text] Evaluate css:text elements."
		assertEquals(Message.Level.ERROR, job.getMessages().get(47).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(48).level); // "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.for-each-anonymous-inline-boxes] Wrap/unwrap with inline css:box elements."
		assertEquals(Message.Level.INFO, job.getMessages().get(49).level); // "[progress pxi:css-to-obfl.for-each-anonymous-inline-boxes 1/1 css:make-anonyous-inline-boxes]"
		assertEquals(Message.Level.INFO, job.getMessages().get(50).level); // "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.split-sections] Split flows into sections."
		assertEquals(Message.Level.INFO, job.getMessages().get(51).level); // "[progress pxi:css-to-obfl.split-sections 1/1 pxi:css-to-obfl.split-sections.section]"
		assertEquals(Message.Level.INFO, job.getMessages().get(52).level); // "[progress pxi:css-to-obfl.split-sections.section 50 css:parse-counter-set] Make css:counter-set-page attributes."
		assertEquals(Message.Level.INFO, job.getMessages().get(53).level); // "[progress pxi:css-to-obfl.split-sections.section 50 css:split] Page and volume split."
		assertEquals(Message.Level.INFO, job.getMessages().get(54).level); // "[progress css:split 10 p:add-attribute]"
		assertEquals(Message.Level.INFO, job.getMessages().get(55).level); // "[progress css:split 10 p:add-attribute]"
		assertEquals(Message.Level.INFO, job.getMessages().get(56).level); // "[progress css:split 75 split-into-sections.xsl]"
		assertEquals(Message.Level.INFO, job.getMessages().get(57).level); // "[progress css:split 5 p:filter]"
		assertEquals(Message.Level.INFO, job.getMessages().get(58).level); // "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.pages-and-volumes] Move css:page, css:counter-set-page and css:volume attributes to css:_ root element."
		assertEquals(Message.Level.INFO, job.getMessages().get(59).level); // "[progress pxi:css-to-obfl.pages-and-volumes 1/2]"
		assertEquals(Message.Level.INFO, job.getMessages().get(60).level); // "[progress pxi:css-to-obfl.pages-and-volumes 1/2]"
		assertEquals(Message.Level.INFO, job.getMessages().get(61).level); // "[progress pxi:css-to-obfl 14 css:shift-string-set] Move css:string-set attributes."
		assertEquals(Message.Level.ERROR, job.getMessages().get(62).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(63).level); // "[progress pxi:css-to-obfl 1 pxi:shift-obfl-marker] Move css:_obfl-marker attributes."
		assertEquals(Message.Level.ERROR, job.getMessages().get(64).level); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace"
		assertEquals(Message.Level.INFO, job.getMessages().get(65).level); // "[progress pxi:css-to-obfl 1 css:shift-id] Move css:id attributes to css:box elements."
		assertEquals(Message.Level.ERROR, job.getMessages().get(66).level); // "Creating an attribute here will fail if previous instructions create any children"
		assertEquals(Message.Level.INFO, job.getMessages().get(67).level); // "[progress pxi:css-to-obfl 8 pxi:css-to-obfl.for-each-black-box]"
		assertEquals(Message.Level.INFO, job.getMessages().get(68).level); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 p:unwrap] Unwrap css:_ elements."
		assertEquals(Message.Level.INFO, job.getMessages().get(69).level); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 css:make-anonymous-black-boxes] Wrap inline css:box elements in block css:box elements where necessary."
		assertEquals(Message.Level.INFO, job.getMessages().get(70).level); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 p:unwrap] Unwrap css:_ elements."
		assertEquals(Message.Level.INFO, job.getMessages().get(71).level); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 css:make-anonymous-black-boxes] Wrap inline css:box elements in block css:box elements where necessary."
		assertEquals(Message.Level.INFO, job.getMessages().get(72).level); // "[progress pxi:css-to-obfl 3 pxi:css-to-obfl.for-each-margin-attributes]"
		assertEquals(Message.Level.INFO, job.getMessages().get(73).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes 1/1 pxi:css-to-obfl.for-each-margin-attributes.item]"
		assertEquals(Message.Level.INFO, job.getMessages().get(74).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:parse-properties] Make css:margin-*, css:border-*, css:border-bottom and css:text-indent attributes."
		assertEquals(Message.Level.INFO, job.getMessages().get(75).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:adjust-boxes] Adjust boxes."
		assertEquals(Message.Level.INFO, job.getMessages().get(76).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:new-definition] New definition."
		assertEquals(Message.Level.INFO, job.getMessages().get(77).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 10 p:delete] Remove text nodes from block boxes with no line boxes."
		assertEquals(Message.Level.INFO, job.getMessages().get(78).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 pxi:propagate-page-break] Resolve css:page-break-before=avoid and css:page-break-after=always."
		assertEquals(Message.Level.INFO, job.getMessages().get(79).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 2 p:delete] Move css:page-break-after=avoid to last descendant block"
		assertEquals(Message.Level.INFO, job.getMessages().get(80).level); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 8]"
		assertEquals(Message.Level.INFO, job.getMessages().get(81).level); // "[progress pxi:css-to-obfl 26 css-to-obfl.xsl]"
		assertEquals(Message.Level.INFO, job.getMessages().get(82).level); // "[progress pxi:css-to-obfl 10] add <marker class=\"foo/prev\"/>"
		assertEquals(Message.Level.INFO, job.getMessages().get(83).level); // "[progress pxi:css-to-obfl 1]"
		assertEquals(Message.Level.INFO, job.getMessages().get(84).level); // "[progress pxi:css-to-obfl 1] move table-of-contents elements to the right place"
		assertEquals(Message.Level.INFO, job.getMessages().get(85).level); // "[progress px:dotify-transform 1 pxi:obfl-normalize-space]"
		assertEquals(Message.Level.INFO, job.getMessages().get(86).level); // "[progress px:dotify-transform 2 dotify:obfl-to-pef]"
		assertEquals(Message.Level.INFO, job.getMessages().get(87).level); // "[progress px:dtbook-to-pef.convert 1 pef:add-metadata] Adding metadata to PEF"
		assertEquals(Message.Level.INFO, job.getMessages().get(88).level); // "[progress px:dtbook-to-pef.convert 1 p:add-attribute] Adding language attribute to PEF"
		assertEquals(Message.Level.INFO, job.getMessages().get(89).level); // "[progress 6 px:dtbook-to-pef.store] Storing PEF"
		assertEquals(Message.Level.INFO, job.getMessages().get(90).level); // "[progress px:dtbook-to-pef.store 90 pef:store]"
		assertEquals(Message.Level.INFO, job.getMessages().get(91).level); // "[progress pef:store 1 p:store] Storing PEF as 'file:/some/path/data/jobs/4b613329-3841-401f-8018-d20b5a9a8120/output/pef-output-dir//557889.bok227s.en.pef'"
		assertEquals(Message.Level.INFO, job.getMessages().get(92).level); // "[progress pef:store 17] Not storing as BRF"
		assertEquals(Message.Level.INFO, job.getMessages().get(93).level); // "[progress pef:store 82] Not including HTML preview"
		assertEquals(Message.Level.INFO, job.getMessages().get(94).level); // "[progress px:dtbook-to-pef.store 10 p:sink]"
		
		assertEquals(Message.Level.INFO, job.getMessages().get(0).inferredLevel); // "[progress 1 px:delete-parameters] Collecting parameters", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(1).inferredLevel); // "[progress 1 px:tempdir] Creating temporary directory", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(2).inferredLevel); // "[progress 92 px:dtbook-to-pef.convert] Converting from DTBook to PEF", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(3).inferredLevel); // "[progress px:dtbook-to-pef.convert 1 px:merge-parameters]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(4).inferredLevel); // "[progress px:dtbook-to-pef.convert 1 px:dtbook-load] Loading DTBook", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(5).inferredLevel); // "[progress px:dtbook-to-pef.convert 1 dtbook-to-metadata.xsl] Extracting metadata from DTBook", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(6).inferredLevel); // "[progress px:dtbook-to-pef.convert 1 generate-toc.xsl] Generating table of contents", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(7).inferredLevel); // "[progress px:dtbook-to-pef.convert 6 px:apply-stylesheets] Inlining CSS", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(8).inferredLevel); // "stylesheets: http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/css/default.css bundle://95.0:1/css/default.scss", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(9).inferredLevel); // "[progress px:dtbook-to-pef.convert 4 px:dtbook-to-pef.convert.viewport-math] Transforming MathML", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(10).inferredLevel); // "Transforming from XML with inline CSS to PEF", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(11).inferredLevel); // "[progress px:dtbook-to-pef.convert 84 *]", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(12).inferredLevel); // "[progress px:dotify-transform 27 px:transform]", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(13).inferredLevel); // "err:SXXP0005:The source document is in namespace http://www.daisy.org/z3986/2005/dtbook/, but none of the template rules match elements in this namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(14).inferredLevel); // "[progress px:dotify-transform 70 pxi:css-to-obfl]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(15).inferredLevel); // "[progress pxi:css-to-obfl 1 p:xslt]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(16).inferredLevel); // "[progress pxi:css-to-obfl 1 css:parse-properties] Make css:display, css:render-table-by and css:table-header-policy attributes.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(17).inferredLevel); // "[progress pxi:css-to-obfl 2 css:render-table-by] Layout tables as lists.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(18).inferredLevel); // "[progress pxi:css-to-obfl 1 pxi:recursive-parse-stylesheet-and-make-pseudo-elements] Recursively parse stylesheet and make pseudo elements", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(19).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(20).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.INFO, job.getMessages().get(21).inferredLevel); // "[progress pxi:css-to-obfl 5 for-each.parse-properties-and-eval-string-set]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(22).inferredLevel); // "[progress for-each.parse-properties-and-eval-string-set 1/1 for-each.parse-properties-and-eval-string-set.iteration]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(23).inferredLevel); // "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:parse-properties]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(24).inferredLevel); // "[progress for-each.parse-properties-and-eval-string-set.iteration 50 css:eval-string-set]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(25).inferredLevel); // "[progress pxi:css-to-obfl 2 css:parse-content]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(26).inferredLevel); // "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.foreach-parse-properties] Make css:flow attributes.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(27).inferredLevel); // "[progress pxi:css-to-obfl.foreach-parse-properties 1/1 css:parse-properties]", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(28).inferredLevel); // "[progress pxi:css-to-obfl 1 css:flow-into] Extract named flows based on css:flow attributes.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(29).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(30).inferredLevel); // "[progress pxi:css-to-obfl 1 css:label-targets] Make css:id attributes.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(31).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(32).inferredLevel); // "[progress pxi:css-to-obfl 1 css:eval-target-content] Evaluate css:content elements.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(33).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(34).inferredLevel); // "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.for-each-parse-preserve-table-box]", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(35).inferredLevel); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box 1/1 pxi:css-to-obfl.for-each-parse-preserve-table-box.part]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(36).inferredLevel); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties] Make css:white-space, css:display, css:list-style-type, css:page-break-before and css:page-break-after attributes.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(37).inferredLevel); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:preserve-white-space] Make css:white-space elements from css:white-space attributes.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(38).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(39).inferredLevel); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-table-grid] Create table grid structures from HTML/DTBook tables.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(40).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(41).inferredLevel); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:make-boxes] Make css:box elements based on css:display and css:list-style-type attributes.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(42).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.INFO, job.getMessages().get(43).inferredLevel); // "[progress pxi:css-to-obfl.for-each-parse-preserve-table-box.part 20 css:parse-properties]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(44).inferredLevel); // "[progress pxi:css-to-obfl 8 css:eval-counter] Evaluate css:counter elements.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(45).inferredLevel); // "[progress pxi:css-to-obfl 2 css:flow-from] Evaluate css:flow elements.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(46).inferredLevel); // "[progress pxi:css-to-obfl 1 css:eval-target-text] Evaluate css:text elements.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(47).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.INFO, job.getMessages().get(48).inferredLevel); // "[progress pxi:css-to-obfl 1 pxi:css-to-obfl.for-each-anonymous-inline-boxes] Wrap/unwrap with inline css:box elements.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(49).inferredLevel); // "[progress pxi:css-to-obfl.for-each-anonymous-inline-boxes 1/1 css:make-anonyous-inline-boxes]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(50).inferredLevel); // "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.split-sections] Split flows into sections.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(51).inferredLevel); // "[progress pxi:css-to-obfl.split-sections 1/1 pxi:css-to-obfl.split-sections.section]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(52).inferredLevel); // "[progress pxi:css-to-obfl.split-sections.section 50 css:parse-counter-set] Make css:counter-set-page attributes.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(53).inferredLevel); // "[progress pxi:css-to-obfl.split-sections.section 50 css:split] Page and volume split.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(54).inferredLevel); // "[progress css:split 10 p:add-attribute]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(55).inferredLevel); // "[progress css:split 10 p:add-attribute]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(56).inferredLevel); // "[progress css:split 75 split-into-sections.xsl]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(57).inferredLevel); // "[progress css:split 5 p:filter]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(58).inferredLevel); // "[progress pxi:css-to-obfl 2 pxi:css-to-obfl.pages-and-volumes] Move css:page, css:counter-set-page and css:volume attributes to css:_ root element.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(59).inferredLevel); // "[progress pxi:css-to-obfl.pages-and-volumes 1/2]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(60).inferredLevel); // "[progress pxi:css-to-obfl.pages-and-volumes 1/2]", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(61).inferredLevel); // "[progress pxi:css-to-obfl 14 css:shift-string-set] Move css:string-set attributes.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(62).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(63).inferredLevel); // "[progress pxi:css-to-obfl 1 pxi:shift-obfl-marker] Move css:_obfl-marker attributes.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(64).inferredLevel); // "err:SXXP0005:The source document is in no namespace, but the template rules all expect elements in a namespace", Message.Level.ERROR);
		assertEquals(Message.Level.ERROR, job.getMessages().get(65).inferredLevel); // "[progress pxi:css-to-obfl 1 css:shift-id] Move css:id attributes to css:box elements.", Message.Level.INFO);
		assertEquals(Message.Level.ERROR, job.getMessages().get(66).inferredLevel); // "Creating an attribute here will fail if previous instructions create any children", Message.Level.ERROR);
		assertEquals(Message.Level.INFO, job.getMessages().get(67).inferredLevel); // "[progress pxi:css-to-obfl 8 pxi:css-to-obfl.for-each-black-box]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(68).inferredLevel); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 p:unwrap] Unwrap css:_ elements.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(69).inferredLevel); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 css:make-anonymous-black-boxes] Wrap inline css:box elements in block css:box elements where necessary.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(70).inferredLevel); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 p:unwrap] Unwrap css:_ elements.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(71).inferredLevel); // "[progress pxi:css-to-obfl.for-each-black-box 1/4 css:make-anonymous-black-boxes] Wrap inline css:box elements in block css:box elements where necessary.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(72).inferredLevel); // "[progress pxi:css-to-obfl 3 pxi:css-to-obfl.for-each-margin-attributes]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(73).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes 1/1 pxi:css-to-obfl.for-each-margin-attributes.item]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(74).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:parse-properties] Make css:margin-*, css:border-*, css:border-bottom and css:text-indent attributes.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(75).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:adjust-boxes] Adjust boxes.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(76).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 css:new-definition] New definition.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(77).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 10 p:delete] Remove text nodes from block boxes with no line boxes.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(78).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 20 pxi:propagate-page-break] Resolve css:page-break-before=avoid and css:page-break-after=always.", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(79).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 2 p:delete] Move css:page-break-after=avoid to last descendant block", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(80).inferredLevel); // "[progress pxi:css-to-obfl.for-each-margin-attributes.item 8]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(81).inferredLevel); // "[progress pxi:css-to-obfl 26 css-to-obfl.xsl]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(82).inferredLevel); // "[progress pxi:css-to-obfl 10] add <marker class=\"foo/prev\"/>", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(83).inferredLevel); // "[progress pxi:css-to-obfl 1]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(84).inferredLevel); // "[progress pxi:css-to-obfl 1] move table-of-contents elements to the right place", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(85).inferredLevel); // "[progress px:dotify-transform 1 pxi:obfl-normalize-space]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(86).inferredLevel); // "[progress px:dotify-transform 2 dotify:obfl-to-pef]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(87).inferredLevel); // "[progress px:dtbook-to-pef.convert 1 pef:add-metadata] Adding metadata to PEF", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(88).inferredLevel); // "[progress px:dtbook-to-pef.convert 1 p:add-attribute] Adding language attribute to PEF", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(89).inferredLevel); // "[progress 6 px:dtbook-to-pef.store] Storing PEF", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(90).inferredLevel); // "[progress px:dtbook-to-pef.store 90 pef:store]", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(91).inferredLevel); // "[progress pef:store 1 p:store] Storing PEF as 'file:/some/path/data/jobs/4b613329-3841-401f-8018-d20b5a9a8120/output/pef-output-dir//557889.bok227s.en.pef'", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(92).inferredLevel); // "[progress pef:store 17] Not storing as BRF", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(93).inferredLevel); // "[progress pef:store 82] Not including HTML preview", Message.Level.INFO);
		assertEquals(Message.Level.INFO, job.getMessages().get(94).inferredLevel); // "[progress px:dtbook-to-pef.store 10 p:sink]", Message.Level.INFO);
		
	}
	
}
