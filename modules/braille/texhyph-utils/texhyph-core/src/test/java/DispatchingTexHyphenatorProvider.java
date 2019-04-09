import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.common.TransformProvider.util.Dispatch;
import org.daisy.pipeline.braille.tex.TexHyphenator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;

@Component(
	name = "dispatching-tex-hyphenator-provider",
	service = { DispatchingTexHyphenatorProvider.class }
)
public class DispatchingTexHyphenatorProvider extends Dispatch<TexHyphenator> {
	
	public DispatchingTexHyphenatorProvider() {
		super(null);
	}
	
	private List<TransformProvider<TexHyphenator>> dispatch = new ArrayList<TransformProvider<TexHyphenator>>();
	
	@Reference(
		name = "TexHyphenatorProvider",
		unbind = "-",
		service = TexHyphenator.Provider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addProvider(TexHyphenator.Provider p) {
		dispatch.add(p);
	}
	
	public Iterable<TransformProvider<TexHyphenator>> _dispatch() {
		return dispatch;
	}
	
	public TransformProvider<TexHyphenator> withContext(Logger context) {
		return TransformProvider.util.dispatch(dispatch);
	}
}
