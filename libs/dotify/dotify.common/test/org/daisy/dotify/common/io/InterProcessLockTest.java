package org.daisy.dotify.common.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class InterProcessLockTest {

	@Test
	public void testLock_01() throws IOException {
		InterProcessLock lock = new InterProcessLock(InterProcessLockTest.class.getCanonicalName());
		assertTrue(lock.lock()); 
		assertFalse(lock.lock());
		lock.unlock();
	}
}
