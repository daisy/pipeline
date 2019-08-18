package org.daisy.common.xproc.file;

import java.io.Reader;
import java.io.StringReader;

/**
 * Various file/document related utility functions for use in XProc steps
 */
public final class FileUtils {

	private FileUtils() {}

	/**
	 * Create a <a href="https://www.w3.org/TR/xproc/#cv.result"><code>c:result</code></a> document
	 *
	 * @param text the value of the text node that the c:result element should contain
	 * @return the document as a Reader object
	 */
	public static Reader cResultDocument(String text) {
		StringBuilder sb = new StringBuilder();
		sb.append("<c:result xmlns:c=\"http://www.w3.org/ns/xproc-step\">")
			.append(text)
			.append("</c:result>");
		return new StringReader(sb.toString());
	}
}
