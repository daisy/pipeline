package org.daisy.pipeline.braille.tex;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.bundlesAndDependencies;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackBundles;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
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
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
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
			logbackBundles(),
			felixDeclarativeServices(),
			mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject(),
			mavenBundle().groupId("com.googlecode.texhyphj").artifactId("texhyphj").versionAsInProject(),
			mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.antlr-runtime").versionAsInProject(),
			mavenBundle().groupId("org.daisy.libs").artifactId("jstyleparser").versionAsInProject(),
			mavenBundle().groupId("org.unbescape").artifactId("unbescape").versionAsInProject(),
			mavenBundle().groupId("org.daisy.braille").artifactId("braille-css").versionAsInProject(),
			mavenBundle().groupId("org.daisy.dotify").artifactId("dotify.api").versionAsInProject(),
			bundlesAndDependencies("org.daisy.pipeline.calabash-adapter"),
			brailleModule("common-utils"),
			brailleModule("css-core"),
			thisBundle(),
			bundle("reference:file:" + PathUtils.getBaseDir() + "/target/test-classes/table_paths/"),
			junitBundles()
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
