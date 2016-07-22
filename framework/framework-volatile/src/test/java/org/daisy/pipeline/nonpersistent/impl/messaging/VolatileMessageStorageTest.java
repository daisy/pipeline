package org.daisy.pipeline.nonpersistent.impl.messaging;

import java.util.List;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.nonpersistent.impl.messaging.VolatileMessageStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VolatileMessageStorageTest {

	Message m1;

	Message m2;

	Message m3;

	VolatileMessageStorage storage = VolatileMessageStorage.getInstance();

	@Before
	public void setUp() {
		m1 = new Message.MessageBuilder().withText("message1")
				.withLevel(Level.INFO).withSequence(0).withJobId("id1").build();

		m2 = new Message.MessageBuilder().withText("message2")
				.withLevel(Level.ERROR).withSequence(1).withJobId("id1")
				.build();

		m3 = new Message.MessageBuilder().withText("message3")
				.withLevel(Level.DEBUG).withSequence(2).withJobId("id1")
				.build();

	}
	@After
	public void tearDown(){
		VolatileMessageStorage.getInstance().removeAll();
	}

	@Test
	public void add() {
		storage.add(m1);
		storage.add(m2);
		Assert.assertEquals(storage.get("id1").size(), 2);
	}

	@Test
	public void addAndWait() throws InterruptedException {
		VolatileMessageStorage.setTimeOut(1);
		storage.add(m1);
		Thread sleeper=new Thread() {
			public void run() {
				try {
					Thread.sleep(1100);
				} catch (InterruptedException e) {
				}
			}

		};
		sleeper.start();
		sleeper.join();
		Assert.assertEquals(storage.get("id1").size(), 0);

	}
	@Test
	public void addDebug() {
		storage.add(m3);
		Assert.assertEquals(0,storage.get("id1").size());
	}
	@Test
	public void get() {
		storage.add(m1);
		storage.add(m2);
		List<Message> list= storage.get("id1");
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
		storage.remove("id1");
		Assert.assertEquals(0,storage.get("id1").size());
	}

}
