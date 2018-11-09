package org.daisy.dotify.tasks.impl.input.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.daisy.streamline.api.tasks.TaskSystemException;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TableSplitTest {
/*
	@Test
	public void testOBFL() throws IOException, TaskSystemException, URISyntaxException {
		//Setup

		File res = File.createTempFile(this.getClass().getName(), ".tmp");
		Map<String, Object> params = new HashMap<>();
		params.put("table-split-columns", 2);
		TestHelper.toObfl("resource-files/table-split-input.xml", res, "sv-SE", params);
		System.out.println(res);
		//Test
		assertTrue(true);
	}*/
	
	@Test
	public void testTableSplitNoSplit() throws IOException, TaskSystemException, URISyntaxException, TransformerException {
		//Setup
		Map<String, Object> params = new HashMap<>();
		params.put("table-split-columns", 3);
		//Test
		TestHelper.runXSLT("resource-files/table-split-input.xml", "resource-files/test_table_grid.xsl", "resource-files/table-no-split-expected.xml", params, false);
	}

	@Test
	public void testTableSplitSimple() throws IOException, TaskSystemException, URISyntaxException, TransformerException {
		//Setup
		Map<String, Object> params = new HashMap<>();
		params.put("table-split-columns", 2);
		//Test
		TestHelper.runXSLT("resource-files/table-split-input.xml", "resource-files/test_table_grid.xsl", "resource-files/table-split-expected.xml", params, false);
	}
	
	@Test
	public void testTableSplitComplex() throws IOException, TaskSystemException, URISyntaxException, TransformerException {
		//Setup
		Map<String, Object> params = new HashMap<>();
		params.put("table-split-columns", 2);
		//Test
		TestHelper.runXSLT("resource-files/table-split-complex-input.xml", "resource-files/test_table_grid.xsl", "resource-files/table-split-complex-expected.xml", params, false);
	}

}
