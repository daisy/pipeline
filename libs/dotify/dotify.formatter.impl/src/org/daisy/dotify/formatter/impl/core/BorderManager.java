package org.daisy.dotify.formatter.impl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.common.text.StringTools;
import org.daisy.dotify.formatter.impl.row.MarginProperties;
import org.daisy.dotify.formatter.impl.row.RowImpl;

public class BorderManager {
	private static final Pattern trailingWs = Pattern.compile("\\s*\\z");
	private HeightCalculator hc;
	private List<Row> ret2;
	private DistributedRowSpacing rs = null;
	private RowImpl lastRow = null;
	private boolean closed;
	private final TextBorderStyle borderStyle;
	private final TextBorder border;
	private final LayoutMaster master;
	private final FormatterContext fcontext;
	private final int pageMargin;
	
	// This variable is used to compensate for the fact that the top border was
	// calculated outside of the main logic before
	// and can be removed once the logic has been updated.
	private final float offsetHeight;

	public BorderManager(LayoutMaster master, FormatterContext fcontext, int pageMargin) {
		this.hc = new HeightCalculator(master.getRowSpacing());
		this.ret2 = new ArrayList<>();
		this.closed = false;
		this.borderStyle = master.getBorder()!=null?master.getBorder():TextBorderStyle.NONE;
		this.border = buildBorder(borderStyle, master, fcontext, pageMargin);
		this.master = master;
		this.fcontext = fcontext;
		this.pageMargin = pageMargin;

		if (!TextBorderStyle.NONE.equals(borderStyle)) {
			addTopBorder();
		}
		this.offsetHeight = hc.getCurrentHeight();
	}

	public BorderManager(BorderManager template) {
		this.hc = new HeightCalculator(template.hc);
		this.ret2 = new ArrayList<>(template.ret2);
		this.rs = template.rs;
		this.closed = template.closed;
		this.offsetHeight = template.offsetHeight;
		this.borderStyle = template.borderStyle;
		this.border = template.border;
		this.master = template.master;
		this.fcontext = template.fcontext;
		this.pageMargin = template.pageMargin;
	}
	
	private static TextBorder buildBorder(TextBorderStyle borderStyle, LayoutMaster master, FormatterContext fcontext, int pageMargin) {
		int fsize = borderStyle.getLeftBorder().length() + borderStyle.getRightBorder().length();
		int w = master.getFlowWidth() + fsize + pageMargin;
		return new TextBorder.Builder(w, fcontext.getSpaceCharacter()+"")
				.style(borderStyle)
				.outerLeftMargin(pageMargin)
				.padToSize(!TextBorderStyle.NONE.equals(borderStyle))
				.build();
	}

	/**
	 * Gets the current height excluding the top border.
	 * @return returns the height without the top border
	 * @deprecated Update your code that depend on the height without the top border and then use {@link #getCurrentHeight()}
	 */
	@Deprecated
	public
	float getOffsetHeight() {
		// This method is used to compensate for the fact that the top border was
		// calculated outside of the main logic before
		// and can be removed once the logic has been updated.
		return hc.getCurrentHeight() - offsetHeight;
	}
	
	float getCurrentHeight() {
		return hc.getCurrentHeight();
	}

	public void addAll(Collection<? extends RowImpl> rows) {
		for (RowImpl r : rows) {
			addRow(r);
		}
	}

	public void addRow(RowImpl row) {
		if (closed) {
			throw new IllegalStateException("Cannot add rows when closed.");
		}
		// does the previously added row require additional processing?
		if (rs != null) {
			RowImpl s = null;
			for (int i = 0; i < rs.lines - 1; i++) {
				s = new RowImpl.Builder(border.addBorderToRow(lastRow.getLeftMargin().getContent(),
						lastRow.getRightMargin().getContent())).rowSpacing(rs.spacing).build();
				addRowInner(s);
			}
		}

		RowImpl.Builder r2Builder = addBorders(row);
		Float rs2 = row.getRowSpacing();
		if (!TextBorderStyle.NONE.equals(borderStyle)) {
			// distribute row spacing
			rs = master.distributeRowSpacing(rs2, true);
			r2Builder.rowSpacing(rs.spacing);
		} else {
			r2Builder.rowSpacing(rs2);
		}
		lastRow = r2Builder.build();
		addRowInner(lastRow);
	}

	public List<Row> getRows() {
		if (!closed) {
			close();
		}
		return ret2;
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean hasBorder() {
		return borderStyle != TextBorderStyle.NONE;
	}

	static String padLeft(int w, RowImpl row, char space) {
		String chars = trailingWs.matcher(row.getChars()).replaceAll("");
		return padLeft(w, chars, row.getLeftMargin(), row.getRightMargin(), row.getAlignment(), space);
	}

	private static String padLeft(int w, String text, MarginProperties leftMargin, MarginProperties rightMargin,
			FormattingTypes.Alignment align, char space) {
		if ("".equals(text) && leftMargin.isSpaceOnly() && rightMargin.isSpaceOnly()) {
			return "";
		} else {
			String r = leftMargin.getContent()
					+ StringTools.fill(space, align.getOffset(
							w - (leftMargin.getContent().length() + rightMargin.getContent().length() + text.length())))
					+ text;
			if (rightMargin.isSpaceOnly()) {
				return r;
			} else {
				return r + StringTools.fill(space, w - r.length() - rightMargin.getContent().length())
						+ rightMargin.getContent();
			}
		}
	}

	private void addTopBorder() {
		DistributedRowSpacing rs = master.distributeRowSpacing(master.getRowSpacing(), true);
		RowImpl r = new RowImpl.Builder(border.getTopBorder()).rowSpacing(rs.spacing).build();
		addRowInner(r);
	}

	private void close() {
		if (closed) {
			return;
		}
		closed = true;
		if (!TextBorderStyle.NONE.equals(borderStyle)) {
			addRowInner(new RowImpl(border.getBottomBorder()));
		}
		if (ret2.size() > 0) {
			int index = ret2.size() - 1;
			RowImpl last = (RowImpl) ret2.get(index);
			// Create a builder copy for editing
			RowImpl.Builder lastB = new RowImpl.Builder(last);
			if (master.getRowSpacing() != 1) {
				// set row spacing on the last row to 1.0
				lastB.rowSpacing(1f);
			} else if (last.getRowSpacing() != null) {
				// ignore row spacing on the last row if overall row spacing is
				// 1.0
				lastB.rowSpacing(null);
			}
			// Update the row
			ret2.set(index, lastB.build());
		}
	}

	private void addRowInner(Row r) {
		ret2.add(r);
		hc.addRow(r);
	}

	private RowImpl.Builder addBorders(RowImpl row) {
		String res = "";
		if (row.getChars().length() > 0) {
			// remove trailing whitespace
			String chars = trailingWs.matcher(row.getChars()).replaceAll("");
			res = border.addBorderToRow(padLeft(master.getFlowWidth(), chars, row.getLeftMargin(), row.getRightMargin(),
					row.getAlignment(), fcontext.getSpaceCharacter()), "");
		} else {
			if (!TextBorderStyle.NONE.equals(borderStyle)) {
				res = border.addBorderToRow(row.getLeftMargin().getContent(), row.getRightMargin().getContent());
			} else {
				if (!row.getLeftMargin().isSpaceOnly() || !row.getRightMargin().isSpaceOnly()) {
					res = TextBorder.addBorderToRow(master.getFlowWidth(), row.getLeftMargin().getContent(), "",
							row.getRightMargin().getContent(), fcontext.getSpaceCharacter() + "");
				} else {
					res = "";
				}
			}
		}
		int rowWidth = StringTools.length(res) + pageMargin;
		if (rowWidth > master.getPageWidth()) {
			throw new PaginatorException(
					"Row is too long (" + rowWidth + "/" + master.getPageWidth() + ") '" + res + "'");
		}
		return new RowImpl.Builder(res);
	}

}
