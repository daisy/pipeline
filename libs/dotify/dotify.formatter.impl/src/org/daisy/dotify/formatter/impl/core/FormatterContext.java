package org.daisy.dotify.formatter.impl.core;

import java.util.HashMap;
import java.util.Map;

import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.LayoutMasterBuilder;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

/**
 * Provides formatter context data.
 * @author Joel Håkansson
 *
 */
public class FormatterContext extends FormatterCoreContext {
	private final Map<String, LayoutMaster> masters;
	private final Map<String, ContentCollectionImpl> collections;
	private final HashMap<String, TableOfContentsImpl> tocs;
	private final TransitionBuilderImpl transitionBuilder;
	

	public FormatterContext(BrailleTranslatorFactoryMakerService translatorFactory, TextBorderFactoryMakerService tbf, FormatterConfiguration config) {
		super(translatorFactory, tbf, config);
		this.masters = new HashMap<>();
		this.collections = new HashMap<>();
		this.tocs = new HashMap<>();
		this.transitionBuilder = new TransitionBuilderImpl(this);
	}
	
	public LayoutMasterBuilder newLayoutMaster(String name, LayoutMasterProperties properties) {
		LayoutMaster master = new LayoutMaster(this, properties);
		masters.put(name, master);
		return master;
	}
	
	public ContentCollectionImpl newContentCollection(String collectionId) {
		ContentCollectionImpl collection = new ContentCollectionImpl(this);
		collections.put(collectionId, collection);
		return collection;
	}
	
	public TableOfContentsImpl newTableOfContents(String tocName) {
		TableOfContentsImpl toc = new TableOfContentsImpl(this);
		tocs.put(tocName, toc);
		return toc;
	}
	
	public Map<String, LayoutMaster> getMasters() {
		return masters;
	}
	
	public Map<String, ContentCollectionImpl> getCollections() {
		return collections;
	}
	
	public Map<String, TableOfContentsImpl> getTocs() {
		return tocs;
	}
	
	public TransitionBuilderImpl getTransitionBuilder() {
		return transitionBuilder;
	}
	
}
