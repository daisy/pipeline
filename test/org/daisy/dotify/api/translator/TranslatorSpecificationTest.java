package org.daisy.dotify.api.translator;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;

import org.daisy.dotify.api.translator.TranslatorSpecification;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TranslatorSpecificationTest {

	public TranslatorSpecificationTest() {
		
	}
	
	@Test
	public void testOrder() {
		TranslatorSpecification s1 = new TranslatorSpecification("sv", "uncontracted");
		TranslatorSpecification s2 = new TranslatorSpecification("en", "bypass");
		TranslatorSpecification s3 = new TranslatorSpecification("sv", "bypass");
		ArrayList<TranslatorSpecification> a = new ArrayList<>();
		a.add(s1);
		a.add(s2);
		a.add(s3);
		Collections.sort(a);
		assertEquals(s2, a.get(0));
		assertEquals(s3, a.get(1));
		assertEquals(s1, a.get(2));
	}
	
	@Test
	public void testEqual() {
		TranslatorSpecification s1 = new TranslatorSpecification("sv", "uncontracted");
		TranslatorSpecification s2 = new TranslatorSpecification("sv", "uncontracted");
		assertEquals(s1, s2);
	}

}
