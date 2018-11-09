package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.BlockPosition;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.Segment.SegmentType;
import org.daisy.dotify.formatter.impl.segment.TextSegment;

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
	 * @return returns a new copy
	 */
	public abstract Block copy();

	abstract boolean isEmpty();
	
	protected abstract AbstractBlockContentManager newBlockContentManager(BlockContext context);
	
	void addSegment(Segment s) {
		markIfVolatile(s);
	}

	void addSegment(TextSegment s) {
		markIfVolatile(s);
	}
	
	private void markIfVolatile(Segment s) {
		if (s.getSegmentType()==SegmentType.Reference || s.getSegmentType()==SegmentType.Evaluate) {
			isVolatile = true;
		}
	}

	public BlockAddress getBlockAddress() {
		return blockAddress;
	}

	public void setBlockAddress(BlockAddress blockAddress) {
		this.blockAddress = blockAddress;
	}

	/**
	 * Returns true if this RowDataManager contains objects that makes the formatting volatile,
	 * i.e. prone to change due to for example cross references.
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
	
	Integer getVolumeKeepInsidePriority() {
		return avoidVolumeBreakInsidePriority;
	}
	
	Integer getVolumeKeepAfterPriority() {
		return avoidVolumeBreakAfterPriority;
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
	
	void setVolumeKeepInsidePriority(Integer priority) {
		this.avoidVolumeBreakInsidePriority = priority;
	}
	
	void setVolumeKeepAfterPriority(Integer priority) {
		this.avoidVolumeBreakAfterPriority = priority;
	}
	
	void setIdentifier(String id) {
		this.id = id;
	}

	/**
	 * Sets the vertical position of the block on page.
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
		if (rdm==null || isVolatile()) {
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
		return DefaultContext.from(dc).metaVolume(metaVolume).metaPage(metaPage).build();
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

}
