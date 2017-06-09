import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import org.daisy.pipeline.braille.tex.TexHyphenator;

import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class TexHyphenatorCoreTest extends AbstractTest {
	
	@Inject
	BundleContext context;
	
	@Test
	public void testHyphenate() {
		TransformProvider<TexHyphenator> provider = getProvider(TexHyphenator.class, TexHyphenator.Provider.class);
		assertEquals("foo\u00ADbar",
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
	                         .asFullHyphenator()
		                     .transform("foobar"));
		assertEquals("foo-\u200Bbar",
		             provider.get(query("(table:'foobar.tex')")).iterator().next()
                     .asFullHyphenator()
		                     .transform("foo-bar"));
		assertEquals("foo\u00ADbar",
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
                     .asFullHyphenator()
		                     .transform("foobar"));
		assertEquals("foo-\u200Bbar",
		             provider.get(query("(table:'foobar.properties')")).iterator().next()
                     .asFullHyphenator()
		                     .transform("foo-bar"));
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-core"),
			"com.googlecode.texhyphj:texhyphj:?",
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Import-Package", "org.daisy.pipeline.braille.tex");
		probe.setHeader("Service-Component", "OSGI-INF/table_paths.xml");
		return probe;
	}
	
	private <T extends Transform> TransformProvider<T> getProvider(Class<T> transformerClass, Class<? extends TransformProvider<T>> providerClass) {
		List<TransformProvider<T>> providers = new ArrayList<TransformProvider<T>>();
		try {
			for (ServiceReference<? extends TransformProvider<T>> ref : context.getServiceReferences(providerClass, null))
				providers.add(context.getService(ref)); }
		catch (InvalidSyntaxException e) {
			throw new RuntimeException(e); }
		return dispatch(providers);
	}
}
