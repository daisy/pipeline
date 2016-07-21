import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.daisy.braille.api.embosser.FileFormat;
import org.daisy.braille.api.table.Table;
import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.pef.FileFormatProvider;
import org.daisy.pipeline.braille.pef.TableProvider;

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

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PefCoreTest {
	
	@Test
	public void testBrailleUtilsTableCatalog() {
		Provider<Query,Table> provider = getTableProvider();
		MutableQuery q = mutableQuery();
		q.add("id", "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US");
		Table table = provider.get(q).iterator().next();
		assertEquals("FOOBAR", table.newBrailleConverter().toText("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Test
	public void testLocaleBasedTableProviderEn() {
		Provider<Query,Table> provider = getTableProvider();
		Table table = provider.get(query("(locale:en)")).iterator().next();
		assertEquals("FOOBAR", table.newBrailleConverter().toText("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Test
	public void testLocaleBasedTableProviderNl() {
		Provider<Query,Table> provider = getTableProvider();
		Table table = provider.get(query("(locale:nl)")).iterator().next();
		assertEquals("foobar", table.newBrailleConverter().toText("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Test
	public void testBrailleUtilsFileFormatCatalog() {
		Provider<Query,FileFormat> provider = getFileFormatProvider();
		MutableQuery q = mutableQuery();
		q.add("format", "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRF");
		q.add("table", "org_daisy.EmbosserTableProvider.TableType.MIT");
		provider.get(q).iterator().next();
	}
	
	@Test
	public void testBrailleUtilsEmbosserAsFileFormatCatalog() {
		Provider<Query,FileFormat> provider = getFileFormatProvider();
		MutableQuery q = mutableQuery();
		q.add("embosser", "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_200");
		q.add("locale", "nl");
		provider.get(q).iterator().next();
	}
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			felixDeclarativeServices(),
			domTraversalPackage(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				brailleModule("common-utils"),
				brailleModule("css-core"),
				mavenBundle("org.daisy.braille:braille-utils.api:?"),
				mavenBundle("org.daisy.braille:braille-utils.impl:?"),
				// logging
				logbackClassic())
		);
	}
	
	@Inject
	BundleContext context;
	
	private Provider<Query,Table> getTableProvider() {
		List<TableProvider> providers = new ArrayList<TableProvider>();
		try {
			for (ServiceReference<? extends TableProvider> ref : context.getServiceReferences(TableProvider.class, null))
				providers.add(context.getService(ref)); }
		catch (InvalidSyntaxException e) {
			throw new RuntimeException(e); }
		return dispatch(providers);
	}
	
	private Provider<Query,FileFormat> getFileFormatProvider() {
		List<FileFormatProvider> providers = new ArrayList<FileFormatProvider>();
		try {
			for (ServiceReference<? extends FileFormatProvider> ref : context.getServiceReferences(FileFormatProvider.class, null))
				providers.add(context.getService(ref)); }
		catch (InvalidSyntaxException e) {
			throw new RuntimeException(e); }
		return dispatch(providers);
	}
}
