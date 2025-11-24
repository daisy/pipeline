package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.BlockPosition;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.Segment;

/**
 * <p>Provides a block of rows and the properties associated with it.</p>
 * <p><b>Note that this class does not map directly to OBFL blocks.</b>
 * OBFL has hierarchical blocks, which is represented by multiple
 * Block objects in sequence, a new one is created on each block boundary
 * transition.</p>
 *
 * @author Joel Håkansson
 */

public abstract class Block {
    private BlockContext context;
    private AbstractBlockContentManager rdm;
    private final String blockId;
    private FormattingTypes.BreakBefore breakBefore;
    private FormattingTypes.Keep keep;
    private int keepWithNext;
    private int keepWithPreviousSheets;
    private int keepWithNextSheets;
    private Integer avoidVolumeBreakInsidePriority;
    private Integer avoidVolumeBreakAfterPriority;
    private String id;
    protected RowDataProperties rdp;
    private BlockPosition verticalPosition;
    protected Integer metaVolume;
    protected Integer metaPage;
    private final RenderingScenario rs;
    private boolean isVolatile;
    private BlockAddress blockAddress;
    private TreeInfo treeInfo;

    Block(String blockId, RowDataProperties rdp, RenderingScenario rs) {
        this.context = null;
        this.rdm = null;
        this.blockId = blockId;
        this.breakBefore = FormattingTypes.BreakBefore.AUTO;
        this.keep = FormattingTypes.Keep.AUTO;
        this.keepWithNext = 0;
        this.keepWithPreviousSheets = 0;
        this.keepWithNextSheets = 0;
        this.avoidVolumeBreakInsidePriority = null;
        this.avoidVolumeBreakAfterPriority = null;
        this.id = "";
        this.rdp = rdp;
        this.verticalPosition = null;
        this.metaVolume = null;
        this.metaPage = null;
        this.rs = rs;
        this.isVolatile = false;
    }

    Block(Block template) {
        this.context = template.context;
        this.rdm = template.rdm;
        this.blockId = template.blockId;
        this.breakBefore = template.breakBefore;
        this.keep = template.keep;
        this.keepWithNext = template.keepWithNext;
        this.keepWithPreviousSheets = template.keepWithPreviousSheets;
        this.keepWithNextSheets = template.keepWithNextSheets;
        this.avoidVolumeBreakInsidePriority = template.avoidVolumeBreakInsidePriority;
        this.avoidVolumeBreakAfterPriority = template.avoidVolumeBreakAfterPriority;
        this.id = template.id;
        this.rdp = template.rdp;
        this.verticalPosition = template.verticalPosition;
        this.metaVolume = template.metaVolume;
        this.metaPage = template.metaPage;
        this.rs = template.rs;
        this.isVolatile = template.isVolatile;
        this.blockAddress = template.blockAddress;
    }

    /**
     * Makes a copy of the block. For now, the copy is shallow (like the previous clone method was).
     *
     * @return returns a new copy
     */
    public abstract Block copy();

    abstract boolean isEmpty();

    protected abstract AbstractBlockContentManager newBlockContentManager(BlockContext context);

    /**
     * Opens a text style, such as emphasis or strong. Note that the style is
     * expected to be closed eventually with {@link #endStyle()}.
     *
     * @param style the name of the style
     */
    abstract void startStyle(String style);

    /**
     * Ends an open text style. Calling this method without a preceding call to
     * {@link #startStyle(String)} may throw an exception.
     */
    abstract void endStyle();

    void addSegment(Segment s) {
        markIfVolatile(s);
    }

    private void markIfVolatile(Segment s) {
        switch (s.getSegmentType()) {
        case PageReference:
        case MarkerReference:
        case Evaluate:
            isVolatile = true;
            break;
        default:
        }
    }

    public BlockAddress getBlockAddress() {
        return blockAddress;
    }

    public void setBlockAddress(BlockAddress blockAddress) {
        this.blockAddress = blockAddress;
    }

    /**
     * Set info about the original tree structure, in terms of how
     * many ancestor nodes this block's origin node shares with the
     * origin node of the block that preceeds this one in the
     * sequence. This is relevant for determining which blocks form a
     * unit in the context of orphan and widow control for instance.
     *
     * The tree info from all the blocks in a sequence combined can be
     * used to reconstruct the original tree hierarchy.
     *
     * @param closedAncestors number of ancestor nodes of the
     *                        preceding block's origin node that are
     *                        not ancestors of the current block's
     *                        origin node
     * @param currentAncestors total number of ancestor nodes (depth)
     *                         of the current block's origin node
     */
    public void setTreeInfo(int closedAncestors, int currentAncestors) {
        this.treeInfo = new TreeInfo(closedAncestors, currentAncestors);
    }

    private static class TreeInfo {
        final int closedAncestors, currentAncestors;
        TreeInfo(int closedAncestors, int currentAncestors) {
            if (closedAncestors < 0 || currentAncestors < 0) {
                throw new IllegalArgumentException();
            }
            this.closedAncestors = closedAncestors;
            this.currentAncestors = currentAncestors;
        }
    }

    /**
     * Get the number of ancestors of this block's origin node.
     *
     * @return the number of ancestors
     */
    public int getAncestors() {
        return treeInfo != null ? treeInfo.currentAncestors : 0;
    }

    /**
     * Get the number of common ancestors that this block's origin
     * node has with the preceding block's origin node.
     *
     * @param previousBlock the block that preceeds this block in the sequence
     * @return the number of common ancestors
     */
    public int getCommonAncestors(Block previousBlock) {
        if (treeInfo == null || previousBlock == null || previousBlock.treeInfo == null) {
            return 0;
        } else {
            return previousBlock.treeInfo.currentAncestors - treeInfo.closedAncestors;
        }
    }

    /**
     * Returns true if this RowDataManager contains objects that makes the formatting volatile,
     * i.e. prone to change due to for example cross references.
     *
     * @return returns true if, and only if, the RowDataManager should be discarded if a new pass is requested,
     * false otherwise
     */
    boolean isVolatile() {
        return isVolatile;
    }

    public FormattingTypes.BreakBefore getBreakBeforeType() {
        return breakBefore;
    }

    public FormattingTypes.Keep getKeepType() {
        return keep;
    }

    public int getKeepWithNext() {
        return keepWithNext;
    }

    public int getKeepWithPreviousSheets() {
        return keepWithPreviousSheets;
    }

    public int getKeepWithNextSheets() {
        return keepWithNextSheets;
    }

    public String getIdentifier() {
        return id;
    }

    public BlockPosition getVerticalPosition() {
        return verticalPosition;
    }

    void setBreakBeforeType(FormattingTypes.BreakBefore breakBefore) {
        this.breakBefore = breakBefore;
    }

    void setKeepType(FormattingTypes.Keep keep) {
        this.keep = keep;
    }

    void setKeepWithNext(int keepWithNext) {
        this.keepWithNext = keepWithNext;
    }

    void setKeepWithPreviousSheets(int keepWithPreviousSheets) {
        this.keepWithPreviousSheets = keepWithPreviousSheets;
    }

    void setKeepWithNextSheets(int keepWithNextSheets) {
        this.keepWithNextSheets = keepWithNextSheets;
    }

    void setIdentifier(String id) {
        this.id = id;
    }

    /**
     * Sets the vertical position of the block on page.
     *
     * @param vertical the position
     */
    void setVerticalPosition(BlockPosition vertical) {
        this.verticalPosition = vertical;
    }

    public String getBlockIdentifier() {
        return blockId;
    }

    public AbstractBlockContentManager getBlockContentManager(BlockContext context) {
        if (!context.equals(this.context)) {
            //invalidate, if existing
            rdm = null;
        }
        this.context = context;
        if (rdm == null || isVolatile()) {
            rdm = newBlockContentManager(context);
        } else {
            rdm.reset();
        }
        return rdm;
    }

    public void setMetaVolume(Integer metaVolume) {
        this.metaVolume = metaVolume;
    }

    public void setMetaPage(Integer metaPage) {
        this.metaPage = metaPage;
    }

    public DefaultContext contextWithMeta(DefaultContext dc) {
        return DefaultContext
                .from(dc)
                .metaVolume(metaVolume)
                .metaPage(metaPage)
                .build();
    }

    public RowDataProperties getRowDataProperties() {
        return rdp;
    }

    void setRowDataProperties(RowDataProperties value) {
        rdp = value;
    }

    public RenderingScenario getRenderingScenario() {
        return rs;
    }

    public Integer getAvoidVolumeBreakAfterPriority() {
        return avoidVolumeBreakAfterPriority;
    }

    void setAvoidVolumeBreakAfterPriority(Integer value) {
        this.avoidVolumeBreakAfterPriority = value;
    }

    public Integer getAvoidVolumeBreakInsidePriority() {
        return avoidVolumeBreakInsidePriority;
    }

    void setAvoidVolumeBreakInsidePriority(Integer value) {
        this.avoidVolumeBreakInsidePriority = value;
    }

}
