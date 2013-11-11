package org.daisy.dotify.api.engine;

import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.api.translator.MarkerProcessorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.writer.PagedMediaWriter;

public interface FormatterEngineFactoryService {

	public FormatterEngine newFormatterEngine(String locale, String mode, PagedMediaWriter writer);

	/**
	 * Provides a method to set the FormatterFactory directly. This
	 * is included in the interface as a compromise between OSGi visibility and
	 * SPI compatibility.
	 * 
	 * In an OSGi context, the implementation should not set the implementation
	 * directly, but attach it to the service using DS.
	 * 
	 * @param formatterFactory
	 */
	public void setFormatterFactory(FormatterFactory formatterFactory); 	// note
																						// that
																						// there
																						// isn't
																						// a
																						// service
																						// for
																						// this...

	/**
	 * Provides a method to set the MarkerProcessorFactoryService directly. This
	 * is included in the interface as a compromise between OSGi visibility and
	 * SPI compatibility.
	 * 
	 * In an OSGi context, the implementation should not set the implementation
	 * directly, but attach it to the service using DS.
	 * 
	 * @param mp
	 */
	public void setMarkerProcessor(MarkerProcessorFactoryMakerService mp);

	/**
	 * Provides a method to set the TextBorderFactoryMakerService directly. This
	 * is included in the interface as a compromise between OSGi visibility and
	 * SPI compatibility.
	 * 
	 * In an OSGi context, the implementation should not set the implementation
	 * directly, but attach it to the service using DS.
	 * 
	 * @param tbf
	 */
	public void setTextBorderFactoryMaker(TextBorderFactoryMakerService tbf);

	/**
	 * Provides a method to set the ExpressionFactory directly. This
	 * is included in the interface as a compromise between OSGi visibility and
	 * SPI compatibility.
	 * 
	 * In an OSGi context, the implementation should not set the implementation
	 * directly, but attach it to the service using DS.
	 * 
	 * @param ef
	 */
	public void setExpressionFactory(ExpressionFactory ef);

}
