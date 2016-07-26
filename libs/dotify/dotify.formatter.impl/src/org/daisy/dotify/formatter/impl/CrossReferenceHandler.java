package org.daisy.dotify.formatter.impl;

import java.util.HashSet;
import java.util.Set;

class CrossReferenceHandler {
	private final LookupHandler<String, Integer> pageRefs;
	private final LookupHandler<String, Integer> volumeRefs;
	private final LookupHandler<Integer, Iterable<AnchorData>> anchorRefs;
	private final LookupHandler<String, Integer> variables;
	private final LookupHandler<SheetIdentity, Boolean> breakable;
	private final static String VOLUMES_KEY = "volumes";
	private final static String SHEETS_IN_VOLUME = "sheets-in-volume-";
	private final static String SHEETS_IN_DOCUMENT = "sheets-in-document";
	private final static String PAGES_IN_VOLUME = "pages-in-volume-";
	private final static String PAGES_IN_DOCUMENT = "pages-in-document";
	private Set<String> pageIds;
	
	CrossReferenceHandler() {
		this.pageRefs = new LookupHandler<>();
		this.volumeRefs = new LookupHandler<>();
		this.anchorRefs = new LookupHandler<>();
		this.variables = new LookupHandler<>();
		this.breakable = new LookupHandler<>();
		this.pageIds = new HashSet<>();
	}
	
	/**
	 * Gets the volume for the specified identifier.
	 * @param refid the identifier to get the volume for
	 * @return returns the volume number, one-based
	 */
	public Integer getVolumeNumber(String refid) {
		return volumeRefs.get(refid);
	}
	
	void setVolumeNumber(String refid, int volume) {
		volumeRefs.put(refid, volume);
	}
	
	/**
	 * Gets the page number for the specified identifier.
	 * @param refid the identifier to get the page for
	 * @return returns the page number, one-based
	 */
	public Integer getPageNumber(String refid) {
		return pageRefs.get(refid);
	}
	
	void setPageNumber(String refid, int page) {
		pageRefs.put(refid, page);
		if (!pageIds.add(refid)) {
			throw new IllegalArgumentException("Identifier not unique: " + refid);
		}
	}
	
	public Iterable<AnchorData> getAnchorData(int volume) {
		return anchorRefs.get(volume);
	}
	
	void setAnchorData(int volume, Iterable<AnchorData> data) {
		anchorRefs.put(volume, data);
	}
	
	void setVolumeCount(int volumes) {
		variables.put(VOLUMES_KEY, volumes);
	}
	
	void setSheetsInVolume(int volume, int value) {
		variables.put(SHEETS_IN_VOLUME+volume, value);
	}
	
	void setSheetsInDocument(int value) {
		variables.put(SHEETS_IN_DOCUMENT, value);
	}
	
	void setPagesInVolume(int volume, int value) {
		variables.put(PAGES_IN_VOLUME+volume, value);
	}
	
	void setPagesInDocument(int value) {
		variables.put(PAGES_IN_DOCUMENT, value);
	}
	
	void keepBreakable(SheetIdentity ident, boolean value) {
		breakable.keep(ident, value);
	}
	
	void commitBreakable() {
		breakable.commit();
	}
	

	/**
	 * Gets the number of volumes.
	 * @return returns the number of volumes
	 */
	int getVolumeCount() {
		return variables.get(VOLUMES_KEY, 1);
	}
	
	int getSheetsInVolume(int volume) {
		return variables.get(SHEETS_IN_VOLUME+volume, 0);
	}

	int getSheetsInDocument() {
		return variables.get(SHEETS_IN_DOCUMENT, 0);
	}
	
	int getPagesInVolume(int volume) {
		return variables.get(PAGES_IN_VOLUME+volume, 0);
	}

	int getPagesInDocument() {
		return variables.get(PAGES_IN_DOCUMENT, 0);
	}
	
	boolean getBreakable(SheetIdentity ident) {
		return breakable.get(ident, true);
	}

	boolean isDirty() {
		return pageRefs.isDirty() || volumeRefs.isDirty() || anchorRefs.isDirty() || variables.isDirty() || breakable.isDirty();
	}
	
	void setDirty(boolean value) {
		pageRefs.setDirty(value);
		volumeRefs.setDirty(value);
		anchorRefs.setDirty(value);
		variables.setDirty(value);
		breakable.setDirty(value);
	}
	
	void resetUniqueChecks() {
		pageIds = new HashSet<>();
	}

}
