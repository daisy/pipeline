package org.daisy.pipeline.nonpersistent.impl.messaging;

import java.util.List;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.nonpersistent.impl.messaging.VolatileMessageStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VolatileMessageStorageTest {

	String jobId;

	Message m1, m2, m3;

	VolatileMessageStorage storage = VolatileMessageStorage.getInstance();

	@Before
	public void setUp() {
		jobId = JobIdFactory.newId().toString();
		MessageAppender m;
		m = new MessageBuilder().withText("message1")
				.withLevel(Level.INFO).withOwnerId(jobId).build();
		m.close();
		m1 = (Message)m;
		m = new MessageBuilder().withText("message2")
				.withLevel(Level.ERROR).withOwnerId(jobId)
				.build();
		m.close();
		m2 = (Message)m;
		m = new MessageBuilder().withText("message3")
				.withLevel(Level.DEBUG).withOwnerId(jobId)
				.build();
		m.close();
		m3 = (Message)m;
	}
	@After
	public void tearDown(){
		VolatileMessageStorage.getInstance().removeAll();
	}

	@Test
	public void add() {
		storage.add(m1);
		storage.add(m2);
		Assert.assertEquals(storage.get(jobId).size(), 2);
	}

	@Test
	public void addDebug() {
		storage.add(m3);
		Assert.assertEquals(0,storage.get(jobId).size());
	}
	@Test
	public void get() {
		storage.add(m1);
		storage.add(m2);
		List<Message> list= storage.get(jobId);
		Assert.assertEquals(0,list.get(0).getSequence());
		Assert.assertEquals(1,list.get(1).getSequence());
	}

	@Test
	public void getEmpty() {
		List<Message> list=storage.get("newid");
		Assert.assertEquals(0,list.size());
	}

	@Test
	public void remove() {
		storage.add(m1);
		storage.add(m2);
		storage.remove(jobId);
		Assert.assertEquals(0,storage.get(jobId).size());
	}

}
