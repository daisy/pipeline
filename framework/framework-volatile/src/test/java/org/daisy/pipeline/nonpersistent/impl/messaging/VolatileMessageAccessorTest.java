package org.daisy.pipeline.nonpersistent.impl.messaging;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor.MessageFilter;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.nonpersistent.impl.messaging.VolatileMessageAccessor;
import org.daisy.pipeline.nonpersistent.impl.messaging.VolatileMessageStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

public class VolatileMessageAccessorTest   {

	VolatileMessageStorage storage = VolatileMessageStorage.getInstance();
	Message m1;

	Message m2;

	Message m3;

	VolatileMessageAccessor accessor;
	@Before
	public void setUp() {
		String id = JobIdFactory.newId().toString();
		m1 = new Message.MessageBuilder().withText("message1")
				.withLevel(Level.INFO).withSequence(0).withJobId(id).build();

		m2 = new Message.MessageBuilder().withText("message2")
				.withLevel(Level.ERROR).withSequence(1).withJobId(id)
				.build();

		m3 = new Message.MessageBuilder().withText("message3")
				.withLevel(Level.WARNING).withSequence(2).withJobId(id)
				.build();


		storage.add(m1);
		storage.add(m2);
		storage.add(m3);
		accessor = new VolatileMessageAccessor(JobIdFactory.newIdFromString(id));
	}

	@After
	public void tearDown(){
		VolatileMessageStorage.getInstance().removeAll();
	}

	@Test
	public void getMessagesFromWarn() {

		List<Message> out=accessor.getMessagesFrom(Level.WARNING);			
		Assert.assertEquals(2,out.size());
		Assert.assertEquals(out.get(0).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(1).getLevel(),Level.WARNING);
	}

	@Test
	public void getMessagesFromInfo() {
		List<Message> out=accessor.getMessagesFrom(Level.INFO);			
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
	public void delete() {
		accessor.delete();
		List<Message> out=accessor.getAll();			
		Assert.assertEquals(0,out.size());
	}

	@Test
	public void createFilterRange() {
		List<Message> out= accessor.createFilter().inRange(1,3).getMessages();
		Assert.assertEquals(out.get(0).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(1).getLevel(),Level.WARNING);
	}

	@Test
	public void createFilterFromMessageAll() {
		List<Message> out= accessor.createFilter().greaterThan(-1).getMessages();
		Assert.assertEquals(out.get(0).getLevel(),Level.INFO);
		Assert.assertEquals(out.get(1).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(2).getLevel(),Level.WARNING);
	}
	@Test
	public void createFilterFromMessage() {
		List<Message> out= accessor.createFilter().greaterThan(0).getMessages();
		Assert.assertEquals(out.get(0).getLevel(),Level.ERROR);
		Assert.assertEquals(out.get(1).getLevel(),Level.WARNING);
	}
	@Test
	public void createFilterRangeAndLevel() {
		Set<Level> levels = Sets.newHashSet();
		levels.add(Level.ERROR);
		List<Message> out= accessor.createFilter().inRange(1,2).getMessages();
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

	@Test (expected = IndexOutOfBoundsException.class)
	public void createRangeEndOutOfIndex() {
		List<Message> out= accessor.createFilter().inRange(0,10000).getMessages();
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
