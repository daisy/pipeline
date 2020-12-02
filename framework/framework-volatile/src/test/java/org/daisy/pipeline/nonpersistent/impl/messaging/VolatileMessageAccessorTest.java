package org.daisy.pipeline.nonpersistent.impl.messaging;

import java.util.List;
import java.util.Set;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.pipeline.event.MessageAccessorFromStorage;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.nonpersistent.impl.messaging.VolatileMessageStorage;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class VolatileMessageAccessorTest   {

	MessageStorage storage = VolatileMessageStorage.getInstance();

	Message m1, m2, m3;

	MessageAccessor accessor;

	@Before
	public void setUp() {
		String id = JobIdFactory.newId().toString();
		MessageAppender m;
		m = new MessageBuilder().withText("message1")
				.withLevel(Level.INFO).withOwnerId(id)
				.build();
		m.close();
		m1 = (Message)m;
		m = new MessageBuilder().withText("message2")
				.withLevel(Level.ERROR).withOwnerId(id)
				.build();
		m.close();
		m2 = (Message)m;
		m = new MessageBuilder().withText("message3")
				.withLevel(Level.WARNING).withOwnerId(id)
				.build();
		m.close();
		m3 = (Message)m;
		storage.add(m1);
		storage.add(m2);
		storage.add(m3);
		accessor = new MessageAccessorFromStorage(id, storage);
	}

	@After
	public void tearDown(){
		VolatileMessageStorage.getInstance().removeAll();
	}

	@Test
	public void getMessagesFromWarn() {

		List<Message> out=accessor.getWarnings();
		Assert.assertEquals(2,out.size());
		Assert.assertEquals(out.get(0).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(1).getLevel(),Level.WARNING);
	}

	@Test
	public void getMessagesFromInfo() {
		List<Message> out=accessor.getInfos();
		Assert.assertEquals(3,out.size());
		Assert.assertEquals(out.get(0).getLevel(),Level.INFO);
		Assert.assertEquals(out.get(1).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(2).getLevel(),Level.WARNING);
	}
	@Test
	public void getAll() {
		List<Message> out=accessor.getAll();			
		Assert.assertEquals(3,out.size());
	}

	@Test
	public void createFilterRange() {
		List<Message> out= accessor.createFilter().inRange(2,3).getMessages();
		Assert.assertEquals(out.get(0).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(1).getLevel(),Level.WARNING);
	}

	@Test
	public void createFilterAll() {
		List<Message> out= accessor.createFilter().greaterThan(-1).getMessages();
		Assert.assertEquals(out.get(0).getLevel(),Level.INFO);
		Assert.assertEquals(out.get(1).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(2).getLevel(),Level.WARNING);
	}
	@Test
	public void createFilterGreaterThan() {
		// "greater than 1" means: get all new messages with a sequence number greater
		// than 1, including their parents and all messages that were updated (closed
		// and/or progress updated) since message 1 was opened
		List<Message> out= accessor.createFilter().greaterThan(1).getMessages();
		Assert.assertEquals(out.get(0).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(1).getLevel(),Level.WARNING);
	}
	@Test
	public void createFilterRangeAndLevel() {
		Set<Level> levels = Sets.newHashSet();
		levels.add(Level.ERROR);
		List<Message> out= accessor.createFilter().inRange(2,2).getMessages();
		Assert.assertEquals(out.get(0).getLevel(),Level.ERROR);
	}

	@Test
	public void createFilterLevel() {
		Set<Level> levels = Sets.newHashSet();
		levels.add(Level.INFO);
		levels.add(Level.ERROR);
		List<Message> out= accessor.createFilter().filterLevels(levels).getMessages();
		Assert.assertEquals(2,out.size());
		Assert.assertEquals(out.get(0).getLevel(),Level.INFO);
		Assert.assertEquals(out.get(1).getLevel(),Level.ERROR);
	}

	@Test (expected = IndexOutOfBoundsException.class)
	public void createRangeStartOutOfIndex() {
		List<Message> out= accessor.createFilter().inRange(-1,2).getMessages();
	}

	@Test (expected = IllegalArgumentException.class)
	public void createRangeSanity() {
		List<Message> out= accessor.createFilter().inRange(10,2).getMessages();
	}
	@Test
	public void filterWithEmptyList() {
		VolatileMessageStorage.getInstance().removeAll();
		List<Message> out= accessor.createFilter().greaterThan(0).getMessages();
		Assert.assertEquals(out.size(),0);
	}
}
