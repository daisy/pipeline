package org.daisy.dotify.common.splitter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SizeStep<T extends SplitPointUnit> implements StepForward<T> {
    private float size = 0;
    private final Supplements<T> map;
    private final Set<String> ids;
    private final float breakPoint;
    private final boolean useLastUnitSize;
    private T lastUnit;
    private boolean hasSupplements;

    SizeStep(float breakPoint, Supplements<T> map, boolean useLastUnitSize) {
        this.breakPoint = breakPoint;
        this.useLastUnitSize = useLastUnitSize;
        this.map = map;
        this.ids = new HashSet<>();
        this.hasSupplements = false;
    }

    @Override
    public void addUnit(T unit) {
        if (lastUnit != null) {
            size += lastUnit.getUnitSize();
            lastUnit = null;
        }
        List<String> idList = unit.getSupplementaryIDs();
        if (idList != null) {
            for (String id : idList) {
                if (ids.add(id)) { //id didn't already exist in the list
                    T item = map.get(id);
                    if (item != null) {
                        if (!hasSupplements) {
                            hasSupplements = true;
                            size += map.getOverhead();
                        }
                        size += item.getUnitSize();
                    }
                }
            }
        }
        lastUnit = unit;
    }

    @Override
    public boolean overflows(T buffer) {
        return size + (
                buffer != null ?
                        lastUnitSize(buffer) + (lastUnit != null ? lastUnit.getUnitSize() : 0) :
                        lastUnit != null ? (useLastUnitSize ? lastUnit.getLastUnitSize() : lastUnit.getUnitSize()) : 0
        ) > breakPoint;
    }

    private float lastUnitSize(T b) {
        float ret = 0;
        List<String> idList = b.getSupplementaryIDs();
        if (idList != null) {
            boolean hasAddedOverhead = hasSupplements;
            for (String id : idList) {
                if (!ids.contains(id)) { //id didn't already exist in the list
                    T item = map.get(id);
                    if (item != null) {
                        if (!hasAddedOverhead) {
                            hasAddedOverhead = true;
                            ret += map.getOverhead();
                        }
                        ret += item.getUnitSize();
                    }
                }
            }
        }
        ret += useLastUnitSize ? b.getLastUnitSize() : b.getUnitSize();
        return ret;
    }

    @Override
    public void addDiscarded(T unit) {
        //Nothing to do
    }

}
