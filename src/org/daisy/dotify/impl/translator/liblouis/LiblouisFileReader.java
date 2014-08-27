package org.daisy.dotify.impl.translator.liblouis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.daisy.dotify.text.TextFileReader;
import org.daisy.dotify.tools.BrailleNotationConverter;

class LiblouisFileReader {
	private final ResourceResolver rr;
	private final LiblouisBrailleFilter.Builder cr;
	private final BrailleNotationConverter nc;
	
	private final Logger logger;

	/**
	 * Creates a new empty filter.
	 */
	public LiblouisFileReader(ResourceResolver resolver) {
		this.rr = resolver;
		this.cr = new LiblouisBrailleFilter.Builder();
		this.nc = new BrailleNotationConverter("-");
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
	}
	
	public LiblouisFileReader() {
		this(new ClassLoaderResourceResolver("resource-files/", Charset.forName("utf-8")));
	}
	
	public void parse(String path) throws IOException {
		parse(rr.resolve(path));
	}
	
	public LiblouisBrailleFilter getFilter() {
		return cr.build();
	}

	/**
	 * Parses a Liblouis input stream with the specified encoding and
	 * adds its contents to the filter.
	 * @param in
	 * @param encoding
	 * @throws IOException
	 */
	public void parse(ResourceDescriptor resource) throws IOException {
		TextFileReader tfr = new TextFileReader.Builder(resource.getInputStream()).
				charset(resource.getEncoding()).
				regex("\\s").build();
		
		TextFileReader.LineData ld;
		while ((ld= tfr.nextLine())!=null) {
			String[] f = ld.getFields();
			if ("uplow".equals(f[0])) {
				addUplow(f[1], f[2]);
			} else if ("punctuation".equals(f[0])) {
				addEntry(f[1], f[2], CharClass.PUNCTUATION);
			} else if ("space".equals(f[0])) {
				addEntry(f[1], f[2], CharClass.SPACE);
			} else if ("sign".equals(f[0])) {
				addEntry(f[1], f[2], CharClass.SIGN);
			} else if ("math".equals(f[0])) {
				addEntry(f[1], f[2], CharClass.MATH);
			} else if ("lowercase".equals(f[0])) {
				addEntry(f[1], f[2], CharClass.LOWERCASE);
			} else if ("uppercase".equals(f[0])) {
				addEntry(f[1], f[2], CharClass.UPPERCASE);
			} else if ("digit".equals(f[0])) {
				addEntry(f[1], f[2], CharClass.DIGIT);
			}
			else if ("include".equals(f[0])) {
				try {
					ResourceDescriptor rd = rr.resolve(f[1]);
					if (rd!=null) {
						parse(rd);
					} else {
						File f2 = new File(f[1]);
						if (f2.isFile()) {
							parse(new ResourceDescriptor(new FileInputStream(f2), resource.getEncoding()));
						} else {
							throw new FileNotFoundException(f[1]);
						}
					}
				} catch (IOException e) {
					logger.warning("Include not found: " + f[1]);
				}
			} 
			else if ("display".equals(f[0]) || "locale".equals(f[0])) {
				//ignore
			} else if ("numsign".equals(f[0])) {
				System.out.println("NUMSIGN");
				cr.numsign(nc.parseBrailleNotation(f[1]));
			} else if ("capsign".equals(f[0])) {
				cr.capsign(nc.parseBrailleNotation(f[1]));
			}
			else {
				System.out.println("Not implemented: " + ld.getLine());
			}
		}
	}
	
	public void addEntry(String value, String replacement, CharClass group) {
		int key = StringProcessor.unescape(value).codePointAt(0);
		cr.put(key, nc.parseBrailleNotation(replacement), group);
	}
	
	public void addUplow(String op, String value) {
		op = StringProcessor.unescape(op);
		if (op.length()!=2) {
			Logger.getLogger(this.getClass().getCanonicalName()).info("Uplow op incorrect: " + op);
		} else {
			String[] r = StringProcessor.unescape(value).split(",");
			if (r.length>2 || r.length<1) {
				Logger.getLogger(this.getClass().getCanonicalName()).info("Uplow value incorrect: " + value);
			} else {
				cr.put((int)op.charAt(0), nc.parseBrailleNotation(r[0]), CharClass.UPPERCASE);
				cr.put((int)op.charAt(1), nc.parseBrailleNotation(r[(r.length>1?1:0)]), CharClass.LOWERCASE);
			}
		}
	}

}
