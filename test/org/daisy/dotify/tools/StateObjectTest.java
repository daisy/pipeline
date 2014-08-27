package org.daisy.dotify.tools;
import static org.junit.Assert.assertTrue;

import org.daisy.dotify.tools.StateObject;
import org.junit.Test;

public class StateObjectTest {
	
	@Test
	public void testUnopenedState() {
		StateObject s = new StateObject();
		assertTrue(!s.isOpen());
		assertTrue(!s.isClosed());
		s.assertNotOpen();
		s.assertUnopened();
	}
	
	@Test
	public void testOpenState() {
		StateObject s = new StateObject();
		s.open();
		assertTrue(s.isOpen());
		assertTrue(!s.isClosed());
		s.assertOpen();
	}
	
	@Test
	public void testClosedState() {
		StateObject s = new StateObject();
		s.open();
		s.close();
		assertTrue(s.isClosed());
		assertTrue(!s.isOpen());
		s.assertNotOpen();
		s.assertClosed();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testIllegalUnopenedStateClosed() {
		StateObject s = new StateObject();
		s.assertClosed();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testIllegalUnopenedStateOpen() {
		StateObject s = new StateObject();
		s.assertOpen();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testIllegalOpenStateClosed() {
		StateObject s = new StateObject();
		s.open();
		s.assertClosed();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testIllegalOpenStateNotOpen() {
		StateObject s = new StateObject();
		s.open();
		s.assertNotOpen();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testIllegalCloseStateUnopened() {
		StateObject s = new StateObject();
		s.close();
		s.assertUnopened();
	}
	
	@Test (expected=IllegalStateException.class)
	public void testIllegalCloseStateOpen() {
		StateObject s = new StateObject();
		s.close();
		s.assertOpen();
	}

}
