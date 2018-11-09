package org.daisy.dotify.formatter.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.formatter.PageTemplateBuilder;
import org.daisy.dotify.api.formatter.StringField;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMaker;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMaker;
import org.daisy.dotify.api.translator.TextBorderFactoryMaker;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.core.PageTemplate;
import org.junit.Test;

public class LayoutMasterTest {

	@Test
	public void testFlowHeight() {
		FormatterContext fcontext = new FormatterContext(
				BrailleTranslatorFactoryMaker.newInstance(),
				TextBorderFactoryMaker.newInstance(), MarkerProcessorFactoryMaker.newInstance(), 
				new FormatterConfiguration.Builder("sv-SE", "bypass").build()
		);
			
		LayoutMaster master = new LayoutMaster(fcontext, new LayoutMasterProperties.Builder(10, 10).build());
		// always apply template
		PageTemplateBuilder p = master.newTemplate(null);

		p.addToHeader(new FieldList.Builder(Arrays.asList(new StringField("11"))).build());
		p.addToHeader(new FieldList.Builder(Arrays.asList(new StringField("12"))).build());
		p.addToHeader(new FieldList.Builder(Arrays.asList(new StringField("13"))).build());
		
		p.addToFooter(new FieldList.Builder(Arrays.asList(new StringField("11"))).build());
		p.addToFooter(new FieldList.Builder(Arrays.asList(new StringField("12"))).build());
		p.addToFooter(new FieldList.Builder(Arrays.asList(new StringField("13"))).build());

		PageTemplate p2 = master.getTemplate(1);
		assertEquals(4, master.getFlowHeight(p2));
	}

	@Test
	public void testFlowHeightWithFlowIntoHeaderFooter() {
		FormatterContext fcontext = new FormatterContext(
				BrailleTranslatorFactoryMaker.newInstance(),
				TextBorderFactoryMaker.newInstance(), MarkerProcessorFactoryMaker.newInstance(), 
				new FormatterConfiguration.Builder("sv-SE", "bypass").build()
		);
			
		LayoutMaster master = new LayoutMaster(fcontext, new LayoutMasterProperties.Builder(10, 10).build());
		// always apply template
		PageTemplateBuilder p = master.newTemplate(null);

		p.addToHeader(new FieldList.Builder(Arrays.asList(new StringField("11"))).build());
		p.addToHeader(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("12"))).build());
		p.addToHeader(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("13"))).build());
		
		p.addToFooter(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("11"))).build());
		p.addToFooter(new FieldList.Builder(Arrays.asList(NoField.getInstance(), new StringField("12"))).build());
		p.addToFooter(new FieldList.Builder(Arrays.asList(new StringField("13"))).build());

		PageTemplate p2 = master.getTemplate(1);
		assertEquals(8, master.getFlowHeight(p2));
	}

}
