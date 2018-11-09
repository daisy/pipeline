package org.daisy.dotify.formatter.impl.page;

@FunctionalInterface
interface PageShape {

	/**
	 * Gets the row width for the specified offset. The first flowable row in a page
	 * is 0. If there are combined header/footer with flow, these rows count as
	 * flowable rows.
	 * If the offset is greater than the flow height, the returned value should
	 * be offset % flow-height
	 * @param pagenum the page number
	 * @param rowOffset the row offset
	 * @return returns the row width for the specified row
	 */
	int getWidth(int pagenum, int rowOffset);

}
