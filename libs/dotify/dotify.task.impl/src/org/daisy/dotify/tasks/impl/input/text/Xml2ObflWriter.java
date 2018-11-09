package org.daisy.dotify.tasks.impl.input.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

abstract class Xml2ObflWriter {
	private final InputStream is;
	private final OutputStream os;
	private final String encoding;
	
	private String rootLang;

	Xml2ObflWriter(InputStream is, OutputStream os, String encoding) {
		this.is = is;
		this.os = os;
		this.encoding = encoding;
		this.rootLang = "";
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
			writePrologue(pw);
			boolean first = true;
			while ((line =lnr.readLine()) != null) {
				if (first) { //handle possible byte order mark
					if (line.startsWith("\uFEFF")) {
						line = line.substring(1);
					}
					first = false;
				}
				startPara(pw);
				pw.print(line.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;"));
				if (line.length() == 0) {
					pw.print("&#x00A0;");
				}
				endPara(pw);
			}
			writeEpilogue(pw);
		} finally {
			try { lnr.close(); } catch (IOException e) {}
			pw.close();
			if (pw.checkError()) {
				throw new IOException("An error occured when writing to file.");
			}
		}
	}
	
	protected abstract void writePrologue(PrintWriter pw);

	protected abstract void writeEpilogue(PrintWriter pw);
	
	protected abstract void startPara(PrintWriter pw);
	
	protected abstract void endPara(PrintWriter pw);

}
