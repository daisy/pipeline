package org.daisy.dotify.formatter.impl.page;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.formatter.PageTemplateBuilder;
import org.daisy.dotify.api.formatter.StringField;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMaker;
import org.daisy.dotify.api.translator.TextBorderFactoryMaker;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.search.DocumentSpace;
import org.daisy.dotify.formatter.impl.search.PageDetails;
import org.daisy.dotify.formatter.impl.search.PageId;
import org.daisy.dotify.formatter.impl.search.SequenceId;
import org.daisy.dotify.formatter.impl.search.Space;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class FieldResolverTest {

	@Test
	public void testGetWidth_01() throws TranslatorConfigurationException {
		FormatterContext fcontext = new FormatterContext(
			BrailleTranslatorFactoryMaker.newInstance(),
			TextBorderFactoryMaker.newInstance(), MarkerProcessorFactoryMaker.newInstance(), 
			new FormatterConfiguration.Builder("sv-SE", "bypass").build()
		);

		LayoutMaster master = new LayoutMaster(fcontext, 
				new LayoutMasterProperties.Builder(10, 8).build());
		// always apply template
		PageTemplateBuilder p = master.newTemplate(null);

		// Adds three rows of fields to the header, two which combine body text and header
		p.addToHeader(new FieldList.Builder(Arrays.asList(new StringField("1111"))).build());
		p.addToHeader(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("12"))).build());
		p.addToHeader(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("13"))).build());

		// Adds three rows of fields to the footer, two which combine body text and footer
		p.addToFooter(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("11"))).build());
		p.addToFooter(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("12"))).build());
		p.addToFooter(new FieldList.Builder(Arrays.asList(new StringField("1333"))).build());
		
		PageDetails details = new PageDetails(
				true, 
				new PageId(0, 0, 
					new SequenceId(
						0,
						new DocumentSpace(Space.BODY, null)
					)
				), 
				0);
		FieldResolver resolver = new FieldResolver(master, fcontext, null, details);

		assertEquals(8, resolver.getWidth(1, 0));
		assertEquals(8, resolver.getWidth(1, 1));
		assertEquals(10, resolver.getWidth(1, 2));
		assertEquals(10, resolver.getWidth(1, 3));
		assertEquals(8, resolver.getWidth(1, 4));
		assertEquals(8, resolver.getWidth(1, 5));

		// checking overflow as well
		assertEquals(8, resolver.getWidth(1, 6));
		assertEquals(8, resolver.getWidth(1, 7));
		assertEquals(10, resolver.getWidth(1, 8));
	}

}
