package org.daisy.dotify.common.text;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.daisy.dotify.common.text.BreakPoint;
import org.daisy.dotify.common.text.BreakPointHandler;
import org.junit.Test;

public class BreakPointHandlerTest {

	@Test
	public void testHardBreak_01() {
		// case 1
		BreakPointHandler bph = new BreakPointHandler("citat/blockcitat20");
		BreakPoint bp = bph.nextRow(17, true);
		assertEquals("citat/blockcitat2", bp.getHead());
		assertEquals("0", bp.getTail());
		assertTrue(bp.isHardBreak());
	}
	@Test
	public void testHardBreak_02() {
		// case 2
		BreakPointHandler bph = new BreakPointHandler("citat/blockcitat20");
		BreakPoint bp = bph.nextRow(1, true);
		assertEquals("c", bp.getHead());
		assertEquals("itat/blockcitat20", bp.getTail());
		assertTrue(bp.isHardBreak());
	}
	@Test
	public void testHardBreak_03() {
		// case 3
		BreakPointHandler bph = new BreakPointHandler("citat blockcitat20");
		BreakPoint bp = bph.nextRow(4, true);
		assertEquals("cita", bp.getHead());
		assertEquals("t blockcitat20", bp.getTail());
		assertTrue(bp.isHardBreak());
	}
	
	@Test
	public void testBreakBefore() {
		BreakPointHandler bph = new BreakPointHandler("citat/blockcitat20");
		BreakPoint bp = bph.nextRow(0, false);
		assertEquals("", bp.getHead());
		assertEquals("citat/blockcitat20", bp.getTail());
		assertTrue(!bp.isHardBreak());		
	}
	
	@Test
	public void testBreakAfter() {
		BreakPointHandler bph = new BreakPointHandler("citat/blockcitat20");
		BreakPoint bp = bph.nextRow(35, false);
		assertEquals("citat/blockcitat20", bp.getHead());
		assertEquals("", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testSoftBreakIncWhiteSpace() {
		BreakPointHandler bph = new BreakPointHandler("citat blockcitat20");
		BreakPoint bp = bph.nextRow(5, false);
		assertEquals("citat", bp.getHead());
		assertEquals("blockcitat20", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testHyphen_01() {
		BreakPointHandler bph = new BreakPointHandler("citat-blockcitat20");
		BreakPoint bp = bph.nextRow(12, false);
		assertEquals("citat-", bp.getHead());
		assertEquals("blockcitat20", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	@Test
	public void testHyphen_02() {
		BreakPointHandler bph = new BreakPointHandler("Negative number: -154");
		BreakPoint bp = bph.nextRow(19, false);
		assertEquals("Negative number:", bp.getHead());
		assertEquals("-154", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	@Test
	public void testHyphen_03() {
		BreakPointHandler bph = new BreakPointHandler("Negative numbers - odd! (and even)");
		BreakPoint bp = bph.nextRow(18, false);
		assertEquals("Negative numbers -", bp.getHead());
		assertEquals("odd! (and even)", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	@Test
	public void testHyphen_04() {	
		BreakPointHandler bph = new BreakPointHandler("Negative numbers - odd! (and even)");
		BreakPoint bp = bph.nextRow(17, false);
		assertEquals("Negative numbers", bp.getHead());
		assertEquals("- odd! (and even)", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testHyphenKeepRemove() {
		BreakPointHandler bph = new BreakPointHandler("at the ev­i­dence on the ev­i­dence");
		BreakPoint bp = bph.nextRow(11, false);
		assertEquals("at the evi-", bp.getHead());
		assertEquals("dence on the ev­i­dence", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	@Test
	public void testFinalizer() {
		BreakPointHandler bph = new BreakPointHandler("Remove softhyphen (\u00ad) and zero width space (\u200b)");
		assertEquals("Remove softhyphen () and zero width space ()", bph.getRemaining());
	}
	@Test
	public void testFinalizer_performance() {
		//This test is most interesting to run manually when optimizing performance, but it is included 
		//here in case of future improvements. 
		BreakPointHandler bph = new BreakPointHandler("Remove softhyphen (\u00ad) and zero width space (\u200b)");
		int threshold = 300;
		long d= System.currentTimeMillis();
		for (int i=0; i<100000; i++) {
			bph.getRemaining();
		}
		long actualTime = System.currentTimeMillis()-d;
		assertTrue("Time exceeded threshold ("+threshold+" ms), was " + actualTime + " ms.", (actualTime<threshold));
	}
	
	@Test
	public void testZWSP_01() {
		BreakPointHandler bph = new BreakPointHandler("citat\u200bblockcitat20");
		BreakPoint bp = bph.nextRow(12, false);
		assertEquals("citat", bp.getHead());
		assertEquals("blockcitat20", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}

	@Test
	public void testZWSP_02() {
		BreakPointHandler bph = new BreakPointHandler("citat\u200bblockcitat20");
		BreakPoint bp = bph.nextRow(5, false);
		assertEquals("citat", bp.getHead());
		assertEquals("blockcitat20", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}

	@Test
	public void testZWSP_03() {
		BreakPointHandler bph = new BreakPointHandler("ABCDE\u200bFGHIJKL");
		BreakPoint bp = bph.nextRow(6, false);
		assertEquals("ABCDE", bp.getHead());
		assertEquals("FGHIJKL", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testZWSP_04() {
		BreakPointHandler bph = new BreakPointHandler("ABCDE\u200bF\u200bGHIJKL");
		BreakPoint bp = bph.nextRow(6, false);
		assertEquals("ABCDEF", bp.getHead());
		assertEquals("GHIJKL", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testZWSP_05() {
		BreakPointHandler bph = new BreakPointHandler("\u200b\u200bABCDE\u200b\u200bF\u200bGHIJKL");
		BreakPoint bp = bph.nextRow(5, false);
		assertEquals("ABCDE", bp.getHead());
		assertEquals("F\u200bGHIJKL", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testSpace_01() {
		BreakPointHandler bph = new BreakPointHandler("ABCDE  F GHIJKL");
		BreakPoint bp = bph.nextRow(5, false);
		assertEquals("ABCDE", bp.getHead());
		assertEquals("F GHIJKL", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testNSH_01() {
		BreakPointHandler bph = new BreakPointHandler.Builder("til\u00adlåta").
				addHyphenationInfo(2, 3, "ll\u00adl").build();
		BreakPoint bp = bph.nextRow(5, false);
		assertEquals("till-", bp.getHead());
		assertEquals("låta", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testNSH_02() {
		BreakPointHandler bph = new BreakPointHandler.Builder("til\u00adlåter det mes\u00adta.").
				addHyphenationInfo(2, 3, "ll\u00adl").build();
		BreakPoint bp = bph.nextRow(17, false);
		assertEquals("tillåter det mes-", bp.getHead());
		assertEquals("ta.", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testNSH_03() {
		BreakPointHandler bph = new BreakPointHandler.Builder("til\u00ad").
				addHyphenationInfo(2, 2, "ll\u00ad").build();
		BreakPoint bp = bph.nextRow(17, false);
		assertEquals("til", bp.getHead());
		assertEquals("", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testNSH_04() {
		BreakPointHandler bph = new BreakPointHandler.Builder("til\u00adlåta").
				addHyphenationInfo(2, 3, "ll\u00adl").build();
		BreakPoint bp = bph.nextRow(4, false);
		assertEquals("", bp.getHead());
		assertEquals("til\u00adlåta", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testNSH_05() {
		BreakPointHandler bph = new BreakPointHandler.Builder("Det ska vara til\u00adlåtet.").
				addHyphenationInfo(15, 3, "ll\u00adl").build();
		bph.nextRow(7, false);
		BreakPoint bp = bph.nextRow(11, false);
		assertEquals("vara till-", bp.getHead());
		assertEquals("låtet.", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testNSH_06() {
		BreakPointHandler bph = new BreakPointHandler.Builder("eigh\u00adteen").
				addHyphenationInfo(4, 2, "t\u00adt").build();
		BreakPoint bp = bph.nextRow(6, false);
		assertEquals("eight-", bp.getHead());
		assertEquals("teen", bp.getTail());
		assertTrue(!bp.isHardBreak());
	}

}