package org.daisy.pipeline.braille.pef.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.pef.TableProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

// Note that although the name suggests it, this class doesn't use instances
// of the TableCatalog interface. It uses instances of the more low-level
// org.daisy.dotify.api.table.TableProvider directly. The name was chosen
// because it more or less provides the same functionality as a TableCatalog,
// except that it's based on the query syntax instead of ID's.
@Component(
	name = "org.daisy.pipeline.braille.pef.impl.BrailleUtilsTableCatalog",
	service = { TableProvider.class }
)
public class BrailleUtilsTableCatalog implements TableProvider {
	
	public Iterable<Table> get(Query query) {
		MutableQuery q = mutableQuery(query);
		if (q.containsKey("id")) {
			String id = q.removeOnly("id").getValue().get();
			if (q.isEmpty())
				return get(id); }
		return empty;
	}
	
	private Iterable<Table> get(String id) {
		for (org.daisy.dotify.api.table.TableProvider p : providers)
			for (FactoryProperties fp : p.list())
				if (fp.getIdentifier().equals(id))
					return Optional.fromNullable(p.newFactory(id)).asSet();
		return empty;
	}

	Collection<FactoryProperties> list() {
		return providers.stream().flatMap(p -> p.list().stream()).collect(Collectors.toList());
	}

	private final static Iterable<Table> empty = Optional.<Table>absent().asSet();
	
	private final List<org.daisy.dotify.api.table.TableProvider> providers
	= new ArrayList<org.daisy.dotify.api.table.TableProvider>();
	
	@Reference(
		name = "TableProvider",
		unbind = "-",
		service = org.daisy.dotify.api.table.TableProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addTableProvider(org.daisy.dotify.api.table.TableProvider provider) {
		providers.add(provider);
	}
	
	public void removeTableProvider(org.daisy.dotify.api.table.TableProvider provider) {
		providers.remove(provider);
	}
}
