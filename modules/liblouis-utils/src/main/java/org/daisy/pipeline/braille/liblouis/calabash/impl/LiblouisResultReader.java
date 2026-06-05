package org.daisy.pipeline.braille.liblouis.calabash.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

public class LiblouisResultReader extends Reader {
	
	private BufferedReader reader;
	
	private final CharBuffer lineBuffer = CharBuffer.allocate(1024);
	private boolean inFrontSection = false;
	private int linesRemainingInFrontSection = 0;
	private boolean endOfLine = false;
	private boolean endOfPage = false;
	private boolean endOfSection = false;
	private boolean endOfFile = false;
	
	public LiblouisResultReader(File resultFile, File bodyTempFile) throws FileNotFoundException {
		reader = asBufferedReader(resultFile);
		lineBuffer.flip();
		try {
			int linesInBody = countLines(bodyTempFile);
			inFrontSection = true;
			linesRemainingInFrontSection = countLines(resultFile) - linesInBody;
			if (linesRemainingInFrontSection < 0)
				throw new RuntimeException(); }
		catch (IOException e) {}
	}
	
	public void close() throws IOException {
		reader.close();
	}
	
	public int read(char cbuf[], int off, int len) throws IOException {
		int read = 0;
		while (read < len) {
			if (lineBuffer.hasRemaining())
				cbuf[off + read++] = lineBuffer.get();
			else if (endOfLine)
				cbuf[off + read++] = '\n';
			else if (endOfPage)
				cbuf[off + read++] = '\f';
			else {
				String line = readLine();
				if (line == null) {
					if (!nextPage())
						if (!nextSection()) {
							if (read == 0)
								return -1;
							else
								return read; }}
				else {
					lineBuffer.clear();
					lineBuffer.append(line).flip();
					endOfLine = true; }}}
		return read;
	}
	
	public String readLine() throws IOException {
		if (lineBuffer.hasRemaining()) {
			String line = lineBuffer.toString();
			lineBuffer.clear().flip();
			endOfLine = false;
			return line; }
		if (endOfPage || endOfSection || endOfFile)
			return null;
		reader.mark(1);
		if (reader.read() == '\f') {
			endOfPage = true;
			return null; }
		else
			reader.reset();
		String line = reader.readLine();
		if (line == null) {
			endOfPage = true;
			endOfSection = true;
			endOfFile = true;
			return null; }
		if (inFrontSection) {
			linesRemainingInFrontSection--;
			if (linesRemainingInFrontSection == 0) {
				endOfPage = true;
				endOfSection = true;
				reader.mark(1);
				if (reader.read() != '\f')
					reader.reset(); }}
		return line;
	}
	
	public String readPage() throws IOException {
		StringBuffer pageBuffer = new StringBuffer();
		String line;
		while ((line = readLine()) != null) {
			pageBuffer.append(line);
			pageBuffer.append('\n'); }
		nextPage();
		if (pageBuffer.length() == 0)
			return null;
		return pageBuffer.toString();
	}
	
	public String readSection() throws IOException {
		StringBuffer sectionBuffer = new StringBuffer();
		String page;
		while ((page = readPage()) != null) {
			sectionBuffer.append(page);
			sectionBuffer.append('\f'); }
		nextSection();
		if (sectionBuffer.length() == 0)
			return null;
		return sectionBuffer.toString();
	}
	
	public boolean nextPage() throws IOException {
		while (readLine() != null) {}
		endOfPage = false;
		return !(endOfSection || endOfFile);
	}
	
	public boolean nextSection() throws IOException {
		while (readPage() != null) {}
		endOfSection = false;
		inFrontSection = false;
		return !endOfFile;
	}
	
	private static int countLines(File file) throws IOException {
		BufferedReader r = asBufferedReader(file);
		int count = 0;
		while (r.readLine() != null)
			count++;
		r.close();
		return count;
	}
	
	private static BufferedReader asBufferedReader(File file) throws FileNotFoundException {
		try {
			return new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); }
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e); }
	}
}
