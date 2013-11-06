package org.daisy.dotify.api.engine;

import org.daisy.dotify.api.writer.PagedMediaWriter;

public interface FormatterEngineFactoryMakerService {

	public FormatterEngine newFormatterEngine(String locale, String mode, PagedMediaWriter writer);

}
