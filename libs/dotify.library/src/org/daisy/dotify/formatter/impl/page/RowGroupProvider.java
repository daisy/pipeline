package org.daisy.dotify.formatter.impl.page;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.FormattingTypes.Keep;
import org.daisy.dotify.formatter.impl.core.Block;
import org.daisy.dotify.formatter.impl.core.BlockContext;
import org.daisy.dotify.formatter.impl.core.LayoutMaster;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.BlockStatistics;
import org.daisy.dotify.formatter.impl.row.LineProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;
import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.VolumeKeepPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Used by {@link RowGroupDataSource} to generate {@link RowGroup}s from a {@link Block}.
 *
 * <p>The input is a {@link Block} (and its corresponding {@link AbstractBlockContentManager}).</p>
 *
 * <p>The {@link RowGroup.Builder#avoidVolumeBreakAfterPriority(VolumeKeepPriority)
 * avoidVolumeBreakAfterPriority} of the {@link RowGroup}s are set to the {@link
 * Block#getAvoidVolumeBreakInsidePriority() getAvoidVolumeBreakInsidePriority} or {@link
 * Block#getAvoidVolumeBreakAfterPriority() getAvoidVolumeBreakAfterPriority} of the {@link Block},
 * depending on whether a RowGroup follows or not.</p>
 *
 * <p>Group markers, group anchors and group identifiers are added to the
 * first content RowGroup (content = not padding, margin or border).</p>
 *
 * <p>An empty RowGroup is produced if the block contains markers, anchors or identifiers but no
 * significant content (significant content = content that results in an actual line, i.e. non-empty
 * text, a non-empty evaluate, a br, a page-number, a leader).</p>
 */
class RowGroupProvider {
    private final LayoutMaster master;
    private final Block g;
    private final AbstractBlockContentManager bcm;
    private final BlockContext bc;

    private final OrphanWidowControl owc;
    private final boolean otherData;
    private DefaultContext context;
    private int rowIndex;
    private int phase;
    private int keepWithNext;
    private final boolean invisible;

    RowGroupProvider(RowGroupProvider template) {
        this.master = template.master;
        this.g = template.g;
        this.bcm = template.bcm == null ? null : template.bcm.copy();
        this.bc = template.bc;
        this.owc = template.owc;
        this.otherData = template.otherData;
        this.rowIndex = template.rowIndex;
        this.phase = template.phase;
        this.keepWithNext = template.keepWithNext;
        this.invisible = template.invisible;
    }

    RowGroupProvider(LayoutMaster master, Block g, AbstractBlockContentManager bcm, BlockContext bc, int keepWithNext) {
        this.master = master;
        this.g = g;
        this.bcm = bcm;
        this.bc = bc;
        this.phase = 0;
        this.rowIndex = 0;
        this.owc = new OrphanWidowControl(g.getRowDataProperties().getOrphans(),
            g.getRowDataProperties().getWidows(),
            bc.getRefs().getRowCount(g.getBlockAddress()));
        this.otherData = !bc.getRefs().getGroupAnchors(g.getBlockAddress()).isEmpty() ||
            !bc.getRefs().getGroupMarkers(g.getBlockAddress()).isEmpty() ||
            !bc.getRefs().getGroupIdentifiers(g.getBlockAddress()).isEmpty() ||
            g.getKeepWithNextSheets() > 0 || g.getKeepWithPreviousSheets() > 0;
        this.keepWithNext = keepWithNext;
        Condition dc = g.getRowDataProperties().getDisplayWhen();
        this.invisible = dc != null && !dc.evaluate(bc);
    }

    int getKeepWithNext() {
        return keepWithNext;
    }

    public boolean hasNext() {
        // these conditions must match the ones in next()
        return
            (phase < 1 && bcm.hasCollapsiblePreContentRows()) ||
            (phase < 2 && bcm.hasInnerPreContentRows()) ||
            (phase < 3 && shouldAddGroupForEmptyContent()) ||
            (phase < 4 && bcm.hasNext()) ||
            (phase < 5 && bcm.hasPostContentRows()) ||
            (phase < 6 && bcm.hasSkippablePostContentRows());
    }

    void close() {
        bc.getRefs().setGroupAnchors(g.getBlockAddress(), bcm.getGroupAnchors());
        bc.getRefs().setGroupMarkers(g.getBlockAddress(), bcm.getGroupMarkers());
        bc.getRefs().setGroupIdentifiers(g.getBlockAddress(), bcm.getGroupIdentifiers());
    }

    BlockStatistics getBlockStatistics() {
        return bcm;
    }

    BlockAddress getBlockAddress() {
        return g.getBlockAddress();
    }

    public RowGroup next(DefaultContext context, LineProperties lineProps) {
        if (this.context == null || !this.context.equals(context)) {
            this.context = g.contextWithMeta(context);
            bcm.setContext(this.context);
        }
        RowGroup b = nextInner(lineProps);
        return b;
    }

    private RowGroup nextInner(LineProperties lineProps) {
        // if the block does not support variable width (e.g. when there are left or right borders)
        // and header or footer fields are present on the line, insert an empty row
        if (lineProps.getReservedWidth() > 0 && !bcm.supportsVariableWidth()) {
            return new RowGroup.Builder(master.getRowSpacing()).add(makeInvisible(new RowImpl()))
                    .mergeable(false)
                    .lineProperties(lineProps)
                    .collapsible(false)
                    .skippable(false)
                    .breakable(false)
                    .build();
        }
        if (phase == 0) {
            phase++;
            //if there is a row group, return it (otherwise, try next phase)
            if (bcm.hasCollapsiblePreContentRows()) {
                return setPropertiesThatDependOnHasNext(
                    new RowGroup.Builder(master.getRowSpacing(), makeInvisible(bcm.getCollapsiblePreContentRows()))
                        .mergeable(true)
                        .lineProperties(lineProps)
                        .collapsible(true)
                        .skippable(false)
                        .breakable(false),
                    hasNext(),
                    g).build();
            }
        }
        if (phase == 1) {
            phase++;
            //if there is a row group, return it (otherwise, try next phase)
            if (bcm.hasInnerPreContentRows()) {
                return setPropertiesThatDependOnHasNext(
                    new RowGroup.Builder(master.getRowSpacing(), makeInvisible(bcm.getInnerPreContentRows()))
                        .mergeable(false)
                        .lineProperties(lineProps)
                        .collapsible(false)
                        .skippable(false)
                        .breakable(false),
                    hasNext(),
                    g
                ).build();
            }
        }
        if (phase == 2) {
            phase++;
            //TODO: Does this interfere with collapsing margins?
            if (shouldAddGroupForEmptyContent()) {
                RowGroup.Builder rgb = setPropertiesForFirstContentRowGroup(
                    new RowGroup.Builder(master.getRowSpacing(), new ArrayList<RowImpl>())
                        .mergeable(true)
                        .lineProperties(lineProps)
                        .collapsible(false)
                        .skippable(false)
                        .breakable(false),
                    bc.getRefs(),
                    g
                );
                return setPropertiesThatDependOnHasNext(rgb, hasNext(), g).build();
            }
        }
        if (phase == 3) {
            Optional<RowImpl> rt;
            if ((rt = bcm.getNext(lineProps)).isPresent()) {
                RowImpl r = rt.get();
                rowIndex++;
                boolean hasNext = bcm.hasNext();
                if (!hasNext) {
                    //we're at the last line, this should be kept with the next block's first line
                    keepWithNext = g.getKeepWithNext();
                    bc.getRefs().setRowCount(g.getBlockAddress(), bcm.getRowCount());
                }
                r = makeInvisible(r);
                RowGroup.Builder rgb = new RowGroup.Builder(master.getRowSpacing()).add(r)
                        .mergeable(bcm.supportsVariableWidth())
                        .lineProperties(lineProps)
                        .collapsible(false)
                        .skippable(false)
                        .breakable(
                            r.allowsBreakAfter() &&
                            owc.allowsBreakAfter(rowIndex - 1) &&
                            keepWithNext <= 0 &&
                            (Keep.AUTO == g.getKeepType() || !hasNext) &&
                            (hasNext || !bcm.hasPostContentRows())
                        );
                if (rowIndex == 1) { //First item
                    setPropertiesForFirstContentRowGroup(rgb, bc.getRefs(), g);
                }
                keepWithNext = keepWithNext - 1;
                return setPropertiesThatDependOnHasNext(rgb, hasNext(), g).build();
            } else {
                phase++;
            }
        }
        if (phase == 4) {
            phase++;
            if (bcm.hasPostContentRows()) {
                return setPropertiesThatDependOnHasNext(
                    new RowGroup.Builder(master.getRowSpacing(), makeInvisible(bcm.getPostContentRows()))
                        .mergeable(false)
                        .lineProperties(lineProps)
                        .collapsible(false)
                        .skippable(false)
                        .breakable(keepWithNext < 0),
                    hasNext(),
                    g
                ).build();
            }
        }
        if (phase == 5) {
            phase++;
            if (bcm.hasSkippablePostContentRows()) {
                return setPropertiesThatDependOnHasNext(
                    new RowGroup.Builder(master.getRowSpacing(), makeInvisible(bcm.getSkippablePostContentRows()))
                        .mergeable(true)
                        .lineProperties(lineProps)
                        .collapsible(true)
                        .skippable(true)
                        .breakable(keepWithNext < 0),
                    hasNext(),
                    g
                ).build();
            }
        }
        return null;
    }

    private boolean shouldAddGroupForEmptyContent() {
        return !bcm.hasSignificantContent() && otherData;
    }

    private static RowGroup.Builder setPropertiesForFirstContentRowGroup(
        RowGroup.Builder rgb,
        CrossReferenceHandler crh,
        Block g
    ) {
        return rgb.markers(crh.getGroupMarkers(g.getBlockAddress()))
            .anchors(crh.getGroupAnchors(g.getBlockAddress()))
            .identifiers(crh.getGroupIdentifiers(g.getBlockAddress()))
            .keepWithNextSheets(g.getKeepWithNextSheets())
            .keepWithPreviousSheets(g.getKeepWithPreviousSheets());
    }

    private static RowGroup.Builder setPropertiesThatDependOnHasNext(RowGroup.Builder rgb, boolean hasNext, Block g) {
        if (hasNext) {
            return rgb.avoidVolumeBreakAfterPriority(
                    VolumeKeepPriority.ofNullable(g.getAvoidVolumeBreakInsidePriority())
                )
                .lastRowGroupInBlock(false);
        } else {
            return rgb.avoidVolumeBreakAfterPriority(VolumeKeepPriority.ofNullable(
                    g.getAvoidVolumeBreakAfterPriority())
                )
                .lastRowGroupInBlock(true);
        }
    }

    /**
     * Make rows invisible based on the block context and the "display-when" property of the block.
     */
    private RowImpl makeInvisible(RowImpl row) {
        if (invisible) {
            row = new RowImpl.Builder(row).invisible(true).build();
        }
        return row;
    }

    private List<RowImpl> makeInvisible(List<RowImpl> rows) {
        if (invisible && rows.size() > 0) {
            rows = rows.stream().map(this::makeInvisible).collect(Collectors.toList());
        }
        return rows;
    }
}
