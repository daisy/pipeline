package org.daisy.pipeline.braille.tex;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class TexHyphenatorCoreTest {
	
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
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				brailleModule("common-utils"),
				brailleModule("css-core"),
				mavenBundle("com.googlecode.texhyphj:texhyphj:?"),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"),
				// logging
				logbackClassic()),
			bundle("reference:file:" + PathUtils.getBaseDir() + "/target/test-classes/table_paths/")
		);
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
