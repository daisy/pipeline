package org.daisy.dotify.formatter.impl.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.DynamicRenderer;
import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.FormatterException;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.FormattingTypes.Keep;
import org.daisy.dotify.api.formatter.Leader;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.NumeralStyle;
import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.api.formatter.SpanProperties;
import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.api.formatter.TableProperties;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.Border;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.row.ListItem;
import org.daisy.dotify.formatter.impl.row.Margin;
import org.daisy.dotify.formatter.impl.row.Margin.Type;
import org.daisy.dotify.formatter.impl.row.MarginComponent;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.daisy.dotify.formatter.impl.row.SingleLineDecoration;
import org.daisy.dotify.formatter.impl.search.BlockAddress;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.AnchorSegment;
import org.daisy.dotify.formatter.impl.segment.ConnectedTextSegment;
import org.daisy.dotify.formatter.impl.segment.Evaluate;
import org.daisy.dotify.formatter.impl.segment.IdentifierSegment;
import org.daisy.dotify.formatter.impl.segment.LeaderSegment;
import org.daisy.dotify.formatter.impl.segment.MarkerSegment;
import org.daisy.dotify.formatter.impl.segment.NewLineSegment;
import org.daisy.dotify.formatter.impl.segment.PageNumberReferenceSegment;
import org.daisy.dotify.formatter.impl.segment.StyledSegmentGroup;
import org.daisy.dotify.formatter.impl.segment.TextSegment;

public class FormatterCoreImpl extends Stack<Block> implements FormatterCore, BlockGroup {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7775469339792146048L;
	private static final Logger logger = Logger.getLogger(FormatterCoreImpl.class.getCanonicalName());
	protected final Stack<AncestorContext> propsContext;
	private final long groupNumber;
	private BlockAddress currentBlockAddress;
	private Stack<MarginComponent> leftMarginComps;
	private Stack<MarginComponent> rightMarginComps;
	
	private Stack<Integer> blockIndentParent;
	private Stack<StyledSegmentGroup> styles;
	private int blockIndent;
	private ListItem listItem;
	protected RenderingScenario scenario;
	
	private final boolean discardIdentifiers;
	private Table table;
	protected final FormatterCoreContext fc;
	//The code where this variable is used is not very nice, but it will do to get the feature running
	private Boolean endStart = null;
	// TODO: fix recursive keep problem
	// TODO: Implement floating elements
	public FormatterCoreImpl(FormatterCoreContext fc) {
		this(fc, false);
	}
	
	public FormatterCoreImpl(FormatterCoreContext fc, boolean discardIdentifiers) {
		super();
		this.groupNumber = BlockAddress.getNextGroupNumber();
		this.fc = fc;
		this.propsContext = new Stack<>();
		this.leftMarginComps = new Stack<>();
		this.rightMarginComps = new Stack<>();
		this.listItem = null;
		this.blockIndent = 0;
		this.blockIndentParent = new Stack<>();
		this.styles = new Stack<StyledSegmentGroup>();
		blockIndentParent.add(0);
		this.discardIdentifiers = discardIdentifiers;
		this.scenario = null;
		this.currentBlockAddress = new BlockAddress(groupNumber, 0);
	}

	@Override
	public void startBlock(BlockProperties p) {
		startBlock(p, null);
	}

	@Override
	public void startBlock(BlockProperties p, String blockId) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		if (endStart!=null && endStart == true) {
			getCurrentBlock().setVolumeKeepAfterPriority(getCurrentVolumeKeepPriority());
		}
		endStart = null;
		String lb = "";
		String rb = "";
		if (p.getTextBorderStyle()!=null) {
			TextBorderStyle t = p.getTextBorderStyle();
			lb = t.getLeftBorder();
			rb = t.getRightBorder();
		}
		leftMarginComps.push(new MarginComponent(lb, p.getMargin().getLeftSpacing(), p.getPadding().getLeftSpacing()));
		rightMarginComps.push(new MarginComponent(rb, p.getMargin().getRightSpacing(), p.getPadding().getRightSpacing()));
		if (propsContext.size()>0) {
			addToBlockIndent(propsContext.peek().getBlockProperties().getBlockIndent());
		}

		RowDataProperties.Builder rdp = new RowDataProperties.Builder().
					textIndent(p.getTextBlockProperties().getTextIndent()).
					firstLineIndent(p.getTextBlockProperties().getFirstLineIndent()).
					align(p.getTextBlockProperties().getAlignment()).
					rowSpacing(p.getTextBlockProperties().getRowSpacing()).
					orphans(p.getOrphans()).
					widows(p.getWidows()).
					blockIndent(blockIndent).
					blockIndentParent(blockIndentParent.peek()).
					leftMargin(new Margin(Type.LEFT, leftMarginComps)).
					rightMargin(new Margin(Type.RIGHT, rightMarginComps)).
					outerSpaceBefore(p.getMargin().getTopSpacing()).
					underlineStyle(p.getUnderlineStyle());
		Block c = newBlock(blockId, rdp.build());
		if (propsContext.size()>0) {
			if (propsContext.peek().getBlockProperties().getListType()!=FormattingTypes.ListStyle.NONE) {
				String listLabel = p.getListItemLabel();
				switch (propsContext.peek().getBlockProperties().getListType()) {
				case OL:
					Integer item = null;
					if (listLabel!=null) {
						try {
							item = Integer.parseInt(listLabel);
							propsContext.peek().setListNumber(item);
						} catch (NumberFormatException e) {
							logger.log(Level.FINE, "Failed to convert a list item label to an integer.", e);
						}
					} else {
						item = propsContext.peek().nextListNumber();
					}
					if (item!=null) {
						NumeralStyle f = propsContext.peek().getBlockProperties().getListNumberFormat();
						listLabel = f.format(item.intValue());
					}
					break;
				case UL:
					if (listLabel==null) {
						listLabel = propsContext.peek().getBlockProperties().getDefaultListLabel();
						if (listLabel==null) {
							listLabel = "•";
						}
					}
					break;
				case PL: default:
					listLabel = "";
				}
				listItem = new ListItem(listLabel, propsContext.peek().getBlockProperties().getListType());
			}
		}
		c.setBreakBeforeType(p.getBreakBeforeType());
		c.setKeepType(p.getKeepType());
		c.setKeepWithNext(p.getKeepWithNext());
		if (!discardIdentifiers) {
			c.setIdentifier(p.getTextBlockProperties().getIdentifier());
		}
		c.setKeepWithNextSheets(p.getKeepWithNextSheets());
		c.setVerticalPosition(p.getVerticalPosition());
		AncestorContext ac = new AncestorContext(p, inheritVolumeKeepPriority(p.getVolumeKeepPriority()));
		// We don't get the volume keep priority from block properties, because it could have been inherited from an ancestor
		c.setVolumeKeepInsidePriority(ac.getVolumeKeepPriority());
		c.setVolumeKeepAfterPriority(ac.getVolumeKeepPriority());
		propsContext.push(ac);
		Block bi = getCurrentBlock();
		RowDataProperties.Builder builder = new RowDataProperties.Builder(bi.getRowDataProperties());
		if (p.getTextBorderStyle()!=null) {
			TextBorderStyle t = p.getTextBorderStyle();
			if (t.getTopLeftCorner().length()+t.getTopBorder().length()+t.getTopRightCorner().length()>0) {
				builder.leadingDecoration(new SingleLineDecoration(t.getTopLeftCorner(), t.getTopBorder(), t.getTopRightCorner()));
			}
		}
		builder.innerSpaceBefore(p.getPadding().getTopSpacing());
		bi.setRowDataProperties(builder.build());
		//firstRow = true;
	}
	
	private Integer inheritVolumeKeepPriority(Integer value) {
		return (value==null?getCurrentVolumeKeepPriority():value);
	}
	
	private Integer getCurrentVolumeKeepPriority() {
		return propsContext.isEmpty()?null:propsContext.peek().getVolumeKeepPriority();
	}
	
	private Integer getParentVolumeKeepPriority() {
		return propsContext.size()<2?null:propsContext.get(propsContext.size()-2).getVolumeKeepPriority();
	}

	@Override
	public void endBlock() {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		if (listItem!=null) {
			addChars("", new TextProperties.Builder(null).build());
		}
		if (endStart == null) {
			endStart = true;
		} else {
			endStart = false;
		}
		{
		AncestorContext ac = propsContext.pop();
		BlockProperties p = ac.getBlockProperties();
		Block bi = getCurrentBlock();
		RowDataProperties.Builder builder = new RowDataProperties.Builder(bi.getRowDataProperties());
		if (p.getTextBorderStyle()!=null) {
			TextBorderStyle t = p.getTextBorderStyle();
			if (t.getBottomLeftCorner().length()+ t.getBottomBorder().length()+ t.getBottomRightCorner().length()>0) {
				builder.trailingDecoration(new SingleLineDecoration(t.getBottomLeftCorner(), t.getBottomBorder(), t.getBottomRightCorner()));
			}
		}
		builder.innerSpaceAfter(p.getPadding().getBottomSpacing()).
			outerSpaceAfter(bi.getRowDataProperties().getOuterSpaceAfter()+p.getMargin().getBottomSpacing());
		bi.setKeepWithPreviousSheets(p.getKeepWithPreviousSheets());
		bi.setRowDataProperties(builder.build());
		//set the volume keep after for the closing block to the parent priority 
		bi.setVolumeKeepAfterPriority(getCurrentVolumeKeepPriority());
		if (bi.isEmpty()) {
			// if this group doesn't have data, then 
			// apply this blocks volume break after priority to the previous block 
			// if that block's break after priority is equal to this block's break
			// inside priority.
			Block preceding = size()>1?get(size()-2):null;
			if (preceding!=null && preceding.getAvoidVolumeBreakAfterPriority()==bi.getAvoidVolumeBreakInsidePriority()) {
				preceding.setAvoidVolumeBreakAfterPriority(bi.getAvoidVolumeBreakAfterPriority());
			}
		}
		}
		leftMarginComps.pop();
		rightMarginComps.pop();
		if (propsContext.size()>0) {
			AncestorContext ac = propsContext.peek(); 
			BlockProperties p = ac.getBlockProperties();
			Keep keep = p.getKeepType();
			int next = p.getKeepWithNext();
			subtractFromBlockIndent(p.getBlockIndent());
			RowDataProperties.Builder rdp = new RowDataProperties.Builder().
						textIndent(p.getTextBlockProperties().getTextIndent()).
						firstLineIndent(p.getTextBlockProperties().getFirstLineIndent()).
						align(p.getTextBlockProperties().getAlignment()).
						rowSpacing(p.getTextBlockProperties().getRowSpacing()).
						orphans(p.getOrphans()).
						widows(p.getWidows()).
						blockIndent(blockIndent).
						blockIndentParent(blockIndentParent.peek()).
						leftMargin(new Margin(Type.LEFT, leftMarginComps)). //.stackMarginComp(formatterContext, false, false)
						//leftMarginParent((Margin)leftMargin.clone()). //.stackMarginComp(formatterContext, true, false)
						rightMargin(new Margin(Type.RIGHT, rightMarginComps)). //.stackMarginComp(formatterContext, false, true)
						//rightMarginParent((Margin)rightMargin.clone())
						//.stackMarginComp(formatterContext, true, true)
						underlineStyle(p.getUnderlineStyle());
			Block c = newBlock(null, rdp.build());
			c.setKeepType(keep);
			c.setKeepWithNext(next);
			// We don't get the volume keep priority from the BlockProperties, as it could have been inherited from an ancestor
			c.setVolumeKeepInsidePriority(getCurrentVolumeKeepPriority());
			c.setVolumeKeepAfterPriority(getParentVolumeKeepPriority());
		}
		//firstRow = true;
	}
	
	public Block newBlock(String blockId, RowDataProperties rdp) {
		RegularBlock block = new RegularBlock(blockId, rdp, scenario);
		currentBlockAddress = new BlockAddress(groupNumber, currentBlockAddress.getBlockNumber()+1);
		block.setBlockAddress(currentBlockAddress);
		return this.push(block);
	}
	
	public Block getCurrentBlock() {
		return this.peek();
	}

	@Override
	public void insertMarker(Marker m) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		getCurrentBlock().addSegment(new MarkerSegment(m));
	}
	
	@Override
	public void insertAnchor(String ref) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		getCurrentBlock().addSegment(new AnchorSegment(ref));
	}

	@Override
	public void insertLeader(Leader leader) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		getCurrentBlock().addSegment(new LeaderSegment(leader));
	}

	@Override
	public void addChars(CharSequence c, TextProperties p) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		Block bl = getCurrentBlock();
		if (listItem!=null) {
			//append to this block
			RowDataProperties.Builder builder = new RowDataProperties.Builder(bl.getRowDataProperties());
			builder.listProperties(new ListItem(listItem.getLabel(), listItem.getType()));
			bl.setRowDataProperties(builder.build());
			//list item has been used now, discard
			listItem = null;
		}
		bl.addSegment(styles.isEmpty() ?
			new TextSegment(c.toString(), p) :
			new ConnectedTextSegment(c.toString(), p, styles.peek()));
	}

	@Override
	public void newLine() {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		getCurrentBlock().addSegment(new NewLineSegment());
	}

	@Override
	public void insertReference(String identifier, NumeralStyle numeralStyle) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		PageNumberReferenceSegment r; {
			if (styles.isEmpty()) {
				r = new PageNumberReferenceSegment(identifier, numeralStyle);
			} else {
				String[] style = new String[styles.size()];
				int i = 0;
				for (StyledSegmentGroup s : styles) {
					style[i++] = s.getName();
				}
				r = new PageNumberReferenceSegment(identifier, numeralStyle, style);
			}
		}
		getCurrentBlock().addSegment(r);
	}

	@Override
	public void insertEvaluate(DynamicContent exp, TextProperties t) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		Evaluate e; {
			if (styles.isEmpty()) {
				e = new Evaluate(exp, t);
			} else {
				String[] style = new String[styles.size()];
				int i = 0;
				for (StyledSegmentGroup s : styles) {
					style[i++] = s.getName();
				}
				e = new Evaluate(exp, t, style);
			}
		}
		getCurrentBlock().addSegment(e);
	}
	
	private void addToBlockIndent(int value) {
		blockIndentParent.push(blockIndent);
		blockIndent += value;
	}
	
	private void subtractFromBlockIndent(int value) {
		int test = blockIndentParent.pop();
		blockIndent -= value;
		assert blockIndent==test;
	}

	@Override
	public List<Block> getBlocks(FormatterContext context, DefaultContext c,
			CrossReferenceHandler crh) {
		return this;
	}

	@Override
	public boolean isGenerated() {
		return false;
	}

	@Override
	public void insertDynamicLayout(DynamicRenderer renderer) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		for (RenderingScenario rs : renderer.getScenarios()) {
			try {
				scenario = rs;
				//this is a downcast, which is generally unsafe, but it will work
				//here if it is used correctly, in other words, not calling table
				//methods from inside a block
				rs.renderScenario((FormatterCore)this);
			} catch (FormatterException e) {
				logger.log(Level.INFO, "Failed to render scenario.", e);
				//if the scenario fails here, it should be excluded from evaluation later (otherwise it might win)
				while (size()>0 && peek().getRenderingScenario()==rs) {
					//FIXME: this isn't enough, because other properties in this object may need to be rewound as well
					pop();
				}
			}
		}
		scenario = null;
		//TODO: The following is a quick workaround for a scenario grouping problem that should be solved in a better way. 
		//		It prevents consecutive dynamic renderers to be viewed as scenarios for the same dynamic renderer (leading to data loss).
		startBlock(new BlockProperties.Builder().build());
		endBlock();
	}
	
	@Override
	public void startTable(TableProperties props) {
		if (table!=null) {
			throw new IllegalStateException("A table is already open.");
		}
		String lb = "";
		String rb = "";
		TextBorderStyle borderStyle = null;
		if (props.getBorder()!=null) {
			Border b = props.getBorder();
			TextBorderFactoryMakerService tbf = fc.getTextBorderFactoryMakerService();
			Map<String, Object> features = new HashMap<String, Object>();
			features.put(TextBorderFactory.FEATURE_MODE, fc.getTranslatorMode());
			features.put("border", b);
			try {
				borderStyle = tbf.newTextBorderStyle(features);
			} catch (TextBorderConfigurationException e) {
				logger.log(Level.WARNING, "Failed to add border: " + b, e);
			}
		}
		if (borderStyle!=null) {
			lb = borderStyle.getLeftBorder();
			rb = borderStyle.getRightBorder();
		}
		leftMarginComps.push(new MarginComponent(lb, props.getMargin().getLeftSpacing(), props.getPadding().getLeftSpacing()));//
		rightMarginComps.push(new MarginComponent(rb, props.getMargin().getRightSpacing(), props.getPadding().getRightSpacing()));//
		RowDataProperties.Builder rdp = new RowDataProperties.Builder()
				//text properties are not relevant here, since a table block doesn't support mixed content
				//textIndent, firstLineIndent, align, orphans, widows, blockIndent and blockIndentParent
				//rowSpacing is handled by the table itself
				.leftMargin(new Margin(Type.LEFT, leftMarginComps))
				.rightMargin(new Margin(Type.RIGHT, rightMarginComps))
				//all margins are set here, because the table is an opaque block
				.outerSpaceBefore(props.getMargin().getTopSpacing())
				.outerSpaceAfter(props.getMargin().getBottomSpacing())
				.innerSpaceBefore(props.getPadding().getTopSpacing())
				.innerSpaceAfter(props.getPadding().getBottomSpacing());
		leftMarginComps.pop();
		rightMarginComps.pop();
		if (borderStyle!=null) {
			if (borderStyle.getTopLeftCorner().length()+borderStyle.getTopBorder().length()+borderStyle.getTopRightCorner().length()>0) {
				rdp.leadingDecoration(new SingleLineDecoration(borderStyle.getTopLeftCorner(), borderStyle.getTopBorder(), borderStyle.getTopRightCorner()));
			}
			if (borderStyle.getBottomLeftCorner().length()+ borderStyle.getBottomBorder().length()+ borderStyle.getBottomRightCorner().length()>0) {
				rdp.trailingDecoration(new SingleLineDecoration(borderStyle.getBottomLeftCorner(), borderStyle.getBottomBorder(), borderStyle.getBottomRightCorner()));
			}
		}
		table = new Table(fc, props, rdp.build(), fc.getTextBorderFactoryMakerService(), fc.getTranslatorMode(), scenario);
		currentBlockAddress = new BlockAddress(groupNumber, currentBlockAddress.getBlockNumber()+1);
		table.setBlockAddress(currentBlockAddress);
		add(table);
		//no need to create and configure a regular block (as is done in startBlock)
		//if there is a list item, we ignore it
		//no need to push context
	}

	@Override
	public void beginsTableHeader() {
		//no action, header is assumed in the implementation
	}

	@Override
	public void beginsTableBody() {
		table.beginsTableBody();
	}

	@Override
	public void beginsTableRow() {
		table.beginsTableRow();
	}

	@Override
	public FormatterCore beginsTableCell(TableCellProperties props) {
		return table.beginsTableCell(props);
	}

	@Override
	public void endTable() {
		//margins were cloned before adding the table's margins
		table.closeTable();
		table = null;
	}

	@Override
	public void startStyle(String style) {
		if (styles.isEmpty()) {
			styles.push(new StyledSegmentGroup(style, fc));
		} else {
			styles.push(new StyledSegmentGroup(style, styles.peek(), fc));
		}
	}

	@Override
	public void endStyle() {
		styles.pop();
	}

	@Override
	public void startSpan(SpanProperties props) {
		if (table!=null) {
			throw new IllegalStateException("A table is open.");
		}
		props.getIdentifier().ifPresent(id->getCurrentBlock().addSegment(new IdentifierSegment(id)));
	}

	@Override
	public void endSpan() {
		//NO OP
	}

}
