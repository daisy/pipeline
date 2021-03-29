import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.embosser.FileFormat;

import org.daisy.pipeline.braille.common.Provider;
import org.daisy.pipeline.braille.common.Provider.util.Dispatch;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.pef.FileFormatProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "dispatching-file-format-provider",
	service = { DispatchingFileFormatProvider.class }
)
public class DispatchingFileFormatProvider extends Dispatch<Query,FileFormat> {
	
	private List<Provider<Query,FileFormat>> dispatch = new ArrayList<Provider<Query,FileFormat>>();
	
	@Reference(
		name = "FileFormatProvider",
		unbind = "-",
		service = FileFormatProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addProvider(FileFormatProvider p) {
		dispatch.add(p);
	}
	
	public Iterable<Provider<Query,FileFormat>> dispatch() {
		return dispatch;
	}
}
