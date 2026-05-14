package org.daisy.pipeline.braille.pef.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableProvider;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.pef.AbstractTableProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

// Note that this class doesn't use instances of the
// org.daisy.dotify.api.table.TableCatalog interface. It uses instances of the
// more low-level org.daisy.dotify.api.table.TableProvider directly. The name
// was chosen because it more or less provides the same functionality as a
// org.daisy.dotify.api.table.TableCatalog, except that it's based on the
// query syntax instead of ID's.
@Component(
	name = "org.daisy.pipeline.braille.pef.impl.TableCatalog",
	service = { org.daisy.pipeline.braille.pef.TableProvider.class }
)
public class TableCatalog extends AbstractTableProvider {

	@Override
	protected Iterable<Table> _get(Query query) {
		MutableQuery q = mutableQuery(query);
		if (q.containsKey("id")) {
			String id = q.removeOnly("id").getValue().get();
			if (q.isEmpty())
				return get(id); }
		return empty;
	}
	
	private Iterable<Table> get(String id) {
		for (TableProvider p : providers)
			for (FactoryProperties fp : p.list())
				if (fp.getIdentifier().equals(id))
					return Optional.fromNullable(p.newFactory(id)).asSet();
		return empty;
	}

	// list all available tables, not only those from the cache (for unit test)
	Collection<FactoryProperties> listAll() {
		return providers.stream().flatMap(p -> p.list().stream()).collect(Collectors.toList());
	}

	private final static Iterable<Table> empty = Optional.<Table>absent().asSet();
	
	private final List<TableProvider> providers = new ArrayList<>();
	
	@Reference(
		name = "TableProvider",
		unbind = "-",
		service = TableProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addTableProvider(TableProvider provider) {
		providers.add(provider);
	}
	
	public void removeTableProvider(TableProvider provider) {
		providers.remove(provider);
	}
}
