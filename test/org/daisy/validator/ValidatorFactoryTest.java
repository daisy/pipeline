package org.daisy.validator;

import org.junit.Test;
import static org.junit.Assert.*;
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