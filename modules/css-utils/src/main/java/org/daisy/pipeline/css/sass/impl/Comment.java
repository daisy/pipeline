package org.daisy.pipeline.css.sass.impl;

class Comment {

	final String text;
	final int column;

	// FIXME: take into account column number of first line of the document

	/**
	 * @param column Position of the comment's {@code /*} opening within the stylesheet document, as
	 *               a 0-based column number.
	 */
	Comment(String text, int column) {
		this.text = text;
		this.column = column;
	}

	Comment(Comment comment) {
		this(comment.text, comment.column);
	}

	@Override
	public String toString() {
		return "/*" + text + "*/";
	}
}
