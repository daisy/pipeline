package org.daisy.dotify.tasks.tools;

import org.junit.Test;

import static org.junit.Assert.*;
public class XsltTaskTest {

	@Test
	public void testStringSplitter_01() {
		String input = "\t\t\t";
		String[] actuals = XsltTask.splitFields(input).toArray(new String[]{});
		String[] expecteds = new String[]{"", "", "", ""};
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testStringSplitter_02() {
		String input = "1\t2\t3";
		String[] actuals = XsltTask.splitFields(input).toArray(new String[]{});
		String[] expecteds = new String[]{"1", "2", "3"};
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testStringSplitter_03() {
		String input = "123";
		String[] actuals = XsltTask.splitFields(input).toArray(new String[]{});
		String[] expecteds = new String[]{"123"};
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testStringSplitter_04() {
		String input = "\t2\t";
		String[] actuals = XsltTask.splitFields(input).toArray(new String[]{});
		String[] expecteds = new String[]{"", "2", ""};
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testStringSplitter_05() {
		String input = "1\t2";
		String[] actuals = XsltTask.splitFields(input).toArray(new String[]{});
		String[] expecteds = new String[]{"1", "2"};
		assertArrayEquals(expecteds, actuals);
	}
}
