package org.daisy.common.properties;

import org.junit.Assert;
import org.junit.Test;

public class PropertyPublisherFactoryTest {
	PropertyTracker tracker= new PropertyTracker();
	/**
	 * Tests 'newPropertyPublisher'.
	 *
	 * @see org.org.daisy.common.properties.PropertyPublisherFactory#newPropertyPublisher()
	 */
	@Test
	public void newPropertyPublisher() throws Exception {
		PropertyPublisherFactory factory= new PropertyPublisherFactory();
		factory.setTracker(tracker);
		factory.newPropertyPublisher();
		//so far so good...
	}

	/**
	 * Tests 'newPropertyPublisher'.
	 *
	 * @see org.org.daisy.common.properties.PropertyPublisherFactory#newPropertyPublisher()
	 */
	@Test
	public void newPropertyPublisherFail() throws Exception {
		PropertyPublisherFactory factory= new PropertyPublisherFactory();
		try{
			factory.newPropertyPublisher();
			Assert.fail("Exception should've been thrown");
		}catch(Exception e){
		}
	}
	
}
