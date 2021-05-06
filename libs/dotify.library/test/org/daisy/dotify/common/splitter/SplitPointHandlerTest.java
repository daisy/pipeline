package org.daisy.dotify.common.splitter;

import org.daisy.dotify.common.collection.SplitList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
public class SplitPointHandlerTest {

    DummySplitPoint c = new DummySplitPoint.Builder().breakable(false).skippable(false).size(1).build();
    DummySplitPoint e = new DummySplitPoint.Builder().breakable(true).skippable(true).size(1).build();

    @Test
    public void testHardBreak_01() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                9,
                Arrays.asList(c, c, c, c, c, c, c, c, c, c),
                StandardSplitOption.ALLOW_FORCE
            );
        assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c), bp.getHead());
        assertEquals(Arrays.asList(c), bp.getTail().getRemaining());
        assertTrue(bp.isHardBreak());
    }

    @Test
    public void testHardBreak_02() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                1,
                Arrays.asList(c, c, c, c, c, c, c, c, c, c),
                StandardSplitOption.ALLOW_FORCE
            );
        assertEquals(Arrays.asList(c), bp.getHead());
        assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c), bp.getTail().getRemaining());
        assertTrue(bp.isHardBreak());
    }

    @Test
    public void testHardBreak_03() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                4,
                Arrays.asList(c, c, c, c, c, c, c, c, c, c),
                StandardSplitOption.ALLOW_FORCE
            );
        assertEquals(Arrays.asList(c, c, c, c), bp.getHead());
        assertEquals(Arrays.asList(c, c, c, c, c, c), bp.getTail().getRemaining());
        assertTrue(bp.isHardBreak());
    }

    @Test
    public void testHardBreakWithCost_01() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                4,
                Arrays.asList(c, c, c, c, c, c, c, c, c, c),
                (units, index, breakpoint) -> index == 1 ? 0 : 100,
                StandardSplitOption.ALLOW_FORCE
            );
        assertEquals("" + bp.getHead().size(), Arrays.asList(c, c), bp.getHead());
        assertEquals(Arrays.asList(c, c, c, c, c, c, c, c), bp.getTail().getRemaining());
        assertTrue(bp.isHardBreak());
    }

    @Test
    public void testHardBreakWithCost_02() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                6,
                Arrays.asList(c, c, c, c, c, c, c, c, c, c),
                new SplitPointCost<DummySplitPoint>() {
                    double[] values = {4, 5, 3, 1, 2, 4, 5, 100, 12, 1};
                    @Override
                    public double getCost(SplitPointDataSource<DummySplitPoint, ?> units, int index, int breakpoint) {
                        return values[index];
                    }
                },
                StandardSplitOption.ALLOW_FORCE
            );
        assertEquals("" + bp.getHead().size(), Arrays.asList(c, c, c, c), bp.getHead());
        assertEquals(Arrays.asList(c, c, c, c, c, c), bp.getTail().getRemaining());
        assertTrue(bp.isHardBreak());
    }

    @Test
    public void testBreakBefore() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                0,
                Arrays.asList(c, c, c, c, c, c, c, c, c, c)
            );
        assertEquals(new ArrayList<DummySplitPoint>(), bp.getHead());
        assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c, c), bp.getTail().getRemaining());
        assertTrue(!bp.isHardBreak());
    }

    @Test
    public void testBreakAfter() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                35,
                Arrays.asList(c, c, c, c, c, c, c, c, c, c)
            );
        assertEquals(Arrays.asList(c, c, c, c, c, c, c, c, c, c), bp.getHead());
        assertEquals(new ArrayList<DummySplitPoint>(), bp.getTail().getRemaining());
        assertTrue(!bp.isHardBreak());
    }

    @Test
    public void testSoftBreakIncWhiteSpace() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                5,
                Arrays.asList(c, c, c, c, t, c, c, c, c, c)
            );
        assertEquals(Arrays.asList(c, c, c, c, t), bp.getHead());
        assertEquals(Arrays.asList(c, c, c, c, c), bp.getTail().getRemaining());
        assertTrue(!bp.isHardBreak());
    }

    @Test
    public void testHyphen_01() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                5,
                Arrays.asList(c, c, c, c, c, e, c, c, c, c, c)
            );
        assertEquals(Arrays.asList(c, c, c, c, c), bp.getHead());
        assertEquals(Arrays.asList(c, c, c, c, c), bp.getTail().getRemaining());
        assertTrue(!bp.isHardBreak());
    }

    @Test
    public void testSpace_01() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(
                5,
                Arrays.asList(c, c, c, e, e, e, e, e, c, c, c, t, c, c, c, c)
            );
        assertEquals(Arrays.asList(c, c, c), bp.getHead());
        assertEquals(
            Arrays.asList(c, c, c, t, c, c, c, c),
            SplitPointHandler.trimLeading(bp.getTail().getRemaining()).getSecondPart()
        );
        assertTrue(!bp.isHardBreak());
    }

    @Test
    public void testTrimLeading_01() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitList<DummySplitPoint> res = SplitPointHandler.trimLeading(
            Arrays.asList(e, e, e, e, e, c, c, c, t, c, c, c, c)
        );
        assertEquals(Arrays.asList(e, e, e, e, e), res.getFirstPart());
        assertEquals(Arrays.asList(c, c, c, t, c, c, c, c), res.getSecondPart());
    }

    @Test
    public void testTrimLeading_02() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> res = SplitPointHandler.trimLeading(
            new SplitPointDataList<>(e, e, e, e, e, c, c, c, t, c, c, c, c)
        );
        assertEquals(Arrays.asList(e, e, e, e, e), res.getDiscarded());
        assertEquals(Arrays.asList(c, c, c, t, c, c, c, c), res.getTail().getRemaining());
        assertEquals(Collections.emptyList(), res.getHead());
        assertEquals(Collections.emptyList(), res.getSupplements());
        assertEquals(false, res.isHardBreak());
    }

    @Test
    public void testTrimLeading_03() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> res = SplitPointHandler.trimLeading(
            new SplitPointDataList<>(c, c, c, t, c, c, c, c)
        );
        assertEquals(Collections.emptyList(), res.getDiscarded());
        assertEquals(Arrays.asList(c, c, c, t, c, c, c, c), res.getTail().getRemaining());
        assertEquals(Collections.emptyList(), res.getHead());
        assertEquals(Collections.emptyList(), res.getSupplements());
        assertEquals(false, res.isHardBreak());
    }

    @Test
    public void testTrimLeading_04() {
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> res = SplitPointHandler.trimLeading(
            new SplitPointDataList<>(e, e, e, e, e)
        );
        assertEquals(Arrays.asList(e, e, e, e, e), res.getDiscarded());
        assertEquals(Collections.emptyList(), res.getTail().getRemaining());
        assertEquals(Collections.emptyList(), res.getHead());
        assertEquals(Collections.emptyList(), res.getSupplements());
        assertEquals(false, res.isHardBreak());
    }

    @Test
    public void testSkippable_01() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(false).skippable(true).size(1).build();
        DummySplitPoint a = new DummySplitPoint.Builder().breakable(true).skippable(true).size(1).build();
        int res = SplitPointHandler.forwardSkippable(new SplitPointDataList<>(Arrays.asList(t, t, t, a)), 0);
        assertEquals(3, res);
    }

    @Test
    public void testSkippable_02() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(false).skippable(true).size(1).build();
        int res = SplitPointHandler.forwardSkippable(new SplitPointDataList<>(Arrays.asList(t, t, t, c)), 0);
        assertEquals(0, res);
    }

    @Test
    public void testSkippable_03() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(false).skippable(true).size(1).build();
        int res = SplitPointHandler.forwardSkippable(new SplitPointDataList<>(Arrays.asList(t, t, t, t)), 0);
        assertEquals(3, res);
    }

    @Test
    public void testWidth() {
        DummySplitPoint t = new DummySplitPoint.Builder().breakable(true).skippable(false).size(0.7f).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(2, Arrays.asList(t, t, t, t));
        assertEquals(Arrays.asList(t, t), bp.getHead());
    }

    @Test
    public void testCollapsable_01() {
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
        DummySplitPoint y =
            new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(4).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(6, Arrays.asList(c, x, y, c));
        assertEquals(Arrays.asList(c, y, c), bp.getHead());
        assertEquals(Arrays.asList(x), bp.getDiscarded());
    }

    @Test
    public void testCollapsable_02() {
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
        DummySplitPoint y =
            new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(4).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(6, Arrays.asList(c, c, x, y, c));
        assertEquals(Arrays.asList(c, c), bp.getHead());
        assertEquals(Arrays.asList(c), bp.getTail().getRemaining());
    }

    @Test
    public void testCollapsable_03() {
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
        DummySplitPoint y =
            new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(4.1f).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(6, Arrays.asList(c, c, x, y, c));
        assertEquals(Arrays.asList(c, c), bp.getHead());
        assertEquals(Arrays.asList(c), SplitPointHandler.trimLeading(bp.getTail().getRemaining()).getSecondPart());
    }

    @Test
    public void testCollapsable_04() {
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(false).collapsable(true).size(2).build();
        DummySplitPoint y =
            new DummySplitPoint.Builder().breakable(true).skippable(false).collapsable(true).size(4.1f).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(6, Arrays.asList(c, c, x, y, c));
        assertEquals(Arrays.asList(c, c, x), bp.getHead());
        assertEquals(Arrays.asList(y, c), bp.getTail().getRemaining());
    }

    @Test
    public void testCollapsable_05() {
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(true).collapsable(true).size(2).build();
        DummySplitPoint y =
            new DummySplitPoint.Builder().breakable(false).skippable(true).collapsable(true).size(4).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(6, Arrays.asList(c, c, x, y, c));
        assertEquals(Arrays.asList(c, c), bp.getHead());
        assertEquals(Arrays.asList(y, c), bp.getTail().getRemaining());
    }

    @Test
    public void testSupplementary_01() {
        final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        Supplements<DummySplitPoint> supps = id -> {
            switch (id) {
                case "s1": return s1;
                case "s2": return s2;
                case "s3": return s3;
                default: return null;
            }
        };

        DummySplitPoint c1 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).supplementID("s1").build();
        DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c3 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1)
                .supplementID("s1").supplementID("s2").build();
        DummySplitPoint c4 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPointHandler<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bph = new SplitPointHandler<>();

        SplitPointDataList<DummySplitPoint> spd = new SplitPointDataList<>(Arrays.asList(c1, c2, c3, c4, c5), supps);
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp = bph.split(6, spd);
        assertEquals(Arrays.asList(c1, c2, c3, c4), bp.getHead());
        assertEquals(Arrays.asList(c5), bp.getTail().getRemaining());
        assertEquals(Arrays.asList(s1, s2), bp.getSupplements());
    }

    @Test
    public void testSupplementary_02() {
        final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        Supplements<DummySplitPoint> supps = new Supplements<DummySplitPoint>() {
            @Override
            public DummySplitPoint get(String id) {
                switch (id) {
                    case "s1": return s1;
                    case "s2": return s2;
                    case "s3": return s3;
                    default: return null;
                }
            }

            @Override
            public double getOverhead() {
                return 1;
            }

        };

        DummySplitPoint c1 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).supplementID("s1").build();
        DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c3 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1)
                .supplementID("s1").supplementID("s2").build();
        DummySplitPoint c4 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPointHandler<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bph = new SplitPointHandler<>();

        SplitPointDataList<DummySplitPoint> spd = new SplitPointDataList<>(Arrays.asList(c1, c2, c3, c4, c5), supps);
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp = bph.split(6, spd);
        assertEquals(Arrays.asList(c1, c2, c3), bp.getHead());
        assertEquals(Arrays.asList(c4, c5), bp.getTail().getRemaining());
        assertEquals(Arrays.asList(s1, s2), bp.getSupplements());
    }

    @Test
    public void testSupplementary_03() {
        final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        Supplements<DummySplitPoint> supps = new Supplements<DummySplitPoint>() {
            @Override
            public DummySplitPoint get(String id) {
                switch (id) {
                    case "s1": return s1;
                    case "s2": return s2;
                    case "s3": return s3;
                    default: return null;
                }
            }

            @Override
            public double getOverhead() {
                return 1;
            }

        };

        DummySplitPoint c1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c4 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c6 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c7 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1)
                .supplementID("s1").supplementID("s2").build();
        SplitPointHandler<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bph = new SplitPointHandler<>();

        SplitPointDataList<DummySplitPoint> spd =
            new SplitPointDataList<>(Arrays.asList(c1, c2, c3, c4, c5, c6, c7), supps);
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp = bph.split(6, spd);
        assertEquals(Arrays.asList(c1, c2, c3, c4, c5, c6), bp.getHead());
        assertEquals(Arrays.asList(c7), bp.getTail().getRemaining());
        assertEquals(Collections.emptyList(), bp.getSupplements());
    }


    @Test
    public void testTotalSize_01() {
        final DummySplitPoint s1 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0.5f).build();
        final DummySplitPoint s2 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0.5f).build();
        final DummySplitPoint s3 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0.5f).build();
        float res = SplitPointHandler.totalSize(
            new SplitPointDataList<>(Arrays.asList(s1, s2, s3)), 3, true
        );
        assertEquals(2.5, res, 0);
    }

    @Test
    public void testMinimumSize_01() {
        final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        Supplements<DummySplitPoint> supps = id -> {
            switch (id) {
                case "s1": return s1;
                case "s2": return s2;
                case "s3": return s3;
                default: return null;
            }
        };

        DummySplitPoint c1 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).supplementID("s1").build();
        DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        DummySplitPoint c3 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1)
                .supplementID("s1").supplementID("s2").build();
        DummySplitPoint c4 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1.5f).minSize(1).build();
        DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPointHandler<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bph = new SplitPointHandler<>();

        SplitPointDataList<DummySplitPoint> spd = new SplitPointDataList<>(Arrays.asList(c1, c2, c3, c4, c5), supps);
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp = bph.split(6, spd);
        assertEquals(Arrays.asList(c1, c2, c3, c4), bp.getHead());
        assertEquals(Arrays.asList(c5), bp.getTail().getRemaining());
        assertEquals(Arrays.asList(s1, s2), bp.getSupplements());
    }

    @Test
    public void testMinimumSize_02() {
        final DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        final DummySplitPoint s3 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        Supplements<DummySplitPoint> supps = id -> {
            switch (id) {
                case "s1": return s1;
                case "s2": return s2;
                case "s3": return s3;
                default: return null;
            }
        };

        DummySplitPoint c1 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0)
                .supplementID("s1").build();
        DummySplitPoint c2 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0).build();
        DummySplitPoint c3 =
            new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).minSize(0)
                .supplementID("s1").supplementID("s2").build();
        DummySplitPoint c4 =
                new DummySplitPoint.Builder().breakable(true).skippable(false).size(1.5f).minSize(1).build();
        DummySplitPoint c5 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPointHandler<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bph = new SplitPointHandler<>();

        SplitPointDataList<DummySplitPoint> spd = new SplitPointDataList<>(Arrays.asList(c1, c2, c3, c4, c5), supps);
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp = bph.split(6, spd);
        assertEquals(Arrays.asList(c1, c2, c3, c4), bp.getHead());
        assertEquals(Arrays.asList(c5), bp.getTail().getRemaining());
        assertEquals(Arrays.asList(s1, s2), bp.getSupplements());
    }

    @Test
    public void testSize_01() {
        int unitSize = 10;
        int breakPoint = 6;
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(false).collapsable(true).size(unitSize).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(breakPoint, Arrays.asList(x, x));
        assertEquals(Arrays.asList(), bp.getHead());
        assertEquals(Arrays.asList(x, x), bp.getTail().getRemaining());
    }

    @Test
    public void testSize_02() {
        int unitSize = 10;
        int breakPoint = 6;
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(false).collapsable(true).size(unitSize).build();
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(breakPoint, Arrays.asList(x, x));
        assertEquals(Arrays.asList(), bp.getHead());
        assertEquals(Arrays.asList(x, x), bp.getTail().getRemaining());
    }

    @Test
    public void testEmpty() {
        int breakPoint = 6;
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp =
            SplitPointHandler.split(breakPoint, Arrays.asList());
        assertEquals(Arrays.asList(), bp.getHead());
        assertEquals(Arrays.asList(), bp.getTail().getRemaining());
    }

    @Test
    public void testSupplementsSize_01() {
        int breakPoint = 6;
        Supplements<DummySplitPoint> supps = new Supplements<DummySplitPoint>() {
            DummySplitPoint s1 = new DummySplitPoint.Builder().breakable(true).skippable(false).size(10).build();
            @Override
            public DummySplitPoint get(String id) {
                switch (id) {
                    case "s1": return s1;
                    default: return null;
                }
            }
        };
        SplitPointHandler<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bph = new SplitPointHandler<>();
        DummySplitPoint x =
            new DummySplitPoint.Builder().breakable(true).skippable(false).supplementID("s1").size(1).build();
        //DummySplitPoint y = new DummySplitPoint.Builder().breakable(true).skippable(false).size(1).build();
        SplitPointDataList<DummySplitPoint> spd = new SplitPointDataList<>(Arrays.asList(x), supps);
        SplitPoint<DummySplitPoint, SplitPointDataList<DummySplitPoint>> bp = bph.split(breakPoint, spd);
        assertEquals(Arrays.asList(), bp.getHead());
        assertEquals(Arrays.asList(x), bp.getTail().getRemaining());
    }
}
