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

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class PefCoreTest extends AbstractTest {
	
	@Inject
	public DispatchingTableProvider tableProvider;
	
	@Test
	public void testBrailleUtilsTableCatalog() {
		MutableQuery q = mutableQuery();
		q.add("id", "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US");
		Table table = tableProvider.get(q).iterator().next();
		assertEquals("FOOBAR", table.newBrailleConverter().toText("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Test
	public void testLocaleBasedTableProviderEn() {
		Table table = tableProvider.get(query("(locale:en)")).iterator().next();
		assertEquals("FOOBAR", table.newBrailleConverter().toText("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Test
	public void testLocaleBasedTableProviderNl() {
		Table table = tableProvider.get(query("(locale:nl)")).iterator().next();
		assertEquals("foobar", table.newBrailleConverter().toText("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Inject
	public DispatchingFileFormatProvider formatProvider;
	
	@Test
	public void testBrailleUtilsFileFormatCatalog() {
		MutableQuery q = mutableQuery();
		q.add("format", "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRF");
		q.add("table", "org_daisy.EmbosserTableProvider.TableType.MIT");
		formatProvider.get(q).iterator().next();
	}
	
	@Test
	public void testBrailleUtilsEmbosserAsFileFormatCatalog() {
		MutableQuery q = mutableQuery();
		q.add("embosser", "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_200");
		q.add("locale", "nl");
		formatProvider.get(q).iterator().next();
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("common-utils"),
			pipelineModule("file-utils"),
			"org.daisy.braille:braille-utils.api:?",
			"org.daisy.braille:braille-utils.impl:?",
			"org.daisy.braille:braille-utils.pef-tools:?",
			"org.daisy.pipeline:calabash-adapter:?",
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Service-Component", "OSGI-INF/dispatching-table-provider.xml,"
		                                   + "OSGI-INF/dispatching-file-format-provider.xml");
		return probe;
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			// FIXME: BrailleUtils needs older version of jing
			mavenBundle("org.daisy.libs:jing:20120724.0.0"),
			composite(super.config()));
	}
}
