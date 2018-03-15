package org.daisy.pipeline.braille.dotify.impl;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableMap;

import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

public interface DotifyOBFLTransform {
	
	@Component(
		name = "org.daisy.pipeline.braille.dotify.impl.DotifyOBFLTransform.Provider",
		service = {
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<Transform> {
		
		private URI href;
		
		@Activate
		private void activate(ComponentContext context, final Map<?,?> properties) {
			href = asURI(context.getBundleContext().getBundle().getEntry("xml/transform/obfl-to-pef.xpl"));
		}
		
		private final static Iterable<Transform> empty = Iterables.<Transform>empty();
		
		protected Iterable<Transform> _get(Query query) {
			try {
				MutableQuery q = mutableQuery(query);
				if (q.containsKey("formatter"))
					if (!"dotify".equals(q.removeOnly("formatter").getValue().get()))
						return empty;
				Iterator<Feature> input = q.get("input").iterator();
				while (input.hasNext())
					if ("obfl".equals(input.next().getValue().get())) {
						input.remove();
						for (Feature f : q.removeAll("output"))
							if (!("pef".equals(f.getValue().get()) || "braille".equals(f.getValue().get())))
								return empty;
						q.add("output", "braille");
						return Iterables.<Transform>of(
							logCreate((Transform)new TransformImpl(q.toString()))); }}
			catch (IllegalStateException e) {}
			return empty;
		}
		
		private class TransformImpl extends AbstractTransform {
			
			private final XProc xproc;
			
			private TransformImpl(String textTransformQuery) {
				xproc = new XProc(href, null, ImmutableMap.of("text-transform", textTransformQuery));
			}
			
			@Override
			public XProc asXProc() {
				return xproc;
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return Objects.toStringHelper("o.d.p.b.dotify.impl.DotifyOBFLTransform$Provider$TransformImpl");
			}
		}
	}
}
