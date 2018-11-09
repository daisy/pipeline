package org.daisy.dotify.api.paper;

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
	
	/**
	 * Returns true if user papers are supported, false otherwise. If this method
	 * returns false, {@link #addNewSheetPaper(String, String, Length, Length)},
	 * {@link #addNewRollPaper(String, String, Length)},
	 * {@link #addNewTractorPaper(String, String, Length, Length)},
	 * {@link #isRemovable(Paper)},
	 * {@link #remove(Paper)} 
	 *  should not be used.
	 * 
	 * @return true if user papers are supported, false otherwise
	 */
	public boolean supportsUserPapers();
	
	/**
	 * Adds a new sheet paper to the collection.
	 * @param name the name
	 * @param desc the description
	 * @param width the width
	 * @param height the height
	 * @return true if the paper was added successfully, false otherwise
	 */
	public boolean addNewSheetPaper(String name, String desc, Length width, Length height);
	
	/**
	 * Adds a new tractor paper to the collection.
	 * @param name the name
	 * @param desc the description
	 * @param across the length across the feed
	 * @param along the length along the feed
	 * @return true if the paper was added successfully, false otherwise
	 */
	public boolean addNewTractorPaper(String name, String desc, Length across, Length along);
	
	/**
	 * Adds a new roll paper to the collection.
	 * @param name the name
	 * @param desc the description
	 * @param across the length across the feed
	 * @return true if the paper was added successfully, false otherwise
	 */
	public boolean addNewRollPaper(String name, String desc, Length across);

	/**
	 * Returns true if the paper can be removed, false otherwise
	 * @param paper the paper
	 * @return true if the paper can be removed, false otherwise
	 */
	public boolean isRemovable(Paper paper);

	/**
	 * Removes the specified paper from the collection.
	 * @param p the paper to remove
	 * @return true if the paper was successfully removed, false otherwise
	 */
	public boolean remove(Paper p);

	//TODO: is needed? public Collection<Paper> listRemovable();

}
