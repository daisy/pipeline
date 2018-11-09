package org.daisy.dotify.formatter.impl.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides the data needed for searching a document space.  
 * @author Joel Håkansson
 */
class DocumentSpaceData {

		final List<PageDetails> pageDetails;
		final Map<Integer, View<PageDetails>> volumeViews;
		final Map<Integer, View<PageDetails>> sequenceViews;
		
		DocumentSpaceData() {
			this.pageDetails = new ArrayList<>();
			this.volumeViews = new HashMap<>();
			this.sequenceViews = new HashMap<>();		
		}
	}