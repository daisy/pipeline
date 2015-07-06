package org.daisy.dotify.impl.input.xml;

import static org.junit.Assert.assertTrue;

import org.daisy.dotify.api.cr.TaskGroupSpecification;
import org.daisy.dotify.common.text.FilterLocale;
import org.junit.Test;

public class XMLInputManagerFactoryTest {

	@Test
	public void testFactoryExists() {
		//Setup
		XMLInputManagerFactory factory = new XMLInputManagerFactory();
		
		//Test
		assertTrue(factory != null);
	}
	
	@Test
	public void testLocateInputManagerForEnglish() {
		//Setup
		XMLInputManagerFactory factory = new XMLInputManagerFactory();
		FilterLocale filter = FilterLocale.parse("en-US");
		
		//Test
		assertTrue(factory.newTaskGroup(new TaskGroupSpecification("xml", "obfl", filter.toString()))!=null);
	}

	
	@Test
	public void testLocateInputManagerForSwedish() {
		//Setup
		XMLInputManagerFactory factory = new XMLInputManagerFactory();
		FilterLocale filter = FilterLocale.parse("sv-SE");
		
		//Test
		assertTrue(factory.newTaskGroup(new TaskGroupSpecification("xml", "obfl", filter.toString()))!=null);
	}
	/*
	@Test
	public void testLocateInputManagerForSwedishFA44() throws UnsupportedLocaleException {
		//Setup
		DefaultInputManagerFactory factory = new DefaultInputManagerFactory();
		FilterLocale filter = FilterLocale.parse("sv-SE-FA44");
		
		//Test
		assertTrue(factory.newInputManager(filter)!=null);
	}
	*/
	@Test (expected=IllegalArgumentException.class)
	public void testLocateInputManagerForUnknownLocale() {
		//Setup
		XMLInputManagerFactory factory = new XMLInputManagerFactory();
		FilterLocale filter = FilterLocale.parse("fi");
		
		//Test
		assertTrue(factory.newTaskGroup(new TaskGroupSpecification("xml", "obfl", filter.toString()))!=null);
	}
}
