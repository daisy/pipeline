package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.api.formatter.TableProperties;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.Border;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.formatter.impl.Margin.Type;

class FormatterCoreImpl extends Stack<Block> implements FormatterCore, BlockGroup {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7775469339792146048L;
	private final static Logger logger = Logger.getLogger(FormatterCoreImpl.class.getCanonicalName());
	protected final Stack<AncestorContext> propsContext;
	private Margin leftMargin;
	private Margin rightMargin;
	
	private Stack<Integer> blockIndentParent;
	private int blockIndent;
	private ListItem listItem;
	protected RenderingScenario scenario;
	
	private final boolean discardIdentifiers;
	private Table table;
	protected final FormatterCoreContext fc;
	private MarkerProcessor mp;
	//The code where this variable is used is not very nice, but it will do to get the feature running
	private Boolean endStart = null;
	// TODO: fix recursive keep problem
	// TODO: Implement floating elements
	public FormatterCoreImpl(FormatterCoreContext fc) {
		this(fc, false);
	}
	
	public FormatterCoreImpl(FormatterCoreContext fc, boolean discardIdentifiers) {
		super();
		this.fc = fc;
		this.propsContext = new Stack<>();
		this.leftMargin = new Margin(Type.LEFT);
		this.rightMargin = new Margin(Type.RIGHT);
		this.listItem = null;
		this.blockIndent = 0;
		this.blockIndentParent = new Stack<>();
		blockIndentParent.add(0);
		this.discardIdentifiers = discardIdentifiers;
		this.scenario = null;
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
		leftMargin.push(new MarginComponent(lb, p.getMargin().getLeftSpacing(), p.getPadding().getLeftSpacing()));
		rightMargin.push(new MarginComponent(rb, p.getMargin().getRightSpacing(), p.getPadding().getRightSpacing()));
		if (propsContext.size()>0) {
			addToBlockIndent(propsContext.peek().getBlockProperties().getBlockIndent());
			if (propsContext.peek().getBlockProperties().getUnderlineStyle()!=null) {
				throw new UnsupportedOperationException("No block allowed within a block with underline properties.");
			}
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
					leftMargin((Margin)leftMargin.clone()).
					rightMargin((Margin)rightMargin.clone()).
					outerSpaceBefore(p.getMargin().getTopSpacing()).
					underlineStyle(p.getUnderlineStyle());
		Block c = newBlock(blockId, rdp.build());
		if (propsContext.size()>0) {
			if (propsContext.peek().getBlockProperties().getListType()!=FormattingTypes.ListStyle.NONE) {
				String listLabel;
				switch (propsContext.peek().getBlockProperties().getListType()) {
				case OL:
					listLabel = propsContext.peek().nextListNumber()+""; break;
				case UL:
					listLabel = "•";
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
		}
		leftMargin.pop();
		rightMargin.pop();
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
						leftMargin((Margin)leftMargin.clone()). //.stackMarginComp(formatterContext, false, false)
						//leftMarginParent((Margin)leftMargin.clone()). //.stackMarginComp(formatterContext, true, false)
						rightMargin((Margin)rightMargin.clone())//. //.stackMarginComp(formatterContext, false, true)
						//rightMarginParent((Margin)rightMargin.clone())
						; //.stackMarginComp(formatterContext, true, true)
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
		return this.push(new BlockWithConnectedSegments(blockId, rdp, scenario));
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
		getCurrentBlock().addSegment(new PageNumberReferenceSegment(identifier, numeralStyle));
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
				for (Style s : styles) {
					style[i++] = s.name;
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
		Margin leftMargin = (Margin)this.leftMargin.clone();
		Margin rightMargin = (Margin)this.rightMargin.clone();
		leftMargin.add(new MarginComponent(lb, props.getMargin().getLeftSpacing(), props.getPadding().getLeftSpacing()));//
		rightMargin.add(new MarginComponent(rb, props.getMargin().getRightSpacing(), props.getPadding().getRightSpacing()));//
		RowDataProperties.Builder rdp = new RowDataProperties.Builder()
				//text properties are not relevant here, since a table block doesn't support mixed content
				//textIndent, firstLineIndent, align, orphans, widows, blockIndent and blockIndentParent
				//rowSpacing is handled by the table itself
				.leftMargin(leftMargin)
				.rightMargin(rightMargin)
				//all margins are set here, because the table is an opaque block
				.outerSpaceBefore(props.getMargin().getTopSpacing())
				.outerSpaceAfter(props.getMargin().getBottomSpacing())
				.innerSpaceBefore(props.getPadding().getTopSpacing())
				.innerSpaceAfter(props.getPadding().getBottomSpacing());
		if (borderStyle!=null) {
			if (borderStyle.getTopLeftCorner().length()+borderStyle.getTopBorder().length()+borderStyle.getTopRightCorner().length()>0) {
				rdp.leadingDecoration(new SingleLineDecoration(borderStyle.getTopLeftCorner(), borderStyle.getTopBorder(), borderStyle.getTopRightCorner()));
			}
			if (borderStyle.getBottomLeftCorner().length()+ borderStyle.getBottomBorder().length()+ borderStyle.getBottomRightCorner().length()>0) {
				rdp.trailingDecoration(new SingleLineDecoration(borderStyle.getBottomLeftCorner(), borderStyle.getBottomBorder(), borderStyle.getBottomRightCorner()));
			}
		}
		table = new Table(fc, props, rdp.build(), fc.getTextBorderFactoryMakerService(), fc.getTranslatorMode(), scenario);
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
			styles.push(new Style(style));
		} else {
			styles.push(new Style(style, styles.peek()));
		}
	}

	@Override
	public void endStyle() {
		styles.pop();
	}
	
	private Stack<Style> styles = new Stack<Style>();
	
	static class SegmentGroup {
		
		final List<Object> segments = new ArrayList<Object>();
		int n = 0;
		
		/*
		 * @returns the index of segment inside the group
		 */
		int add(TextSegment segment) {
			segments.add(segment);
			n++;
			return n - 1;
		}
		
		/*
		 * @returns the index of the child group inside the parent group
		 */
		int add(SegmentGroup group) {
			segments.add(group);
			n++;
			return n - 1;
		}
		
		TextSegment getSegmentAt(int idx) {
			return (TextSegment)segments.get(idx);
		}
		
		SegmentGroup getGroupAt(int idx) {
			return (SegmentGroup)segments.get(idx);
		}
	}
	
	/*
	 * Associates a text style with a group of segments.
	 */
	class Style extends SegmentGroup {
		
		final Style parentStyle;
		final int idx;
		final String name;
		
		Style(String name) {
			this(name, null);
		}
		
		Style(String name, Style parentStyle) {
			super();
			this.parentStyle = parentStyle;
			if (parentStyle != null)
				idx = parentStyle.add(this);
			else
				idx = -1;
			this.name = name;
		}
		
		SegmentGroup processAttributes;
		SegmentGroup processAttributes() {
			if (parentStyle != null) {
				return parentStyle.processAttributes().getGroupAt(idx);
			} else {
				
				// FIXME: either make group incl. children immutable, or recompute whenever group is mutated
				if (processAttributes == null) {
					List<String> text = _text(segments);
					TextAttribute attributes = _attributes(name, segments);
					if (mp == null) {
						try {
							String locale = fc.getConfiguration().getLocale();
							String mode = fc.getTranslatorMode();
							mp = fc.getMarkerProcessorFactoryMakerService().newMarkerProcessor(locale, mode);
						} catch (MarkerProcessorConfigurationException e) {
							throw new IllegalArgumentException(e);
						}
					}
					String[] processedText = mp.processAttributesRetain(attributes, text.toArray(new String[text.size()]));
					processAttributes = _processAttributes(segments, Arrays.asList(processedText).iterator());
				}
				return processAttributes;
			}
		}
	}
		
	static List<String> _text(List<Object> segments) {
		List<String> l = new ArrayList<String>();
		for (Object o : segments) {
			if (o instanceof TextSegment) {
				TextSegment s = (TextSegment)o;
				l.add(s.getText());
			} else {
				SegmentGroup g = (SegmentGroup)o;
				l.addAll(_text(g.segments));
			}
		}
		return l;
	}
	
	static TextAttribute _attributes(String name, List<Object> segments) {
		DefaultTextAttribute.Builder b = new DefaultTextAttribute.Builder(name);
		int w = 0;
		for (Object o : segments) {
			if (o instanceof TextSegment) {
				TextSegment s = (TextSegment)o;
				TextAttribute a = new DefaultTextAttribute.Builder().build(s.getText().length());
				b.add(a);
				w += a.getWidth();
			} else if (o instanceof Style) {
				Style s = (Style)o;
				TextAttribute a = _attributes(s.name, s.segments);
				b.add(a);
				w += a.getWidth();
			} else {
				SegmentGroup g = (SegmentGroup)o;
				TextAttribute a = _attributes(null, g.segments);
				b.add(a);
				w += a.getWidth();
			}
		}
		return b.build(w);
	}
	
	static SegmentGroup _processAttributes(List<Object> segments, Iterator<String> processedText) {
		SegmentGroup processedGroup = new SegmentGroup();
		for (Object o : segments) {
			if (o instanceof TextSegment) {
				processedGroup.add(new TextSegment(processedText.next(), ((TextSegment)o).getTextProperties()));
			} else {
				processedGroup.add(_processAttributes(((Style)o).segments, processedText));
			}
		}
		return processedGroup;
	}

	
	/*
	 * Text segment that is "connected" with other segments through Style elements.
	 */
	static class ConnectedTextSegment extends TextSegment {
		
		final Style parentStyle;
		final int idx;
		final int width;
		
		ConnectedTextSegment(String chars, TextProperties tp, Style parentStyle) {
			super(chars, tp);
			this.parentStyle = parentStyle;
			idx = parentStyle.add(this);
			width = chars.length();
		}
		
		@Override
		public TextAttribute getTextAttribute() {
			DefaultTextAttribute.Builder b = new DefaultTextAttribute.Builder();
			Style s = parentStyle;
			while (s != null) {
				b = new DefaultTextAttribute.Builder(s.name).add(b.build(width));
				s = s.parentStyle;
			}
			return b.build(width);
		}
		
		TextSegment processAttributes() {
			return parentStyle.processAttributes().getSegmentAt(idx);
		}
	}

	static class BlockWithConnectedSegments extends RegularBlock {
		
		BlockWithConnectedSegments(String blockId, RowDataProperties rdp, RenderingScenario scenario) {
			super(blockId, rdp, scenario);
		}
		
		@Override
		protected AbstractBlockContentManager newBlockContentManager(BlockContext context) {
			Stack<Segment> processedSegments = processAttributes(segments);
			segments.clear();
			for (Segment s : processedSegments)
				if (s instanceof TextSegment) {
					// cast to TextSegment in order to enable merging
					addSegment((TextSegment)s);
				} else {
					addSegment(s);
				}
			return super.newBlockContentManager(context);
		}
		
		/*
		 * Process non-null text attributes of text segments. "Connected" segments are processed
		 * together.
		 */
		static Stack<Segment> processAttributes(Stack<Segment> segments) {
			Stack<Segment> processedSegments = new Stack<Segment>();
			for (Segment s : segments) {
				if (s instanceof ConnectedTextSegment) {
					s = ((ConnectedTextSegment)s).processAttributes();
				}
				processedSegments.push(s);
			}
			return processedSegments;
		}
	}
}
