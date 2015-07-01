package org.daisy.dotify.impl.system.common;

import java.util.Properties;

/**
 * Provides parameters needed when running a Task system.
 * NOTE: some default properties are set in this class.
 * This behavior is misplaced and will be removed in future
 * versions. Users of this class are advised not to rely 
 * on the existence of these properties.
 * 
 * @author Joel Håkansson
 */
class RunParameters {
	private final Properties p;
	private final int flowWidth;
	private final int innerMargin;
	private final int outerMargin;
	private final float rowgap;
	
	RunParameters(Properties p) {
		this.p = p;
		this.flowWidth = Integer.parseInt(p.getProperty("cols", "28"));
		int pageHeight = Integer.parseInt(p.getProperty("rows", "29"));
		this.innerMargin = Integer.parseInt(p.getProperty("inner-margin", "5"));
		this.outerMargin = Integer.parseInt(p.getProperty("outer-margin", "2"));
		this.rowgap = Float.parseFloat(p.getProperty("rowgap", "0"));

		this.p.put("page-height", pageHeight);
		this.p.put("page-width", flowWidth+innerMargin+outerMargin);
		this.p.put("row-spacing", (rowgap/4)+1);
	}

	public String getProperty(Object key) {
		return p.get(key).toString();
	}
	
	public Iterable<Object> getKeys() {
		return p.keySet();
	}

	/**
	 * @return the flowWidth
	 */
	int getFlowWidth() {
		return flowWidth;
	}

	/**
	 * @return the pageHeight
	 *//*
	public int getPageHeight() {
		return pageHeight;
	}*/

	/**
	 * @return the innerMargin
	 */
	int getInnerMargin() {
		return innerMargin;
	}

	/**
	 * @return the outerMargin
	 */
	int getOuterMargin() {
		return outerMargin;
	}

	/**
	 * @return the rowgap
	 */
	float getRowgap() {
		return rowgap;
	}

	@Override
	public String toString() {
		return p.toString();
	}
}
