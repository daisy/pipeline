package org.daisy.dotify.formatter.test;

import java.io.IOException;

import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TableTest extends AbstractFormatterEngineTest {
	
	@Test
	public void testSimpleTable() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/simple-table-input.obfl",
		        "resource-files/dp2/simple-table-expected.pef", false);
	}

	@Test
	public void testTableColSpacing() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-col-spacing-input.obfl",
		        "resource-files/dp2/table-col-spacing-expected.pef", false);
	}

	@Test
	public void testTableHeaderUnderline() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-header-underline-input.obfl",
		        "resource-files/dp2/table-header-underline-expected.pef", false);
	}

	@Test
	public void testTableFixedWidth() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-fixed-width-input.obfl",
		        "resource-files/dp2/table-fixed-width-expected.pef", false);
	}

	@Test
	public void testTableAutoColumnWidth() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-auto-column-width-input.obfl",
		        "resource-files/dp2/table-auto-column-width-expected.pef", false);
	}
	@Ignore // not implemented yet
	@Test
	public void testTableAutoColumnWidthCellWrapping() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-auto-column-width-cell-wrapping-input.obfl",
		        "resource-files/dp2/table-auto-column-width-cell-wrapping-expected.pef", false);
	}
	@Ignore // not implemented yet
	@Test
	public void testTableHeaderRepeat() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-header-repeat-input.obfl",
		        "resource-files/dp2/table-header-repeat-expected.pef", false);
	}

	@Test
	public void testTableLeaders() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-leaders-input.obfl",
		        "resource-files/dp2/table-leaders-expected.pef", false);
	}

	@Test
	public void testTableColspan() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-colspan-input.obfl",
		        "resource-files/dp2/table-colspan-expected.pef", false);
	}
	@Ignore // not implemented yet
	@Test
	public void testTableColspanFill() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/dp2/table-colspan-fill-input.obfl",
		        "resource-files/dp2/table-colspan-fill-expected.pef", false);
	}
	
	@Test
	public void testTables() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-input.obfl", "resource-files/table/tables-expected.pef", false);
	}
	
	@Test
	public void testTableWithColspan() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-colspan-input.obfl", "resource-files/table/tables-colspan-expected.pef", false);
	}
	
	@Test
	public void testTableWithRowspan() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-rowspan-input.obfl", "resource-files/table/tables-rowspan-expected.pef", false);
	}
	
	//This test can be re-enabled when either:
	//	1) pre-translated has been implemented in the translator
	//	2) the Swedish translator supports 0x200b (zero width space)
	@Test @Ignore
	public void testTableWithMultilineCells() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-multiline-input.obfl", "resource-files/table/tables-multiline-expected.pef", false);
	}
	
	@Test
	public void testTableColSpacing2() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-col-spacing-input.obfl", "resource-files/table/tables-col-spacing-expected.pef", false);
	}
	
	@Test
	public void testTableLeaderAlign() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-leader-input.obfl", "resource-files/table/tables-leader-expected.pef", false);
	}
	
	@Test
	public void testTableLeader2Colspacing() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-leader2-input.obfl", "resource-files/table/tables-leader2-expected.pef", false);
	}
	
	@Test
	public void testTableLeader3Colspacing() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-leader3-input.obfl", "resource-files/table/tables-leader3-expected.pef", false);
	}
	
	@Test
	public void testTableAlign() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-align-input.obfl", "resource-files/table/tables-align-expected.pef", false);
	}
	
	@Test
	public void testTableBorder() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-borders-input.obfl", "resource-files/table/tables-borders-expected.pef", false);
	}
	
	@Test
	public void testTableKeepRow() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-keep-row-input.obfl", "resource-files/table/tables-keep-row-expected.pef", false);
	}
	
	@Test
	public void testTableRowSpacing() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-row-spacing-input.obfl", "resource-files/table/tables-row-spacing-expected.pef", false);
	}
	
	@Test
	public void testTableShrink() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-shrink-input.obfl", "resource-files/table/tables-shrink-expected.pef", false);
	}
	
	/*
	 * When forced line breaks were taken into account, this test broke. It could be updated with contents that 
	 * doesn't have any forced breaks, as the test was originally written for the purpose of checking that
	 * failing solutions didn't result in an exception. However, the test as it is also seems to trigger another
	 * bug when the forced line breaks were introduced, namely an empty page (see issue #186).
	 */
	@Test @Ignore()
	public void testTableShrink_02() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-shrink2-input.obfl", "resource-files/table/tables-shrink2-expected.pef", false);
	}
	
	@Test
	public void testTableMarker() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-marker-input.obfl", "resource-files/table/tables-marker-expected.pef", false);
	}
	
	@Test
	public void testTableAnchor() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-anchor-input.obfl", "resource-files/table/tables-anchor-expected.pef", false);
	}
	
	@Test
	public void testTableCellBorder() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-border-input.obfl", "resource-files/table/tables-border-expected.pef", false);
	}
	
	@Test
	public void testTableCellBorder2() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-border2-input.obfl", "resource-files/table/tables-border2-expected.pef", false);
	}
	
	@Test
	public void testTableMissingCell() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-missing-cell-input.obfl", "resource-files/table/tables-missing-cell-expected.pef", false);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testTableMissingCell2() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-missing-cell2-input.obfl", "resource-files/table/tables-missing-cell2-expected.pef", false);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testTableMissingCell3() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-missing-cell3-input.obfl", "resource-files/table/tables-missing-cell3-expected.pef", false);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testTableMissingCell4() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-missing-cell4-input.obfl", "resource-files/table/tables-missing-cell4-expected.pef", false);
	}
	
	@Test
	public void testTableMissingCell5() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/tables-missing-cell5-input.obfl", "resource-files/table/tables-missing-cell5-expected.pef", false);
	}
	
	@Test
	public void testTableInBlock() throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF("resource-files/table/table-in-block-input.obfl", "resource-files/table/table-in-block-expected.pef", false);
	}
	
}
