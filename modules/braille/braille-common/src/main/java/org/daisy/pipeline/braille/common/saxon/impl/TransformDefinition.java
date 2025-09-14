package org.daisy.pipeline.braille.common.saxon.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import com.xmlcalabash.core.XProcRuntime;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;

import org.daisy.common.spi.ServiceLoader;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.common.xproc.calabash.XProcRuntimeFactory;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.braille.common.LazyValue;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import org.daisy.pipeline.braille.common.calabash.impl.PxTransformStep;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pf:transform",
	service = { ExtensionFunctionProvider.class }
)
public class TransformDefinition extends ReflexiveExtensionFunctionProvider {

	@Override
	public Collection<ExtensionFunctionDefinition> getDefinitions() {
		return getDefinitions(null);
	}

	@Override
	public Collection<ExtensionFunctionDefinition> getDefinitions(Collection<Object> context) {
		XProcRuntime runtime = context != null
			? Iterables.getOnlyElement(Iterables.filter(context, XProcRuntime.class), null)
			: null;
		XProcMonitor monitor = context != null
			? Iterables.getOnlyElement(Iterables.filter(context, XProcMonitor.class), null)
			: null;
		Map<String,String> properties = context != null
			? (Map<String,String>)Iterables.getOnlyElement(Iterables.filter(context, Map.class), null)
			: null;
		Configuration saxonConfiguration = context != null
			? (Configuration)Iterables.getOnlyElement(Iterables.filter(context, Configuration.class), null)
			: null;
		if (runtime == null && saxonConfiguration == null)
			return Collections.emptySet();
		return new ReflexiveExtensionFunctionProvider() {{
			addExtensionFunctionDefinitionsFromClass(
				Transform.class,
				new Transform(runtime, monitor, properties, saxonConfiguration));
		}}.getDefinitions();
	}

	@Reference(
		name = "TransformProvider",
		unbind = "-",
		service = TransformProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	@SuppressWarnings(
		"unchecked" // safe cast to TransformProvider<Transform>
	)
	public void bindTransformProvider(TransformProvider<?> provider) {
		providers.add((TransformProvider<org.daisy.pipeline.braille.common.Transform>)provider);
		logger.debug("Adding Transform provider: {}", provider);
	}

	private List<TransformProvider<org.daisy.pipeline.braille.common.Transform>> providers = new ArrayList<>();
	private TransformProvider<org.daisy.pipeline.braille.common.Transform> provider
		= dispatch(providers).withContext(logger);

	private XProcRuntimeFactory runtimeFactory = null;

	public class Transform {

		private final LazyValue<PxTransformStep> xprocStep;
		private XProcRuntime closeRuntime = null;

		private Transform(XProcRuntime runtime,
		                  XProcMonitor monitor,
		                  Map<String,String> properties,
		                  Configuration saxonConfiguration) {
			// computed lazily in order to avoid circular dependency on SaxonConfigurator
			xprocStep = LazyValue.from(() -> {
				if (runtime == null)
					if (runtimeFactory == null)
						try {
							runtimeFactory = OSGiHelper.inOSGiContext()
								? OSGiHelper.getXProcRuntimeFactory()
								: SPIHelper.getXProcRuntimeFactory();
						} catch (NoSuchElementException e) {
							throw new UnsupportedOperationException(); // should not happen
						}
				return new PxTransformStep(
					runtime != null
						? runtime
						: runtimeFactory.newRuntime(saxonConfiguration, monitor, properties),
					null,
					monitor,
					properties,
					provider);
				}
			);
		}

		/**
		 * Simple wrapper around {@code PxTransformStep.run()}
		 */
		public void transform(String query, XMLInputValue<?> source, XMLOutputValue<?> result) {
			xprocStep
				.apply()
				.transform(ImmutableMap.of(new QName("query"), new InputValue<>(query),
				                           new QName("source"), source),
				           ImmutableMap.of(new QName("result"), result))
				.run();
		}

		@Override
		public void finalize() {
			if (closeRuntime != null)
				closeRuntime.close();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TransformDefinition.class);

	// static nested class in order to delay class loading
	private static abstract class OSGiHelper {

		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}

		static XProcRuntimeFactory getXProcRuntimeFactory() {
			BundleContext bc = FrameworkUtil.getBundle(TransformDefinition.class).getBundleContext();
			try {
				ServiceReference[] refs = bc.getServiceReferences(XProcRuntimeFactory.class.getName(), null);
				if (refs != null && refs.length > 0)
					return (XProcRuntimeFactory)bc.getService(refs[0]);
				else
					throw new NoSuchElementException();
			} catch (InvalidSyntaxException e) {
				throw new IllegalStateException(e); // should not happen
			}
		}
	}

	// static nested class in order to delay class loading
	private static abstract class SPIHelper {
		static XProcRuntimeFactory getXProcRuntimeFactory() {
			return ServiceLoader.load(XProcRuntimeFactory.class).iterator().next();
		}
	}
}
