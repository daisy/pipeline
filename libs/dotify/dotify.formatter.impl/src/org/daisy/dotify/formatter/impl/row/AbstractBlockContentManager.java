package org.daisy.dotify.formatter.impl.row;

import java.util.List;
import java.util.Optional;

import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

public abstract class AbstractBlockContentManager implements BlockStatistics {
	//Immutable
	protected final int flowWidth;
	protected final RowDataProperties rdp;
	protected final BlockMargin margins;
	protected final BlockSpacing spacing;
	private final int minWidth;
	
	//Mutable
	protected final FormatterCoreContext fcontext;
	
	AbstractBlockContentManager(int flowWidth, RowDataProperties rdp, FormatterCoreContext fcontext) {
		this(flowWidth, rdp, fcontext, null);
	}
	
	protected AbstractBlockContentManager(int flowWidth, RowDataProperties rdp, FormatterCoreContext fcontext, Integer minWidth) {
		this.flowWidth = flowWidth;
		this.margins = new BlockMargin(rdp, fcontext.getSpaceCharacter());
		this.spacing = new BlockSpacing(margins, rdp, flowWidth, fcontext.getSpaceCharacter());
		this.fcontext = fcontext;
		this.rdp = rdp;
		this.minWidth = minWidth==null ? flowWidth-margins.getLeftMargin().getContent().length()-margins.getRightMargin().getContent().length() : minWidth;
	}
	
	protected AbstractBlockContentManager(AbstractBlockContentManager template) {
		this.flowWidth = template.flowWidth;
		this.rdp = template.rdp;
		this.margins = template.margins;
		this.spacing = template.spacing;
		this.minWidth = template.minWidth;
		// FIXME: fcontext is mutable, but mutating is related to DOM creation, and we assume for now that DOM creation is NOT going on when rendering has begun.
		this.fcontext = template.fcontext;
	}
	
	public abstract AbstractBlockContentManager copy();
	
	public abstract void setContext(DefaultContext context);
    
    /**
     * Returns true if the manager has more rows.
     * @return returns true if there are more rows, false otherwise
     */
    public abstract boolean hasNext();
    
	/**
	 * Returns true if the manager has some "significant" content.
	 * @return returns true if there is significant content, false otherwise.
	 */
	public abstract boolean hasSignificantContent();
	
    /**
     * Gets the next row from the manager with the specified width
     * @return returns the next row
     */
	public Optional<RowImpl> getNext() {
		return getNext(false);
	}
	
    public abstract Optional<RowImpl> getNext(boolean wholeWordsOnly);

	/**
	 * Returns true if this manager supports rows with variable maximum
	 * width, false otherwise.
	 * @return true if variable maximum width is supported, false otherwise
	 */
	public abstract boolean supportsVariableWidth();

    /**
     * Resets the state of the content manager to the first row.
     */
    public abstract void reset();

    public MarginProperties getLeftMarginParent() {
		return margins.getLeftParent();
	}

	public MarginProperties getRightMarginParent() {
		return margins.getRightParent();
	}

	public List<RowImpl> getCollapsiblePreContentRows() {
		return spacing.getCollapsiblePreContentRows();
	}
	
	public boolean hasCollapsiblePreContentRows() {
		return !spacing.getCollapsiblePreContentRows().isEmpty();
	}

	public List<RowImpl> getInnerPreContentRows() {
		return spacing.getInnerPreContentRows();
	}
	
	public boolean hasInnerPreContentRows() {
		return !spacing.getInnerPreContentRows().isEmpty();
	}

	public List<RowImpl> getPostContentRows() {
		return spacing.getPostContentRows();
	}
	
	public boolean hasPostContentRows() {
		return !spacing.getPostContentRows().isEmpty();
	}
	
	public List<RowImpl> getSkippablePostContentRows() {
		return spacing.getSkippablePostContentRows();
	}
	
	public boolean hasSkippablePostContentRows() {
		return !spacing.getSkippablePostContentRows().isEmpty();
	}
	
	@Override
	public int getMinimumAvailableWidth() {
		return minWidth;
	}

	/**
	 * Get markers that are not attached to a row, i.e. markers that proceeds any text contents
	 * @return returns markers that proceeds this FlowGroups text contents
	 */
	public abstract List<Marker> getGroupMarkers();
	
	public abstract List<String> getGroupAnchors();
	
	public abstract List<String> getGroupIdentifiers();
	
}
