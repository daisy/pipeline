package org.daisy.common.properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyPublisherTest {
	PropertyTracker tracker;
	Property prop;
	PropertyPublisher publisher;


	private static String BUNDLE_NAME="name";	
	private static long BUNDLE_ID=1L;	
	private static String PROPERTY_NAME="property1";	
	private static String PROPERTY_VALUE="value";	

	@Before
	public void setUp(){
		this.tracker= new PropertyTracker();	
		this.publisher = new PropertyPublisher(tracker);	
		this.prop=new Property.Builder().withValue(PROPERTY_VALUE).withPropertyName(PROPERTY_NAME).withBundleId(BUNDLE_ID).withBundleName(BUNDLE_NAME).build();

	}

	/**
	 * Tests 'publish'.
	 *
	 * @see org.org.daisy.common.properties.PropertyPublisher#publish(Property)
	 */
	@Test
	public void publish() throws Exception {
		this.publisher.publish(this.prop);	
		Assert.assertEquals(this.tracker.getProperty(PROPERTY_NAME,BUNDLE_NAME),this.prop);
	}

	/**
	 * Tests 'unpublish'.
	 *
	 * @see org.org.daisy.common.properties.PropertyPublisher#unpublish(Property)
	 */
	@Test
	public void unpublish() throws Exception {
		this.publisher.publish(this.prop);	
		this.publisher.unpublish(this.prop);	
		Assert.assertNull(this.tracker.getProperty(PROPERTY_NAME,BUNDLE_NAME));
	}

}
