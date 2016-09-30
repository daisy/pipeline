package org.daisy.braille.api.paper;

import java.util.Collection;

/**
 * <p>
 * Provides an interface for a PaperCatalog service. The purpose of
 * this interface is to expose an implementation of a PaperCatalog
 * as an OSGi service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 */
public interface PaperCatalogService {
	
	/**
	 * Gets a paper by identifier.
	 * @param identifier the identifier of the paper
	 * @return returns the paper
	 */
	public Paper get(String identifier);
	
	/**
	 * Lists all available papers.
	 * @return returns a list of papers
	 */
	public Collection<Paper> list();
	
	/**
	 * Lists available papers that the filter accepts.
	 * @param filter the filter
	 * @return returns a list of papers
	 */
	public Collection<Paper> list(PaperFilter filter);

}
