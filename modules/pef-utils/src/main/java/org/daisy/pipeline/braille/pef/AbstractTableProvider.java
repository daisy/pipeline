package org.daisy.pipeline.braille.pef;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.base.Optional;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;

public abstract class AbstractTableProvider implements TableProvider {
	
	protected abstract Iterable<Table> _get(Query query);
	
	private final Map<String,Table> tablesFromId = new HashMap<String,Table>();

	@Override
	public Collection<FactoryProperties> list() {
		// only list cached tables in preview-table option
		return Collections.unmodifiableCollection(tablesFromId.values());
	}

	@Override
	public Table newFactory(String identifier) {
		MutableQuery q = mutableQuery();
		q.add("id", identifier);
		try {
			return get(q).iterator().next(); }
		catch (NoSuchElementException e) {
			return null; }
	}

	@Override
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
