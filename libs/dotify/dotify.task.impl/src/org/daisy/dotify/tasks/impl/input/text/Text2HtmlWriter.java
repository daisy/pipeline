package org.daisy.dotify.tasks.impl.input.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

class Text2HtmlWriter extends Xml2ObflWriter {

	Text2HtmlWriter(InputStream is, OutputStream os, String encoding) {
		super(is, os, encoding);
	}

	Text2HtmlWriter(File input, File output, String encoding) throws FileNotFoundException {
		this(new FileInputStream(input), new FileOutputStream(output), encoding);
	}
	
	Text2HtmlWriter(String input, String output, String encoding) throws FileNotFoundException {
		this(new File(input), new File(output), encoding);
	}

	@Override
	protected void writePrologue(PrintWriter pw) {
		String outputEncoding = "utf-8";
		pw.println("<?xml version=\"1.0\" encoding=\""+outputEncoding+"\"?>");
		pw.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\""+getRootLang()+"\">");
		pw.println("<head>");
		pw.println("<meta charset=\""+outputEncoding+"\"/>");
		pw.println("</head>");
		pw.println("<body>");
	}

	@Override
	protected void startPara(PrintWriter pw) {
		pw.print("<p>");
	}

	@Override
	protected void endPara(PrintWriter pw) {
		pw.println("</p>");
	}

	@Override
	protected void writeEpilogue(PrintWriter pw) {
		pw.println("</body>");
		pw.println("</html>");
	}

}
