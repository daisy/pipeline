package org.daisy.pipeline.braille.liblouis.impl;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import static org.daisy.common.file.URIs.asURI;
import org.daisy.common.file.URLs;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.calabash.CxEvalBasedTransformer;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.of;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

public interface LiblouisMathMLTransform {
	
	public enum MathCode {
		NEMETH, UKMATHS, MARBURG, WOLUWE
	}
	
	@Component(
		name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisMathMLTransform.Provider",
		service = {
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<Transform> {
		
		private URI href;
		
		@Activate
		protected void activate(final Map<?,?> properties) {
			href = asURI(URLs.getResourceFromJAR("xml/translate-mathml.xpl", LiblouisMathMLTransform.class));
		}
		
		private final static Iterable<Transform> empty = Iterables.<Transform>empty();
		
		private final static List<String> supportedOutput = ImmutableList.of("braille");
		
		protected Iterable<Transform> _get(final Query query) {
			try {
				if ("mathml".equals(query.getOnly("input").getValue().get())) {
					for (Feature f : query.get("output"))
						if (!supportedOutput.contains(f.getValue().get()))
							return empty;
					if (query.containsKey("locale")) {
						Locale locale; {
							try {
								locale = parseLocale(query.getOnly("locale").getValue().get()); }
							catch (IllegalArgumentException e) {
								return empty; }
						}
						MathCode code = mathCodeFromLocale(locale);
						if (code != null)
							return of(logCreate((Transform)new TransformImpl(code))); }}}
			catch (IllegalStateException e) {}
			return empty;
		}
		
		private class TransformImpl extends AbstractTransform implements XProcStepProvider {
			
			private final MathCode code;
			private final Map<String,String> options;
			
			private TransformImpl(MathCode code) {
				this.code = code;
				options = ImmutableMap.of("math-code", code.name());
			}
			
			@Override
			public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
				return new CxEvalBasedTransformer(href, null, options).newStep(runtime, step);
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisMathMLTransform$Provider$TransformImpl")
					.add("mathCode", code);
			}
		}
		
		private static MathCode mathCodeFromLocale(Locale locale) {
			String language = locale.getLanguage().toLowerCase();
			String country = locale.getCountry().toUpperCase();
			if (language.equals("en")) {
				if (country.equals("GB"))
					return MathCode.UKMATHS;
				else
					return MathCode.NEMETH; }
			else if (language.equals("de"))
				return MathCode.MARBURG;
			else if (language.equals("nl"))
				return MathCode.WOLUWE;
			else
				return null;
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper(LiblouisMathMLTransform.Provider.class.getName());
		}
	}
}
