package org.daisy.dotify.formatter.impl;

import java.util.Stack;

import org.daisy.dotify.api.formatter.BlockPosition;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.RenderingScenario;

/**
 * <p>Provides a block of rows and the properties associated with it.<p>
 * <p><b>Note that this class does not map directly to OBFL blocks.</b> 
 * OBFL has hierarchical blocks, which is represented by multiple
 * Block objects in sequence, a new one is created on each block boundary
 * transition.</p>
 * 
 * @author Joel Håkansson
 */

abstract class Block implements Cloneable {
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
	protected final Stack<Segment> segments;
	protected RowDataProperties rdp;
	private BlockPosition verticalPosition;
	protected Integer metaVolume = null, metaPage = null;
	private final RenderingScenario rs;

	Block(String blockId, RowDataProperties rdp) {
		this(blockId, rdp, null);
	}
	
	Block(String blockId, RowDataProperties rdp, RenderingScenario rs) {
		this.breakBefore = FormattingTypes.BreakBefore.AUTO;
		this.keep = FormattingTypes.Keep.AUTO;
		this.keepWithNext = 0;
		this.keepWithPreviousSheets = 0;
		this.keepWithNextSheets = 0;
		this.avoidVolumeBreakInsidePriority = null;
		this.avoidVolumeBreakAfterPriority = null;
		this.id = "";
		this.blockId = blockId;
		this.segments = new Stack<>();
		this.rdp = rdp;
		this.verticalPosition = null;
		this.rdm = null;
		this.rs = rs;
	}
	
	public abstract void addSegment(Segment s);

	public abstract void addSegment(TextSegment s);
	
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
	
	public Integer getVolumeKeepInsidePriority() {
		return avoidVolumeBreakInsidePriority;
	}
	
	public Integer getVolumeKeepAfterPriority() {
		return avoidVolumeBreakAfterPriority;
	}
	
	public String getIdentifier() {
		return id;
	}

	public BlockPosition getVerticalPosition() {
		return verticalPosition;
	}

	public void setBreakBeforeType(FormattingTypes.BreakBefore breakBefore) {
		this.breakBefore = breakBefore;
	}
	
	public void setKeepType(FormattingTypes.Keep keep) {
		this.keep = keep;
	}
	
	public void setKeepWithNext(int keepWithNext) {
		this.keepWithNext = keepWithNext;
	}
	
	public void setKeepWithPreviousSheets(int keepWithPreviousSheets) {
		this.keepWithPreviousSheets = keepWithPreviousSheets;
	}
	
	public void setKeepWithNextSheets(int keepWithNextSheets) {
		this.keepWithNextSheets = keepWithNextSheets;
	}
	
	public void setVolumeKeepInsidePriority(Integer priority) {
		this.avoidVolumeBreakInsidePriority = priority;
	}
	
	public void setVolumeKeepAfterPriority(Integer priority) {
		this.avoidVolumeBreakAfterPriority = priority;
	}
	
	public void setIdentifier(String id) {
		this.id = id;
	}

	/**
	 * Gets the vertical position of the block on page, or null if none is
	 * specified
	 */
	public void setVerticalPosition(BlockPosition vertical) {
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
		if (rdm==null || rdm.isVolatile()) {
			rdm = newBlockContentManager(context);
		}
		return rdm;
	}
	
	protected abstract AbstractBlockContentManager newBlockContentManager(BlockContext context);

	public void setMetaVolume(Integer metaVolume) {
		this.metaVolume = metaVolume;
	}

	public void setMetaPage(Integer metaPage) {
		this.metaPage = metaPage;
	}
	
	public RowDataProperties getRowDataProperties() {
		return rdp;
	}
	
	public void setRowDataProperties(RowDataProperties value) {
		rdp = value;
	}

	RenderingScenario getRenderingScenario() {
		return rs;
	}
	
	Integer getAvoidVolumeBreakAfterPriority() {
		return avoidVolumeBreakAfterPriority;
	}
	
	Integer getAvoidVolumeBreakInsidePriority() {
		return avoidVolumeBreakInsidePriority;
	}
	

	@Override
	public Object clone() {
    	try {
	    	Block newObject = (Block)super.clone();
	    	/* Probably no need to deep copy clone segments
	    	if (this.segments!=null) {
	    		newObject.segments = (Stack<Segment>)this.segments.clone();
	    	}*/
	    	return newObject;
    	} catch (CloneNotSupportedException e) { 
    	    // this shouldn't happen, since we are Cloneable
    	    throw new InternalError();
    	}
    }

}
