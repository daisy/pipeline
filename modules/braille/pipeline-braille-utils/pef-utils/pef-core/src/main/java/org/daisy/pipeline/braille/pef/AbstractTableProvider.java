package org.daisy.pipeline.braille.pef;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Optional;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.braille.api.table.Table;

public abstract class AbstractTableProvider implements TableProvider /*, org.daisy.braille.api.table.TableProvider */ {
	
	protected abstract Iterable<Table> _get(Query query);
	
	private final Map<String,Table> tablesFromId = new HashMap<String,Table>();
	
	/*
	public Collection<FactoryProperties> list() {
		return new ImmutableList.Builder<FactoryProperties>().addAll(tablesFromId.values()).build();
	}
	
	public Table newFactory(String identifier) {
		MutableQuery q = mutableQuery();
		q.add("id", identifier);
		try {
			return get(q).iterator().next(); }
		catch (NoSuchElementException e) {
			return null; }
	}
	*/
	
	public final Iterable<Table> get(Query query) {
		MutableQuery q = mutableQuery(query);
		if (q.containsKey("id")) {
			String id = q.removeOnly("id").getValue().get();
			if (q.isEmpty()) {
				Table table = tablesFromId.get(id);
				if (table != null)
					return Collections.singleton(table); }
			else
				return empty; }
		return cache(_get(query));
	}
	
	private final static Iterable<Table> empty = Optional.<Table>absent().asSet();
	
	private Iterable<Table> cache(final Iterable<Table> tables) {
		return new Iterable<Table>() {
			public Iterator<Table> iterator() {
				return new Iterator<Table>() {
					Iterator<Table> i = null;
					public boolean hasNext() {
						if (i == null) i = tables.iterator();
						return i.hasNext();
					}
					public Table next() {
						Table t;
						if (i == null) i = tables.iterator();
						t = i.next();
						tablesFromId.put(t.getIdentifier(), t);
						return t;
					}
					public void remove() {
						if (i == null) i = tables.iterator();
						i.remove();
					}
				};
			}
		};
	}
}
