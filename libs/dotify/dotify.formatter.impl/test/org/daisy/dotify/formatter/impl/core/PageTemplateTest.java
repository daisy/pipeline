package org.daisy.dotify.formatter.impl.core;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.formatter.StringField;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class PageTemplateTest {

	@Test
	public void testAnalyzeHeader_01() {
		PageTemplate p = new PageTemplate(1);
		p.addToHeader(
			new FieldList.Builder(
				Arrays.asList(new StringField("11"))
			).build()
		);

		p.addToHeader(
			new FieldList.Builder(
				Arrays.asList(NoField.getInstance(), new StringField("12"))
			).build()
		);
		
		p.addToHeader(
			new FieldList.Builder(
				Arrays.asList(NoField.getInstance(), new StringField("13"))
			).build()
		);
		
		int actual = p.validateAndAnalyzeHeader();
		assertEquals(2, actual);
		assertEquals(0, p.validateAndAnalyzeFooter());
	}
	
	@Test
	public void testAnalyzeFooter_01() {
		PageTemplate p = new PageTemplate(1);
		p.addToFooter(
			new FieldList.Builder(
				Arrays.asList(NoField.getInstance(), new StringField("11"))
			).build()
		);

		p.addToFooter(
			new FieldList.Builder(
				Arrays.asList(NoField.getInstance(), new StringField("12"))
			).build()
		);
		
		p.addToFooter(
			new FieldList.Builder(
				Arrays.asList(new StringField("13"))
			).build()
		);
		
		int actual = p.validateAndAnalyzeFooter();
		assertEquals(2, actual);
		assertEquals(0, p.validateAndAnalyzeHeader());
	}

}
