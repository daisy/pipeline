package org.daisy.common.spi;

import java.util.Map;

public interface ServiceWithProperties {
	
	public void spi_deactivate();
	
	public Map spi_getProperties();
	
}
