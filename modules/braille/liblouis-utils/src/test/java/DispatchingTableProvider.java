import java.util.ArrayList;
import java.util.List;

import org.daisy.braille.api.table.Table;

import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.Provider.util.Dispatch;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.pef.TableProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "dispatching-table-provider",
	service = { DispatchingTableProvider.class }
)
public class DispatchingTableProvider extends Dispatch<Query,Table> {
	
	private List<Provider<Query,Table>> dispatch = new ArrayList<Provider<Query,Table>>();
	
	@Reference(
		name = "TableProvider",
		unbind = "-",
		service = TableProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addProvider(TableProvider p) {
		dispatch.add(p);
	}
	
	public Iterable<Provider<Query,Table>> dispatch() {
		return dispatch;
	}
}
