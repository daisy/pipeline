package org.daisy.dotify.common.layout;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

public class SplitPointHandlerTest {

	DummySplitPoint c = new DummySplitPoint.Builder().breakable(false).skippable(false).size(1).build();
	DummySplitPoint e = new DummySplitPoint.Builder().breakable(true).skippable(true).size(1).build();

	@Test
	public void testHardBreak_01() {
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(9, true, Arrays.asList(c, c, c, c, c, c, c, c, c, c));
		assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c), bp.getHead());
		assertEquals(Arrays.asList(c), bp.getTail());
		assertTrue(bp.isHardBreak());
	}
	
	@Test
	public void testHardBreak_02() {
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(1, true, Arrays.asList(c, c, c, c, c, c, c, c, c, c));
		assertEquals(Arrays.asList(c), bp.getHead());
		assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c), bp.getTail());
		assertTrue(bp.isHardBreak());
	}

	@Test
	public void testHardBreak_03() {
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(4, true, Arrays.asList(c, c, c, c, c, c, c, c, c, c));
		assertEquals(Arrays.asList(c, c, c, c), bp.getHead());
		assertEquals(Arrays.asList(c, c, c, c, c, c), bp.getTail());
		assertTrue(bp.isHardBreak());
	}

	@Test
	public void testBreakBefore() {
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(0, false, Arrays.asList(c, c, c, c, c, c, c, c, c, c));
		assertEquals(new ArrayList<DummySplitPoint>(), bp.getHead());
		assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c, c), bp.getTail());
		assertTrue(!bp.isHardBreak());		
	}
	
	@Test
	public void testBreakAfter() {
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(35, false, Arrays.asList(c, c, c, c, c, c, c, c, c, c));
		assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c, c), bp.getHead());
		assertEquals(new ArrayList<DummySplitPoint>(), bp.getTail());
		assertTrue(!bp.isHardBreak());
	}

	@Test
	public void testSoftBreakIncWhiteSpace() {
		DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(5, false, Arrays.asList(c, c, c, c, t, c, c, c, c, c));
		assertEquals(Arrays.asList(c, c, c, c, t), bp.getHead());
		assertEquals(Arrays.asList(c, c, c, c, c), bp.getTail());
		assertTrue(!bp.isHardBreak());
	}

	@Test
	public void testHyphen_01() {
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(5, false, Arrays.asList(c, c, c, c, c, e, c, c, c, c, c));
		assertEquals(Arrays.asList(c, c, c, c, c), bp.getHead());
		assertEquals(Arrays.asList(c, c, c, c, c), bp.getTail());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testSpace_01() {
		DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(5, false, Arrays.asList(c, c, c, e, e, e, e, e, c, c, c, t, c, c, c, c));
		assertEquals(Arrays.asList(c, c, c), bp.getHead());
		assertEquals(Arrays.asList(c, c, c, t, c, c, c, c), SplitPointHandler.trimLeading(bp.getTail()).getSecondPart());
		assertTrue(!bp.isHardBreak());
	}
	
	@Test
	public void testSkippable_01() {
		DummySplitPoint t = new DummySplitPoint.Builder().breakable(false).skippable(true).size(1).build();
		DummySplitPoint a = new DummySplitPoint.Builder().breakable(true).skippable(true).size(1).build();
		int res = SplitPointHandler.forwardSkippable(Arrays.asList(t, t, t, a), 0);
		assertEquals(3, res);
	}
	
	@Test
	public void testSkippable_02() {
		DummySplitPoint t = new DummySplitPoint.Builder().breakable(false).skippable(true).size(1).build();
		int res = SplitPointHandler.forwardSkippable(Arrays.asList(t, t, t, c), 0);
		assertEquals(0, res);
	}
	
	@Test
	public void testSkippable_03() {
		DummySplitPoint t = new DummySplitPoint.Builder().breakable(false).skippable(true).size(1).build();
		int res = SplitPointHandler.forwardSkippable(Arrays.asList(t, t, t, t), 0);
		assertEquals(3, res);
	}
	
	@Test
	public void testWidth() {
		DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(0.7f).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(2, false, Arrays.asList(t, t, t, t));
		assertEquals(Arrays.asList(t, t), bp.getHead());
	}
	
	@Test
	public void testCollapsable_01() {
		DummySplitPoint x = new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
		DummySplitPoint y = new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(4).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, Arrays.asList(c, x, y, c));
		assertEquals(Arrays.asList(c, y, c), bp.getHead());
		assertEquals(Arrays.asList(x), bp.getDiscarded());
	}
	
	@Test
	public void testCollapsable_02() {
		DummySplitPoint x = new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
		DummySplitPoint y = new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(4).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, Arrays.asList(c, c, x, y, c));
		assertEquals(Arrays.asList(c, c), bp.getHead());
		assertEquals(Arrays.asList(c), bp.getTail());
	}
	
	@Test
	public void testCollapsable_03() {
		DummySplitPoint x = new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
		DummySplitPoint y = new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(4.1f).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, Arrays.asList(c, c, x, y, c));
		assertEquals(Arrays.asList(c, c), bp.getHead());
		assertEquals(Arrays.asList(c), SplitPointHandler.trimLeading(bp.getTail()).getSecondPart());
	}
	
	@Test
	public void testCollapsable_04() {
		DummySplitPoint x = new DummySplitPoint.Builder().breakable(true).skippable(false).collapsable(true).size(2).build();
		DummySplitPoint y = new DummySplitPoint.Builder().breakable(true).skippable(false).collapsable(true).size(4.1f).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, Arrays.asList(c, c, x, y, c));
		assertEquals(Arrays.asList(c, c, x), bp.getHead());
		assertEquals(Arrays.asList(y, c), bp.getTail());
	}
	
	@Test
	public void testCollapsable_05() {
		DummySplitPoint x = new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
		DummySplitPoint y = new DummySplitPoint.Builder().breakable(false).skippable(true).collapsable(true).size(4).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, Arrays.asList(c, c, x, y, c));
		assertEquals(Arrays.asList(c, c), bp.getHead());
		assertEquals(Arrays.asList(y, c), bp.getTail());
	}
	
	@Test
	public void testSupplementary_01() {
		final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		Supplements<DummySplitPoint> supps = new Supplements<DummySplitPoint>(){

			@Override
			public DummySplitPoint get(String id) {
				switch (id) {
					case "s1": return s1;
					case "s2": return s2;
					case "s3": return s3;
					default: return null;
				}
			}
		};
		
		DummySplitPoint c1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).supplementID("s1").build();
		DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		DummySplitPoint c3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).supplementID("s1").supplementID("s2").build();
		DummySplitPoint c4 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();

		SplitPointData<DummySplitPoint> spd = new SplitPointData<DummySplitPoint>(Arrays.asList(c1, c2, c3, c4, c5), supps);
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, spd);
		assertEquals(Arrays.asList(c1, c2, c3, c4), bp.getHead());
		assertEquals(Arrays.asList(c5), bp.getTail());
		assertEquals(Arrays.asList(s1, s2), bp.getSupplements());
	}
	
	@Test
	public void testTotalSize_01() {
		final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0.5f).build();
		final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0.5f).build();
		final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0.5f).build();
		Supplements<DummySplitPoint> empty = new Supplements<DummySplitPoint>() {

			@Override
			public DummySplitPoint get(String id) {
				return null;
			}};
		float res = SplitPointHandler.totalSize(Arrays.asList(s1, s2, s3), empty);
		assertEquals(2.5, res, 0);
	}
	
	@Test
	public void testMinimumSize_01() {
		final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		Supplements<DummySplitPoint> supps = new Supplements<DummySplitPoint>(){

			@Override
			public DummySplitPoint get(String id) {
				switch (id) {
					case "s1": return s1;
					case "s2": return s2;
					case "s3": return s3;
					default: return null;
				}
			}
		};
		
		DummySplitPoint c1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).supplementID("s1").build();
		DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		DummySplitPoint c3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).supplementID("s1").supplementID("s2").build();
		DummySplitPoint c4 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1.5f).minSize(1).build();
		DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();

		SplitPointData<DummySplitPoint> spd = new SplitPointData<DummySplitPoint>(Arrays.asList(c1, c2, c3, c4, c5), supps);
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, spd);
		assertEquals(Arrays.asList(c1, c2, c3, c4), bp.getHead());
		assertEquals(Arrays.asList(c5), bp.getTail());
		assertEquals(Arrays.asList(s1, s2), bp.getSupplements());
	}
	
	@Test
	public void testMinimumSize_02() {
		final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		Supplements<DummySplitPoint> supps = new Supplements<DummySplitPoint>(){

			@Override
			public DummySplitPoint get(String id) {
				switch (id) {
					case "s1": return s1;
					case "s2": return s2;
					case "s3": return s3;
					default: return null;
				}
			}
		};
		
		DummySplitPoint c1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0).supplementID("s1").build();
		DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0).build();
		DummySplitPoint c3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0).supplementID("s1").supplementID("s2").build();
		DummySplitPoint c4 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1.5f).minSize(1).build();
		DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();

		SplitPointData<DummySplitPoint> spd = new SplitPointData<DummySplitPoint>(Arrays.asList(c1, c2, c3, c4, c5), supps);
		SplitPoint<DummySplitPoint> bp = bph.split(6, false, spd);
		assertEquals(Arrays.asList(c1, c2, c3, c4), bp.getHead());
		assertEquals(Arrays.asList(c5), bp.getTail());
		assertEquals(Arrays.asList(s1, s2), bp.getSupplements());
	}
	
	@Test
	@Ignore("Fails due to an unhandled case where unitSize > breakPoint. See issue #141")
	public void testSize_01() {
		int unitSize = 10;
		int breakPoint = 6;
		SplitPointHandler<DummySplitPoint> bph = new SplitPointHandler<DummySplitPoint>();
		DummySplitPoint x = new DummySplitPoint.Builder().breakable(true).skippable(false).collapsable(true).size(unitSize).build();
		bph.split(breakPoint, true, Arrays.asList(x, x));
	}


}