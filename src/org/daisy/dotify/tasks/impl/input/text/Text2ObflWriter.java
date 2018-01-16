package org.daisy.dotify.tasks.impl.input.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

class Text2ObflWriter extends Xml2ObflWriter {
	private int height;
	private int width;

	Text2ObflWriter(InputStream is, OutputStream os, String encoding) {
		super(is, os, encoding);
		this.height = 29;
		this.width = 32;
	}

	Text2ObflWriter(File input, File output, String encoding) throws FileNotFoundException {
		this(new FileInputStream(input), new FileOutputStream(output), encoding);
	}
	
	Text2ObflWriter(String input, String output, String encoding) throws FileNotFoundException {
		this(new File(input), new File(output), encoding);
	}
	
	int getHeight() {
		return height;
	}

	void setHeight(int value) {
		this.height = value;
	}

	int getWidth() {
		return width;
	}
	
	void setWidth(int value) {
		this.width = value;
	}

	@Override
	protected void writePrologue(PrintWriter pw) {
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<obfl version=\"2011-1\" xml:lang=\"" + getRootLang() + "\" xmlns=\"http://www.daisy.org/ns/2011/obfl\">");
		pw.println("<layout-master name=\"default\" page-height=\""+height+"\" page-width=\""+width+"\">");
		pw.println("<default-template>");
		pw.println("<header></header>");
		pw.println("<footer></footer>");
		pw.println("</default-template>");
		pw.println("</layout-master>");
		pw.println("<sequence master=\"default\">");
	}

	@Override
	protected void startPara(PrintWriter pw) {
		pw.print("<block>");
	}

	@Override
	protected void endPara(PrintWriter pw) {
		pw.println("</block>");
	}

	@Override
	protected void writeEpilogue(PrintWriter pw) {
		pw.println("</sequence>");
		pw.println("</obfl>");
	}

}
