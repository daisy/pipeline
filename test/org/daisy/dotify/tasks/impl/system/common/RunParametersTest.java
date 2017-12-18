package org.daisy.dotify.tasks.impl.system.common;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.daisy.dotify.tasks.impl.system.common.RunParameters;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class RunParametersTest {

	@Test
	public void testWidth_01() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.PAGE_WIDTH, "10");
		map.put(RunParameters.INNER_MARGIN, "0");
		map.put(RunParameters.OUTER_MARGIN, "0");
		RunParameters.verifyAndSetWidth(map);
		assertEquals(4, map.size());
		assertEquals(10, map.get(RunParameters.COLS));
		assertEquals(10, map.get(RunParameters.PAGE_WIDTH));
		assertEquals(0, map.get(RunParameters.INNER_MARGIN));
		assertEquals(0, map.get(RunParameters.OUTER_MARGIN));
	}
	
	@Test
	public void testWidth_02() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.COLS, "10");
		map.put(RunParameters.INNER_MARGIN, "2");
		map.put(RunParameters.OUTER_MARGIN, "2");
		RunParameters.verifyAndSetWidth(map);
		assertEquals(4, map.size());
		assertEquals(10, map.get(RunParameters.COLS));
		assertEquals(14, map.get(RunParameters.PAGE_WIDTH));
		assertEquals(2, map.get(RunParameters.INNER_MARGIN));
		assertEquals(2, map.get(RunParameters.OUTER_MARGIN));
	}
	
	@Test
	public void testDefaultWidth() {
		Map<String, Object> map = new HashMap<>();
		RunParameters.verifyAndSetWidth(map);
		assertEquals(4, map.size());
		assertEquals(28, map.get(RunParameters.COLS));
		assertEquals(32, map.get(RunParameters.PAGE_WIDTH));
		assertEquals(2, map.get(RunParameters.INNER_MARGIN));
		assertEquals(2, map.get(RunParameters.OUTER_MARGIN));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testIncorrectWidth() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.COLS, "11");
		map.put(RunParameters.PAGE_WIDTH, "10");
		map.put(RunParameters.INNER_MARGIN, "1");
		map.put(RunParameters.OUTER_MARGIN, "1");
		RunParameters.verifyAndSetWidth(map);
	}
	
	@Test
	public void testHeight_01() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.PAGE_HEIGHT, "10");
		RunParameters.verifyAndSetHeight(map);
		assertEquals(2, map.size());
		assertEquals(10, map.get(RunParameters.ROWS));
		assertEquals(10, map.get(RunParameters.PAGE_HEIGHT));
	}
	
	@Test
	public void testHeight_02() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.ROWS, "10");
		RunParameters.verifyAndSetHeight(map);
		assertEquals(2, map.size());
		assertEquals(10, map.get(RunParameters.ROWS));
		assertEquals(10, map.get(RunParameters.PAGE_HEIGHT));
	}
	
	@Test
	public void testDefaultHeight() {
		Map<String, Object> map = new HashMap<>();
		RunParameters.verifyAndSetHeight(map);
		assertEquals(2, map.size());
		assertEquals(29, map.get(RunParameters.ROWS));
		assertEquals(29, map.get(RunParameters.PAGE_HEIGHT));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testIncorrectHeight() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.ROWS, "10");
		map.put(RunParameters.PAGE_HEIGHT, "9");
		RunParameters.verifyAndSetHeight(map);
	}
	
	@Test
	public void testRowSpacing_01() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.ROWGAP, "0");
		RunParameters.verifyAndSetRowSpacing(map);
		assertEquals(2, map.size());
		assertEquals(0, map.get(RunParameters.ROWGAP));
		assertEquals("1.0", map.get(RunParameters.ROW_SPACING));
	}
	
	@Test
	public void testRowSpacing_02() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.ROW_SPACING, "2");
		RunParameters.verifyAndSetRowSpacing(map);
		assertEquals(2, map.size());
		assertEquals(4, map.get(RunParameters.ROWGAP));
		assertEquals("2.0", map.get(RunParameters.ROW_SPACING));
	}
	
	@Test
	public void testRowSpacing_03() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.ROW_SPACING, "1.5");
		RunParameters.verifyAndSetRowSpacing(map);
		assertEquals(2, map.size());
		assertEquals(2, map.get(RunParameters.ROWGAP));
		assertEquals("1.5", map.get(RunParameters.ROW_SPACING));
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testIncorrectRowSpacing() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.ROWGAP, "0");
		map.put(RunParameters.ROW_SPACING, "2");
		RunParameters.verifyAndSetRowSpacing(map);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testNegativeRowgap() {
		Map<String, Object> map = new HashMap<>();
		map.put(RunParameters.ROW_SPACING, "0.999");
		RunParameters.verifyAndSetRowSpacing(map);
	}
	
}
