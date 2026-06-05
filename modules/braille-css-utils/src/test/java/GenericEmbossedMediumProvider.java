import java.util.Collections;
import java.util.NoSuchElementException;

import cz.vutbr.web.css.MediaQuery;

import org.daisy.pipeline.braille.css.EmbossedMedium;
import org.daisy.pipeline.braille.css.EmbossedMedium.EmbossedMediumBuilder;
import org.daisy.pipeline.css.MediumProvider;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "GenericEmbossedMediumProvider",
	service = { MediumProvider.class }
)
public class GenericEmbossedMediumProvider implements MediumProvider {
	@Override
	public Iterable<EmbossedMedium> get(MediaQuery query) {
		try {
			return Collections.singleton(
				(EmbossedMedium)new EmbossedMediumBuilder().parse(query).build());
		} catch (IllegalArgumentException e) {
			return Collections.emptyList();
		}
	}
}
