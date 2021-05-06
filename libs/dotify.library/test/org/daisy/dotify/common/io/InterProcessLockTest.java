package org.daisy.dotify.common.io;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
public class InterProcessLockTest {

    @Test
    public void testLock_01() throws IOException {
        InterProcessLock lock = new InterProcessLock(InterProcessLockTest.class.getCanonicalName());
        assertTrue(lock.lock());
        assertFalse(lock.lock());
        lock.unlock();
    }
}
