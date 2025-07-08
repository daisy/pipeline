import javax.inject.Inject;

import org.daisy.dotify.api.table.Table;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.pef.TableRegistry;
import org.daisy.pipeline.css.MediaQueryParser;
import org.daisy.pipeline.css.MediumProvider;
import org.daisy.pipeline.junit.AbstractTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PefCoreTest extends AbstractTest {
	
	@Inject
	public TableRegistry tableProvider;
	
	@Test
	public void testTableCatalog() {
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
	public MediumProvider formatProvider;
	
	@Test
	public void testFileFormatCatalog() {
		formatProvider.get(
			MediaQueryParser.parse(
				"(format: org_daisy\\.BrailleEditorsFileFormatProvider\\.FileType\\.BRF) AND " +
				"(table: org_daisy\\.EmbosserTableProvider\\.TableType\\.MIT)")
		).iterator().next();
	}
	
	@Test
	public void testEmbosserAsFileFormatCatalog() {
		formatProvider.get(
			MediaQueryParser.parse(
				"(embosser: com_braillo\\.BrailloEmbosserProvider\\.EmbosserType\\.BRAILLO_200) AND " +
				"(locale: nl)")
		).iterator().next();
		formatProvider.get(
			MediaQueryParser.parse(
				"(embosser: com_braillo\\.BrailloEmbosserProvider\\.EmbosserType\\.BRAILLO_200) AND " +
				"(table: nl)")
		).iterator().next();
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("braille-common"),
			pipelineModule("file-utils"),
			"org.daisy.dotify:dotify.library:?",
			"org.daisy.pipeline:calabash-adapter:?",
			// because the exclusion of com.fasterxml.woodstox:woodstox-core from the dotify.library
			// dependencies causes stax2-api to be excluded too
			"org.codehaus.woodstox:stax2-api:jar:?",
		};
	}
}
