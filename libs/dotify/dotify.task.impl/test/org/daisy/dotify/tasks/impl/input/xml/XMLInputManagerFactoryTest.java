package org.daisy.dotify.tasks.impl.input.xml;

import static org.junit.Assert.assertTrue;

import org.daisy.dotify.common.text.FilterLocale;
import org.daisy.streamline.api.media.FormatIdentifier;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.junit.Test;

@SuppressWarnings("javadoc")
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
		assertTrue(factory.newTaskGroup(new TaskGroupSpecification.Builder(FormatIdentifier.with("xml"), FormatIdentifier.with("obfl"), filter.toString()).build())!=null);
	}

	
	@Test
	public void testLocateInputManagerForSwedish() {
		//Setup
		XMLInputManagerFactory factory = new XMLInputManagerFactory();
		FilterLocale filter = FilterLocale.parse("sv-SE");
		
		//Test
		assertTrue(factory.newTaskGroup(new TaskGroupSpecification.Builder(FormatIdentifier.with("xml"), FormatIdentifier.with("obfl"), filter.toString()).build())!=null);
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
		assertTrue(factory.newTaskGroup(new TaskGroupSpecification.Builder(FormatIdentifier.with("xml"), FormatIdentifier.with("obfl"), filter.toString()).build())!=null);
	}
}
