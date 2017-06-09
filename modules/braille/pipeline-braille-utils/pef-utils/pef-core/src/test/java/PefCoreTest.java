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

import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class PefCoreTest extends AbstractTest {
	
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
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			brailleModule("css-core"),
			"org.daisy.braille:braille-utils.api:?",
			"org.daisy.braille:braille-utils.impl:?"
		};
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
