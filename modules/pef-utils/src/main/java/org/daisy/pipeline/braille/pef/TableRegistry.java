package org.daisy.pipeline.braille.pef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;

import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import org.daisy.pipeline.braille.common.Provider.util.Memoize;
import org.daisy.pipeline.braille.common.Query;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "table-registry",
	service = { TableRegistry.class }
)
public class TableRegistry extends Memoize<Query,Table> implements TableProvider {
	
	private List<TableProvider> providers = new ArrayList<>();
	private Provider<Query,Table> dispatch = dispatch(providers);
	
	@Reference(
		name = "TableProvider",
		unbind = "-",
		service = TableProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addProvider(TableProvider p) {
		providers.add(p);
	}

	@Override
	public Iterable<Table> _get(Query q) {
		return dispatch.get(q);
	}

	@Override
	public Collection<FactoryProperties> list() {
		List<FactoryProperties> list = new ArrayList<>();
		for (TableProvider p : providers)
			list.addAll(p.list());
		return list;
	}

	@Override
	public Table newFactory(String identifier) {
		for (TableProvider p : providers) {
			Table t = p.newFactory(identifier);
			if (t != null)
				return t;
		}
		return null;
	}
}
