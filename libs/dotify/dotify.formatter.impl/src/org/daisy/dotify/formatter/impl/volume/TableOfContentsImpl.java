package org.daisy.dotify.formatter.impl.volume;

import java.util.LinkedHashMap;
import java.util.Set;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.TableOfContents;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.core.FormatterCoreImpl;


/**
 * Provides table of contents entries to be used when building a Table of Contents
 * @author Joel Håkansson
 */
public class TableOfContentsImpl extends FormatterCoreImpl implements TableOfContents  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2198713822437968076L;
	private final LinkedHashMap<String, String> refs;
	
	public TableOfContentsImpl(FormatterCoreContext fc) {
		super(fc);
		this.refs = new LinkedHashMap<>();
	}
	
	boolean containsTocID(String id) {
		return refs.containsKey(id);
	}
	
	Set<String> getTocIdList() {
		return refs.keySet();
	}
	
	String getRefForID(String id) {
		return refs.get(id);
	}

	@Override
	public void startEntry(String refId, BlockProperties props) {
		String tocId;
		do {
			tocId = ""+((int)Math.round(99999999*Math.random()));
		} while (containsTocID(tocId));
		if (refs.put(tocId, refId)!=null) {
			throw new RuntimeException("Identifier is not unique: " + tocId);
		}
		startBlock(props, tocId);
	}
	
	@Override
	public void endEntry() {
		endBlock();
	}

}
