package org.daisy.pipeline.braille.pef;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.embosser.FileFormat;

import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import org.daisy.pipeline.braille.common.Provider.util.Memoize;
import org.daisy.pipeline.braille.common.Query;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "file-format-registry",
	service = { FileFormatRegistry.class }
)
public class FileFormatRegistry extends Memoize<Query,FileFormat> implements FileFormatProvider {
	
	private List<Provider<Query,FileFormat>> providers = new ArrayList<Provider<Query,FileFormat>>();
	private Provider<Query,FileFormat> dispatch = dispatch(providers);
	
	@Reference(
		name = "FileFormatProvider",
		unbind = "-",
		service = FileFormatProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addProvider(FileFormatProvider p) {
		providers.add(p);
	}
	
	public Iterable<FileFormat> _get(Query q) {
		return dispatch.get(q);
	}
}
