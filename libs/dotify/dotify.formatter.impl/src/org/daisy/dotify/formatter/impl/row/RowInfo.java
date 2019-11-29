package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.common.text.StringTools;

class RowInfo {
	private final String preContent;
	private final int available;

	/**
	 * @param preContent The content
	 * @param available The total space available on a row for content and left margin.
	 */
	RowInfo(String preContent, int available) {
		this.preContent = preContent;
		this.available = available;
	}

	/**
	 * @param r a row
	 * @return the space available on the row for additional content.
	 */
	int getMaxLength(RowImpl.Builder r) {
		int preContentPos = r.getLeftMargin().getContent().length()+StringTools.length(preContent);
		int maxLenText = available-(preContentPos);
		if (maxLenText<1) {
			throw new RuntimeException("Cannot continue layout: No space left for characters.");
		}
		return maxLenText;
	}

	int getPreTabPosition(RowImpl.Builder r) {
		return r.getLeftMargin().getContent().length()+StringTools.length(preContent)+StringTools.length(r.getText());
	}

	public String getPreContent() {
		return preContent;
	}

	public int getAvailable() {
		return available;
	}

}