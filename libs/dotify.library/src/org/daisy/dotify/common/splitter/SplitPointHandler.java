package org.daisy.dotify.common.splitter;

import org.daisy.dotify.common.collection.SplitList;
import org.daisy.dotify.common.splitter.SplitPointSpecification.Type;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Breaks units into results. All allowed break points are supplied with the input.
 *
 * @param <T> the type of split point units
 * @param <U> the type of data source
 * @author Joel Håkansson
 */
public class SplitPointHandler<T extends SplitPointUnit, U extends SplitPointDataSource<T, U>> {
    private final List<T> emptyList = Collections.emptyList();
    private final SplitPointCost<T> defaultCost = new SplitPointCost<T>() {
        @Override
        public double getCost(SplitPointDataSource<T, ?> data, int index, int breakpoint) {
            // 1. the smaller the result, the higher the cost
            // 2. breakable units are always preferred over forced ones
            return (data.get(index).isBreakable() ? 1 : 2) * breakpoint - index;
        }
    };

    /**
     * Splits the data at, or before, the supplied breakPoint according to the rules
     * in the data. If force is used, rules may be broken to achieve a result.
     *
     * @param breakPoint the split point
     * @param units      the data
     * @param <T>        the type of split point units
     * @return returns a split point result
     */
    @SafeVarargs
    public static <T extends SplitPointUnit> SplitPoint<T, SplitPointDataList<T>> split(
        float breakPoint,
        T... units
    ) {
        SplitPointHandler<T, SplitPointDataList<T>> splitter = new SplitPointHandler<>();
        return splitter.split(breakPoint, new SplitPointDataList<T>(units), splitter.defaultCost);
    }

    /**
     * Splits the data at, or before, the supplied breakPoint according to the rules
     * in the data. If force is used, rules may be broken to achieve a result.
     *
     * @param breakPoint the split point
     * @param units      the data
     * @param options    the split options
     * @param <T>        the type of split point units
     * @return returns a split point result
     */
    public static <T extends SplitPointUnit> SplitPoint<T, SplitPointDataList<T>> split(
        float breakPoint,
        List<T> units,
        SplitOption... options
    ) {
        SplitPointHandler<T, SplitPointDataList<T>> splitter = new SplitPointHandler<>();
        return splitter.split(breakPoint, new SplitPointDataList<T>(units), splitter.defaultCost, options);
    }

    /**
     * Splits the data at, or before, the supplied breakPoint according to the rules
     * in the data. If force is used, rules may be broken to achieve a result.
     *
     * @param breakPoint the split point
     * @param units      the data
     * @param cost       the cost function used when determining the optimal <i>forced</i> split point. In other words,
     *                   the cost function is only used if there are no breakable units available.
     * @param options    the split options
     * @param <T>        the type of split point units
     * @return returns a split point result
     */
    public static <T extends SplitPointUnit> SplitPoint<T, SplitPointDataList<T>> split(
        float breakPoint,
        List<T> units,
        SplitPointCost<T> cost,
        SplitOption... options
    ) {
        SplitPointHandler<T, SplitPointDataList<T>> splitter = new SplitPointHandler<>();
        return splitter.split(breakPoint, new SplitPointDataList<T>(units), cost, options);
    }

    /**
     * Splits the data at, or before, the supplied breakPoint according to the rules
     * in the data. If force is used, rules may be broken to achieve a result.
     *
     * @param breakPoint the split point
     * @param data       the data to split
     * @param options    the split options
     * @return returns a split point result
     */
    public SplitPoint<T, U> split(float breakPoint, U data, SplitOption... options) {
        return split(breakPoint, data, defaultCost, options);
    }

    /**
     * Splits the data at, or before, the supplied breakPoint according to the rules
     * in the data. If force is used, rules may be broken to achieve a result.
     *
     * @param breakPoint the split point
     * @param data       the data to split
     * @param cost       the cost function used when determining the optimal <i>forced</i> split point. In other words,
     *                   the cost function is only used if there are no breakable units available.
     * @param options    the split options
     * @return returns a split point result
     * @throws IllegalArgumentException if cost is null
     */
    public SplitPoint<T, U> split(float breakPoint, U data, SplitPointCost<T> cost, SplitOption... options) {
        SplitPointSpecification spec = find(breakPoint, data, cost, options);
        if (cost == null) {
            throw new IllegalArgumentException("Null cost not allowed.");
        }
        if (spec.getType() == Type.EMPTY) {
            // pretty simple...
            return new SplitPoint<>(emptyList, emptyList, data.createEmpty(), emptyList, false);
        } else if (spec.getType() == Type.NONE) {
            return emptyHead(data);
        } else if (spec.getType() == Type.ALL) {
            return finalizeBreakpoint(
                new SplitList<>(data.getRemaining(), emptyList),
                data.createEmpty(),
                data.getSupplements(),
                false
            );
        } else {
            return makeBreakpoint(data, spec);
        }
    }

    /**
     * <p>Splits the data according to the supplied specification. A specification can be created by using
     * {@link #find(float, SplitPointDataSource, SplitPointCost, SplitOption...)} on the data source.</p>
     * <p>No data is beyond the specified split point is produced using this method.
     * Also, only one of the data producing operations is called, either
     * {@link SplitPointDataSource#getRemaining()} or {@link SplitPointDataSource#split(int)}.</p>
     *
     * @param spec the specification
     * @param data the data
     * @return returns a split point result
     */
    public SplitPoint<T, U> split(SplitPointSpecification spec, U data) {
        if (spec.getType() == Type.EMPTY) {
            // pretty simple...
            return new SplitPoint<>(emptyList, emptyList, data.createEmpty(), emptyList, false);
        } else if (spec.getType() == Type.NONE) {
            return emptyHead(data);
        } else if (spec.getType() == Type.ALL) {
            return finalizeBreakpoint(
                new SplitList<>(data.getRemaining(), emptyList),
                data.createEmpty(),
                data.getSupplements(),
                false
            );
        } else {
            return makeBreakpoint(data, spec);
        }
    }

    /**
     * Finds a split point at, or before, the supplied breakPoint according to the rules
     * in the data. If force is used, rules may be broken to achieve a result.
     *
     * @param breakPoint the split point
     * @param data       the data to split
     * @param options    the split options
     * @return returns a split point specification
     */
    public SplitPointSpecification find(float breakPoint, U data, SplitOption... options) {
        return find(breakPoint, data, defaultCost, options);
    }

    /**
     * Finds a split point at, or before, the supplied breakPoint according to the rules
     * in the data. If force is used, rules may be broken to achieve a result.
     *
     * @param breakPoint the split point
     * @param data       the data to split
     * @param cost       the cost function used when determining the optimal <i>forced</i> split point. In other words,
     *                   the cost function is only used if there are no breakable units available.
     * @param options    the split options
     * @return returns a split point specification
     */
    public SplitPointSpecification find(float breakPoint, U data, SplitPointCost<T> cost, SplitOption... options) {
        SplitOptions opts = SplitOptions.parse(options);
        if (cost == null) {
            throw new IllegalArgumentException("Null cost not allowed.");
        }
        if (data.isEmpty()) {
            // pretty simple...
            return SplitPointSpecification.empty();
        } else if (breakPoint <= 0) {
            return SplitPointSpecification.none();
        } else if (fits(data, breakPoint, opts.useLastUnitSize)) {
            return SplitPointSpecification.all();
        } else {
            int startPos = findCollapse(data, new SizeStep<>(breakPoint, data.getSupplements(), opts.useLastUnitSize));
            // If no units are returned here it's because even the first unit doesn't fit.
            // Therefore, force will not help.
            if (startPos < 0) {
                return SplitPointSpecification.none();
            } else {
                return findBreakpoint(data, opts.useForce, startPos, cost, opts.trimTrailing);
            }
        }
    }

    private static class SplitOptions {
        boolean useForce = false;
        boolean trimTrailing = true;
        boolean useLastUnitSize = true;

        static SplitOptions parse(SplitOption... opts) {
            SplitOptions result = new SplitOptions();
            for (SplitOption option : opts) {
                if (option == StandardSplitOption.ALLOW_FORCE) {
                    result.useForce = true;
                } else if (option == StandardSplitOption.RETAIN_TRAILING) {
                    result.trimTrailing = false;
                } else if (option == StandardSplitOption.NO_LAST_UNIT_SIZE) {
                    result.useLastUnitSize = false;
                } else if (option == null) {
                    //no-op
                } else {
                    throw new UnsupportedOperationException("'" + option +
                            "' is not a recognized split option");
                }
            }
            return result;
        }
    }

    private SplitPoint<T, U> emptyHead(U data) {
        return finalizeBreakpoint(new SplitList<>(emptyList, emptyList), data, data.getSupplements(), false);
    }

    private SplitPointSpecification findBreakpoint(
        U data,
        boolean force,
        int pos,
        SplitPointCost<T> cost,
        boolean trimTrailing
    ) {
        pos = forwardSkippable(data, pos);
        // check next unit to see if it can be removed.
        if (!data.hasElementAt(pos + 1)) { // last unit?
            return SplitPointSpecification.all();
        } else {
            return findBreakpointFromPosition(data, pos, force, cost, trimTrailing);
        }
    }

    private SplitPoint<T, U> makeBreakpoint(U data, SplitPointSpecification spec) {
        Supplements<T> map = data.getSupplements();
        SplitResult<T, U> split = data.split(spec.getIndex());
        return finalizeBreakpointFull(split, map, spec.isHard(), spec.shouldTrimTrailing());
    }

    private SplitPointSpecification findBreakpointFromPosition(
        U data,
        int strPos,
        boolean force,
        SplitPointCost<T> cost,
        boolean trimTrailing
    ) {
        // back up
        BreakPointScannerResult result = findBreakpointBefore(data, strPos, cost);
        boolean hard = false;
        int tailStart;
        if (result.bestBreakable != result.bestSplitPoint) { // no breakable found, break hard
            if (force) {
                hard = true;
                tailStart = result.bestSplitPoint + 1;
                // move break point after skippable
                while (data.hasElementAt(tailStart) && data.get(tailStart).isSkippable()) {
                    tailStart++;
                }
            } else {
                tailStart = 0;
            }
        } else {
            tailStart = result.bestBreakable + 1;
        }
        return new SplitPointSpecification(tailStart, hard, trimTrailing);
    }

    private SplitPoint<T, U> finalizeBreakpointFull(
        SplitResult<T, U> result,
        Supplements<T> map,
        boolean hard,
        boolean trimTrailing
    ) {
        if (trimTrailing) {
            return finalizeBreakpoint(trimTrailing(result.head()), result.tail(), map, hard);
        } else {
            return finalizeBreakpoint(new SplitList<>(result.head(), emptyList), result.tail(), map, hard);
        }
    }

    private SplitPoint<T, U> finalizeBreakpoint(SplitList<T> head, U tail, Supplements<T> map, boolean hard) {
        TrimStep<T> trimmed = new TrimStep<>(map);
        findCollapse(new SplitPointDataList<T>(head.getFirstPart()), trimmed);
        List<T> discarded = trimmed.getDiscarded();
        discarded.addAll(head.getSecondPart());
        return new SplitPoint<>(trimmed.getResult(), trimmed.getSupplements(), tail, discarded, hard);
    }

    static <T extends SplitPointUnit> T maxSize(T u1, T u2) {
        return (u1.getUnitSize() >= u2.getUnitSize() ? u1 : u2);
    }

    static <T extends SplitPointUnit> SplitList<T> trimTrailing(List<T> in) {
        int i;
        for (i = in.size() - 1; i >= 0; i--) {
            if (!in.get(i).isSkippable()) {
                break;
            }
        }
        return SplitList.split(in, i + 1);
    }

    /**
     * Finds the index for the last unit that fits into the given space.
     *
     * @param data
     * @param impl
     * @return returns the index for the last unit
     */
    static <T extends SplitPointUnit, U extends SplitPointDataSource<T, U>> int findCollapse(
        U data,
        StepForward<T> impl
    ) {
        int units = -1;
        T maxCollapsable = null;
        for (int i = 0; data.hasElementAt(i); i++) {
            T c = data.get(i);
            units++;
            if (c.isCollapsible()) {
                if (maxCollapsable != null) {
                    if (maxCollapsable.collapsesWith(c)) {
                        if (maxSize(maxCollapsable, c) == c) {
                            //new one is now max, add the previous to collapsed
                            impl.addDiscarded(maxCollapsable);
                            maxCollapsable = c;
                        } else {
                            //old one is max, add the new one to collapsed
                            impl.addDiscarded(c);
                        }
                    } else {
                        impl.addUnit(maxCollapsable);
                        maxCollapsable = c;
                    }
                } else {
                    maxCollapsable = c;
                }
            } else {
                if (maxCollapsable != null) {
                    impl.addUnit(maxCollapsable);
                    maxCollapsable = null;
                }
                impl.addUnit(c);
            }
            if (impl.overflows(maxCollapsable)) { //time to exit
                units--;
                return units;
            }
        }
        if (maxCollapsable != null) {
            impl.addUnit(maxCollapsable);
            maxCollapsable = null;
        }
        return units;
    }

    /**
     * Starting from a unit at a certain position, pass over as many skippable units as possible,
     * with the last one being a breakable unit or the last unit in the source.
     */
    static int forwardSkippable(
        SplitPointDataSource<? extends SplitPointUnit,
        ? extends SplitPointDataSource<?, ?>> data,
        int pos
    ) {
        SplitPointUnit c;
        int p = pos;
        while (data.hasElementAt(++p)) {
            c = data.get(p);
            if (!c.isSkippable()) {
                return pos; // break after last encountered breakable unit or start unit
            }
            if (c.isBreakable()) {
                pos = p;
            }
        }
        return p - 1; // break after last unit in source
    }

    static <T extends SplitPointUnit, U extends SplitPointDataSource<T, U>> BreakPointScannerResult
    findBreakpointBefore(
        U data,
        int strPos,
        SplitPointCost<T> cost
    ) {
        BreakPointScannerResult res = new BreakPointScannerResult();
        res.bestBreakable = -1;
        res.bestSplitPoint = strPos;
        double currentCost = Double.MAX_VALUE;
        double currentBreakableCost = Double.MAX_VALUE;
        for (int index = 0; index <= strPos; index++) {
            double c = cost.getCost(data, index, strPos);
            if (c < currentCost) { // this should always be true for the first unit
                res.bestSplitPoint = index;
                currentCost = c;
            }
            if (c < currentBreakableCost && data.get(index).isBreakable()) {
                res.bestBreakable = index;
                currentBreakableCost = c;
            }
        }
        return res;
    }

    private static class BreakPointScannerResult {
        int bestBreakable;
        int bestSplitPoint;
    }

    /**
     * Returns true if the total size is less than or equal to the limit, false otherwise.
     *
     * @param data  the units
     * @param limit the maximum width that is relevant to calculate
     * @return returns the size
     */
    static <T extends SplitPointUnit, U extends SplitPointDataSource<T, U>> boolean fits(
        U data,
        float limit,
        boolean useLastUnitSize
    ) {
        return totalSize(data, limit, useLastUnitSize) <= limit;
    }

    /**
     * If the total size is less than the limit, the size is returned, otherwise a value greater
     * than or equal to the limit is returned.
     *
     * @param data  the units
     * @param limit the maximum width that is relevant to calculate
     * @return returns the size
     */
    static <T extends SplitPointUnit, U extends SplitPointDataSource<T, U>> float totalSize(
        U data,
        float limit,
        boolean useLastUnitSize
    ) {
        float ret = 0;
        Set<String> ids = new HashSet<>();
        Supplements<T> map = data.getSupplements();
        boolean hasSupplements = false;
        // we check up to the limit and beyond by one element, to make sure that we check enough units
        for (int i = 0; data.hasElementAt(i) && ret <= limit; i++) {
            T unit = data.get(i);
            List<String> suppIds = unit.getSupplementaryIDs();
            if (suppIds != null) {
                for (String id : suppIds) {
                    if (ids.add(id)) { //id didn't already exist in the list
                        T item = map.get(id);
                        if (item != null) {
                            if (!hasSupplements) {
                                hasSupplements = true;
                                ret += map.getOverhead();
                            }
                            ret += item.getUnitSize();
                        }
                    }
                }
            }
            //last unit?
            if (useLastUnitSize && !data.hasElementAt(i + 1)) {
                ret += unit.getLastUnitSize();
            } else {
                ret += unit.getUnitSize();
            }
        }
        return ret;
    }
}
