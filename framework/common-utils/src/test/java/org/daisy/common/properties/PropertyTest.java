package org.daisy.common.properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertyTest {
	Property property;
	Property property2;
	private static String BUNDLE_NAME="name";	
	private static long BUNDLE_ID=1L;	
	private static String PROPERTY_NAME="property";	
	private static String PROPERTY_VALUE="value";	

	@Before
	public void setUp(){
		this.property=new Property.Builder().withValue(PROPERTY_VALUE).withPropertyName(PROPERTY_NAME).withBundleId(BUNDLE_ID).withBundleName(BUNDLE_NAME).build();
		this.property2=new Property.Builder().withValue("other").withPropertyName(PROPERTY_NAME).withBundleId(BUNDLE_ID).withBundleName(BUNDLE_NAME).build();
	}
	/**
	 * Tests 'getBundleName'.
	 *
	 * @see org.org.daisy.common.properties.Property#getBundleName()
	 */
	@Test
	public void getBundleName() throws Exception {
		Assert.assertEquals(this.property.getBundleName(),BUNDLE_NAME);
	}

	/**
	 * Tests 'getBundleId'.
	 *
	 * @see org.org.daisy.common.properties.Property#getBundleId()
	 */
	@Test
	public void getBundleId() throws Exception {
		Assert.assertEquals(this.property.getBundleId(),BUNDLE_ID);
	}

	/**
	 * Tests 'getPropertyName'.
	 *
	 * @see org.org.daisy.common.properties.Property#getPropertyName()
	 */
	@Test
	public void getPropertyName() throws Exception {
		Assert.assertEquals(this.property.getPropertyName(),PROPERTY_NAME);
	}

	/**
	 * Tests 'getValue'.
	 *
	 * @see org.org.daisy.common.properties.Property#getValue()
	 */
	@Test
	public void getValue() throws Exception {
		Assert.assertEquals(this.property.getValue(),PROPERTY_VALUE);
	}
	
}
