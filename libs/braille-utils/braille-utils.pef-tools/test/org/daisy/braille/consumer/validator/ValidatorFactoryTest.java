package org.daisy.braille.consumer.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
public class ValidatorFactoryTest {
	
	@Test
	public void testFactory_01() {
		ValidatorFactory vf = ValidatorFactory.newInstance();
		assertEquals(2, vf.list().size());
	}
	
	@Test
	public void testFactory_02() {
		ValidatorFactory vf = ValidatorFactory.newInstance();
		assertNotNull(vf.newValidator("application/x-pef+xml"));
	}

}