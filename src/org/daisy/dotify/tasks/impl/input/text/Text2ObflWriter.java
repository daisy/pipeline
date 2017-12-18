package org.daisy.dotify.tasks.impl.input.text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

class Text2ObflWriter {
	private final InputStream is;
	private final OutputStream os;
	private final String encoding;
	
	private int height;
	private int width;
	private String rootLang;

	Text2ObflWriter(InputStream is, OutputStream os, String encoding) {
		this.is = is;
		this.os = os;
		this.encoding = encoding;
		this.height = 29;
		this.width = 32;
		this.rootLang = "";
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
	
	String getRootLang() {
		return rootLang;
	}

	void setRootLang(String rootLang) {
		this.rootLang = rootLang;
	}

	void parse() throws IOException {
		LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is, encoding));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		String line;
		try {
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.println("<obfl version=\"2011-1\" xml:lang=\"" + rootLang + "\" xmlns=\"http://www.daisy.org/ns/2011/obfl\">");
			pw.println("<layout-master name=\"default\" page-height=\""+height+"\" page-width=\""+width+"\">");
			pw.println("<default-template>");
			pw.println("<header></header>");
			pw.println("<footer></footer>");
			pw.println("</default-template>");
			pw.println("</layout-master>");
			pw.println("<sequence master=\"default\">");
			boolean first = true;
			while ((line =lnr.readLine()) != null) {
				if (first) { //handle possible byte order mark
					if (line.startsWith("\uFEFF")) {
						line = line.substring(1);
					}
					first = false;
				}
				pw.print("<block>");
				pw.print(line.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;"));
				if (line.length() == 0) {
					pw.print("&#x00A0;");
				}
				pw.println("</block>");
			}
			pw.println("</sequence>");
			pw.println("</obfl>");
		} finally {
			try { lnr.close(); } catch (IOException e) {}
			pw.close();
			if (pw.checkError()) {
				throw new IOException("An error occured when writing to file.");
			}
		}
	}

}
