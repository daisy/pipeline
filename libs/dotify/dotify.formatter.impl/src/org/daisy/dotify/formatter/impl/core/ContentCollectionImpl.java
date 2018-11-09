package org.daisy.dotify.formatter.impl.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.ContentCollection;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;


/**
 * Provides a content collection to be used when placing e.g. footnotes.
 * 
 * @author Joel Håkansson
 */
public class ContentCollectionImpl extends FormatterCoreImpl implements ContentCollection  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2198713822437968076L;
	private final Map<String, Item> items;
	private final Stack<Item> open;
	
	public ContentCollectionImpl(FormatterCoreContext fc) {
		super(fc, true);
		this.items = new LinkedHashMap<>();
		this.open = new Stack<>();
	}
	
	public boolean containsItemID(String id) {
		return items.containsKey(id);
	}
	
	public Set<String> getItemList() {
		return items.keySet();
	}
	
	public String getRefForID(String id) {
		return items.get(id).id;
	}
	
	public List<Block> getBlocks(String id) {
		Item i = items.get(id);
		if (i!=null) {
			return subList(i.from, i.to);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public void startItem(BlockProperties props) {
		String id = props.getTextBlockProperties().getIdentifier();
		if (id!=null) {
			open.push(new Item(id, size()));
			if (items.put(id, open.peek())!=null) {
				throw new RuntimeException("Identifier is not unique: " + id);
			}
		}
		startBlock(props);
	}
	
	@Override
	public void endItem() {
		endBlock();
		Item i = items.get(open.pop().id);
		i.to = size();
	}
	
	private static class Item {
		private final String id;
		/**
		 * fromIndex, inclusive
		 */
		private final int from;
		/**
		 * toIndex, exclusive
		 */
		private int to;
		
		Item(String id, int start) {
			this.id = id;
			this.from = start;
		}
	}

}
