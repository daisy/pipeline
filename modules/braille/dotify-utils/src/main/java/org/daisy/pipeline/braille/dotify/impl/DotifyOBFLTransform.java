package org.daisy.pipeline.braille.dotify.impl;

import java.net.URI;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.file.URLs;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.calabash.CxEvalBasedTransformer;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

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
		protected void activate(final Map<?,?> properties) {
			href = URLs.asURI(URLs.getResourceFromJAR("xml/transform/obfl-to-pef.xpl", DotifyOBFLTransform.class));
		}
		
		private final static Iterable<Transform> empty = Iterables.<Transform>empty();
		
		protected Iterable<Transform> _get(Query query) {
			try {
				MutableQuery q = mutableQuery(query);
				boolean obfl = false;
				for (Feature f : q.removeAll("input")) {
					String i = f.getValue().get();
					if ("obfl".equals(i))
						obfl = true;
					else if (!"text-css".equals(i))
						return empty; }
				if (!obfl)
					return empty;
				for (Feature f : q.removeAll("output"))
					if (!("pef".equals(f.getValue().get()) || "braille".equals(f.getValue().get())))
						return empty;
				if (!q.isEmpty())
					return empty;
				return Iterables.<Transform>of(
					logCreate((Transform)new TransformImpl())); }
			catch (IllegalStateException e) {}
			return empty;
		}
		
		private class TransformImpl extends AbstractTransform implements XProcStepProvider {
			
			private TransformImpl() {
			}
			
			@Override
			public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
				return new CxEvalBasedTransformer(href, null, null).newStep(runtime, step, monitor, properties);
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return MoreObjects.toStringHelper("DotifyOBFLTransform$Provider$TransformImpl");
			}
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("DotifyOBFLTransform$Provider");
		}
	}
}
