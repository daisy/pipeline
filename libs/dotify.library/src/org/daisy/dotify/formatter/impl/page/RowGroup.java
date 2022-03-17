package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.common.splitter.SplitPointUnit;
import org.daisy.dotify.formatter.impl.row.LineProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.VolumeKeepPriority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <code>RowGroup</code> is the unit used when calculating page breaks. For this reason,
 * <code>RowGroup</code> implements {@link SplitPointUnit}. It contains zero, one or more rows that
 * can not be separated.
 */
class RowGroup implements SplitPointUnit {
    private final List<RowImpl> rows;
    private final List<Marker> markers;
    private final List<String> identifiers;
    private final float unitSize, lastUnitSize;
    private final boolean breakable, skippable, collapsible, lazyCollapse;
    private final List<String> ids;
    private final int keepWithNextSheets, keepWithPreviousSheets;
    private final VolumeKeepPriority avoidVolumeBreakAfterPriority;
    private final boolean lastInBlock;
    private final boolean mergeable;
    private final LineProperties lineProps;

    static class Builder {
        private final List<RowImpl> rows;
        private List<Marker> markers;
        private List<String> anchors;
        private List<String> identifiers;
        private final float rowDefault;
        private boolean breakable = false, skippable = false, collapsible = false;
        private int keepWithNextSheets = 0, keepWithPreviousSheets = 0;
        private boolean lazyCollapse = true;
        private VolumeKeepPriority avoidVolumeBreakAfterPriority = VolumeKeepPriority.empty();
        private boolean lastInBlock = false;
        private boolean mergeable = false;
        private LineProperties lineProps = LineProperties.DEFAULT;


        Builder(float rowDefault, RowImpl... rows) {
            this(rowDefault, Arrays.asList(rows));
        }

        Builder(float rowDefault) {
            this(rowDefault, new ArrayList<RowImpl>());
        }

        Builder(float rowDefault, List<RowImpl> rows) {
            this.rows = rows;
            this.rowDefault = rowDefault;
            this.markers = new ArrayList<>();
            this.anchors = new ArrayList<>();
            this.identifiers = new ArrayList<>();
        }

        Builder add(RowImpl value) {
            rows.add(value);
            return this;
        }

        Builder addAll(List<RowImpl> value) {
            rows.addAll(value);
            return this;
        }

        Builder markers(List<Marker> value) {
            this.markers = value;
            return this;
        }

        Builder anchors(List<String> value) {
            this.anchors = value;
            return this;
        }

        Builder identifiers(List<String> value) {
            this.identifiers = value;
            return this;
        }

        Builder breakable(boolean value) {
            this.breakable = value;
            return this;
        }

        Builder skippable(boolean value) {
            this.skippable = value;
            return this;
        }

        Builder collapsible(boolean value) {
            this.collapsible = value;
            return this;
        }

        Builder lazyCollapse(boolean value) {
            this.lazyCollapse = value;
            return this;
        }

        Builder keepWithNextSheets(int value) {
            this.keepWithNextSheets = value;
            return this;
        }

        Builder keepWithPreviousSheets(int value) {
            this.keepWithPreviousSheets = value;
            return this;
        }

        Builder avoidVolumeBreakAfterPriority(VolumeKeepPriority value) {
            this.avoidVolumeBreakAfterPriority = Objects.requireNonNull(value);
            return this;
        }

        /**
         * Sets the last in a block indicator.
         *
         * @param value the value
         */
        Builder lastRowGroupInBlock(boolean value) {
            this.lastInBlock = value;
            return this;
        }

        Builder mergeable(boolean value) {
            this.mergeable = value;
            return this;
        }

        Builder lineProperties(LineProperties value) {
            this.lineProps = value;
            return this;
        }

        RowGroup build() {
            return new RowGroup(this);
        }
    }

    private RowGroup(Builder builder) {
        this.rows = builder.rows;
        this.markers = builder.markers;
        this.identifiers = builder.identifiers;
        this.breakable = builder.breakable;
        this.skippable = builder.skippable;
        this.collapsible = builder.collapsible;
        this.unitSize = calcUnitSize(builder.rowDefault, rows);
        this.lastUnitSize = unitSize == 0 ? 0 : unitSize -
              (rows.isEmpty() ? 0 : Math.max(0, getRowSpacing(builder.rowDefault, rows.get(rows.size() - 1)) - 1));
        this.ids = new ArrayList<>();
        this.lazyCollapse = builder.lazyCollapse;
        ids.addAll(builder.anchors);
        for (RowImpl r : rows) {
            ids.addAll(r.getAnchors());
        }
        this.keepWithNextSheets = builder.keepWithNextSheets;
        this.keepWithPreviousSheets = builder.keepWithPreviousSheets;
        this.avoidVolumeBreakAfterPriority = builder.avoidVolumeBreakAfterPriority;
        this.lastInBlock = builder.lastInBlock;
        this.mergeable = builder.mergeable;
        this.lineProps = builder.lineProps;
    }

    /**
     * Creates a deep copy of the supplied instance.
     *
     * @param template the instance to copy
     */
    RowGroup(RowGroup template) {
        this.rows = new ArrayList<>(template.rows);
        this.markers = new ArrayList<>(template.markers);
        this.identifiers = new ArrayList<>(template.identifiers);
        this.breakable = template.breakable;
        this.skippable = template.skippable;
        this.collapsible = template.collapsible;
        this.unitSize = template.unitSize;
        this.lastUnitSize = template.lastUnitSize;
        this.ids = new ArrayList<>(template.ids);
        this.lazyCollapse = template.lazyCollapse;
        this.keepWithNextSheets = template.keepWithNextSheets;
        this.keepWithPreviousSheets = template.keepWithPreviousSheets;
        this.avoidVolumeBreakAfterPriority = template.avoidVolumeBreakAfterPriority;
        this.lastInBlock = template.lastInBlock;
        this.mergeable = template.mergeable;
        this.lineProps = template.lineProps;
    }

    private static float getRowSpacing(float rowDefault, RowImpl r) {
        return (r.getRowSpacing() != null ? r.getRowSpacing() : rowDefault);
    }

    private static float calcUnitSize(float rowDefault, List<RowImpl> rows) {
        float t = 0;
        for (RowImpl r : rows) {
            if (!r.isInvisible()) {
                t += getRowSpacing(rowDefault, r);
            }
        }
        return t;
    }

    List<RowImpl> getRows() {
        return Collections.unmodifiableList(rows);
    }

    /**
     * Means that the page can be broken after this RowGroup.
     */
    @Override
    public boolean isBreakable() {
        return breakable;
    }

    /**
     * Means that this RowGroup can be skipped if a page break follows (e.g. bottom margins).
     */
    @Override
    public boolean isSkippable() {
        return skippable;
    }

    /**
     * Means that this RowGroup may be combined with preceding RowGroups (e.g. adjoining margins).
     */
    @Override
    public boolean isCollapsible() {
        return collapsible;
    }

    /**
     * <code>1</code> means <a href="http://braillespecs.github.io/pef/images/rendering.jpg">dot-to-dot height</a>.
     */
    @Override
    public float getUnitSize() {
        return unitSize;
    }

    @Override
    public boolean collapsesWith(Object obj) {
        if (lazyCollapse) {
            return collapsible;
        }
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            RowGroup other = (RowGroup) obj;
            if (this.rows == null) {
                return other.rows == null;
            } else {
                Row a = null;
                for (Row b : this.rows) {
                    if (a == null) {
                        a = b;
                    } else {
                        if (!a.equals(b)) {
                            return false;
                        }
                    }
                }
                for (Row b : other.rows) {
                    if (a == null) {
                        a = b;
                    } else {
                        if (!a.equals(b)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
    }

    @Override
    public List<String> getSupplementaryIDs() {
        return ids;
    }

    @Override
    public String toString() {
        return "RowGroup [rows=" + rows + ", variableWidth=" + mergeable + ", unitSize=" + unitSize + ", breakable="
                + breakable + ", skippable=" + skippable + ", collapsible=" + collapsible + ", ids=" + ids + "]";
    }

    @Override
    public float getLastUnitSize() {
        return lastUnitSize;
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    public List<String> getIdentifiers() {
        return identifiers;
    }

    public int getKeepWithNextSheets() {
        return keepWithNextSheets;
    }

    public int getKeepWithPreviousSheets() {
        return keepWithPreviousSheets;
    }

    /**
     * Gets the volume keep priority, never null.
     *
     * @return returns the volume keep priority
     */
    public VolumeKeepPriority getAvoidVolumeBreakAfterPriority() {
        return avoidVolumeBreakAfterPriority;
    }

    /**
     * Returns true if this {@link RowGroup} is the last one in a block.
     *
     * @return true if this row group ends a block, false otherwise
     */
    public boolean isLastRowGroupInBlock() {
        return lastInBlock;
    }


    /**
     * Returns true if this {@link RowGroup} is mergeable with a compatible
     * header or footer.
     *
     * @return true if this row group is mergeable, false otherwise
     */
    boolean isMergeable() {
        return mergeable;
    }

    LineProperties getLineProperties() {
        return lineProps;
    }
}
