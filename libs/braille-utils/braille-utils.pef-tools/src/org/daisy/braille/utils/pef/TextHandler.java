/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.utils.pef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableCatalogService;

/**
 * Provides a handler for reading text and writing a PEF-file.
 * @author Joel Håkansson
 */
public class TextHandler {
	/**
	 * Defines a date format (yyyy-MM-dd).
	 */
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the title of the publication
	 */
	public static final String KEY_TITLE = "title";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the author of the publication
	 */
	public static final String KEY_AUTHOR = "author";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the identifier for the publication 
	 */
	public static final String KEY_IDENTIFIER = "identifier";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should match the table to use
	 */
	public static final String KEY_MODE = "mode";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should contain the language of the publication
	 */
	public static final String KEY_LANGUAGE = "language";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should be "true" for duplex or "false" for simplex
	 */
	public static final String KEY_DUPLEX = "duplex";
	/**
	 * Key for parseTextFile setting,
	 * corresponding settings value should be a string containing a valid date on the form yyyy-MM-dd
	 */
	public static final String KEY_DATE = "date";

	private final File input;
	private final File output;
	private final TableCatalogService factory;
	private final String title;
	private final String author;
	private final String language;
	private final String identifier;
	private final BrailleConverter converter;
	private final boolean duplex;
	private Date date;

	private int maxRows;
	private int maxCols;

	/**
	 * Provides a Builder for TextHandler
	 */
	public static class Builder {
		// required params
		private final File input;
		private final File output;
		private final TableCatalogService factory;

		// optional params
		private String title = "";
		private String author = "";
		private String language = "";
		private String identifier = "";
		private String converterId = null;
		private boolean duplex = true;
		private Date date = new Date();

		/**
		 * Create a new TextParser builder
		 * @param input the input
		 * @param output the output
		 * @param factory the table catalog
		 */
		public Builder(File input, File output, TableCatalogService factory) {
			this.input = input;
			this.output = output;
			this.factory = factory;
			String s = (new Long((Math.round(Math.random() * 1000000000)))).toString();
			char[] chars = s.toCharArray();
			char[] dest = new char[] {'0','0','0','0','0','0','0','0','0'};
			System.arraycopy(chars, 0, dest, 9-chars.length, chars.length);
			this.identifier = "AUTO_ID_" + new String(dest);
		}
		
		/**
		 * Sets options from a map.
		 * @param settings the settings.
		 * @return returns this object
		 * @throws IllegalArgumentException if a key is unknown or if a value doesn't meet the requirements.
		 */
		public Builder options(Map<String, String> settings) {
			for (String key : settings.keySet()) {
				String value = settings.get(key);
				if (KEY_TITLE.equals(key)) {
					title(value);
				} else if (KEY_AUTHOR.equals(key)) {
					author(value);
				} else if (KEY_IDENTIFIER.equals(key)) {
					identifier(value);
				} else if (KEY_MODE.equals(key)) {
					converterId(value);
				} else if (KEY_LANGUAGE.equals(key)) {
					language(value);
				} else if (KEY_DUPLEX.equals(key)) {
					duplex("true".equals(value.toLowerCase()));
				} else if (KEY_DATE.equals(key)) {
					try {
						date(DATE_FORMAT.parse(value));
					} catch (ParseException e) {
						throw new IllegalArgumentException(e);
					}
				} else {
					throw new IllegalArgumentException("Unknown option \"" + key + "\"");
				}
			}
			return this;
		}

		//init optional params here
		/**
		 * Sets the title for publications created using TextHandlers created with this builder.
		 * @param value the title
		 * @return returns this object
		 */
		public Builder title(String value) {
			if (value==null) throw new IllegalArgumentException("Null value not accepted.");
			title = value; return this; 
		}
		/**
		 * Sets the author for publications created using TextHandlers created with this builder.
		 * @param value the author
		 * @return returns this object
		 */
		public Builder author(String value) {
			if (value==null) throw new IllegalArgumentException("Null value not accepted.");
			author = value; return this;
		}
		/**
		 * Sets the language for publications created using TextHandlers created with this builder.
		 * @param value the language
		 * @return returns this object
		 */
		public Builder language(String value) {
			if (value==null) throw new IllegalArgumentException("Null value not accepted.");
			language = value; return this;
		}
		/**
		 * Sets the identifier for publications created using TextHandlers created with this builder.
		 * @param value the identifier
		 * @return returns this object
		 */
		public Builder identifier(String value) {
			if (value==null) throw new IllegalArgumentException("Null value not accepted.");
			identifier = value; return this;
		}
		/**
		 * Sets the converter identifier to be used when creating a TextHandler. See TableCatalog
		 * for available values. If none is suppled, the builder will attempt to select one
		 * based on file input characteristics.
		 * @param value the identifier for the converter
		 * @return returns this object
		 */
		public Builder converterId(String value) {
			converterId = value;
			return this;
		}
		/**
		 * Sets the duplex property for publications created using TextHandlers created with this builder.
		 * @param value the duplex value
		 * @return returns this object
		 */
		public Builder duplex(boolean value) {
			duplex = value; return this;
		}
		/**
		 * Sets the date for publications created using TextHandlers created with this builder.
		 * @param value the date to use
		 * @return returns this object
		 */
		public Builder date(Date value) {
			if (value==null) throw new IllegalArgumentException("Null value not accepted.");
			date = value; return this;
		}
		/**
		 * Builds a TextParser using the settings of this Builder
		 * @return returns a new TextParser
		 * @throws IOException if an error occurs
		 * @throws InputDetectionException if an error occurs 
		 * @throws UnsupportedEncodingException if an error occurs
		 */
		public TextHandler build() throws IOException, InputDetectionException {
			return new TextHandler(this);
		}
		
		/**
		 * Parses the input file with the current settings.
		 * @throws InputDetectionException if an error occurs
		 * @throws IOException if an error occurs
		 */
		public void parse() throws InputDetectionException, IOException {
			new TextHandler(this).parse();
		}
	}

	private TextHandler(Builder builder) throws IOException, InputDetectionException {
		input = builder.input;
		output = builder.output;
		factory = builder.factory;
		title = builder.title;
		author = builder.author;
		language = builder.language;
		identifier = builder.identifier;
		if (builder.converterId==null) {
			List<Table> tableCandiates;
			FileInputStream is = new FileInputStream(input);
			TextInputDetector tid = new TextInputDetector(factory);
			tableCandiates = tid.detect(is);
			if (tableCandiates==null || tableCandiates.size()<1) {
				throw new InputDetectionException("Cannot detect table.");
			}
			if (tableCandiates.size()>1) {
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				for (Table t : tableCandiates) {
					if (!first) {
						sb.append(", ");
					} else {
						first = false;
					}
					sb.append("'" + t.getDisplayName() + "'");
				}
				throw new InputDetectionException("Cannot choose a table automatically. Possible matches: " + sb.toString());
			}
			System.out.println("Using " + tableCandiates.get(0).getDisplayName());
			converter = tableCandiates.get(0).newBrailleConverter();
		} else {
			converter = factory.newTable(builder.converterId).newBrailleConverter();			
		}
		duplex = builder.duplex;
		date = builder.date;
	}
	
	/**
	 * Creates a new builder with the supplied parameters.
	 * @param input the input file
	 * @param output the output file
	 * @param factory the table catalog
	 * @return returns a new builder
	 */
	public static Builder with(File input, File output, TableCatalogService factory) {
		return new TextHandler.Builder(input, output, factory);
	}

	/**
	 * Parse using current settings
	 * @throws IOException if an I/O error occurs
	 */
	public void parse() throws IOException {
		if (date==null) {
			date = new Date();
		}
		FileInputStream is = new FileInputStream(input);
		PrintWriter pw = new PrintWriter(output, "utf-8");
		LineNumberReader lr = new LineNumberReader(new InputStreamReader(is, converter.getPreferredCharset()));
		// determine max rows/page and chars/row

		read(lr, null);

		// reset input
		is = new FileInputStream(input);
		lr = new LineNumberReader(new InputStreamReader(is, converter.getPreferredCharset()));

		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw.println("<pef version=\"2008-1\" xmlns=\"http://www.daisy.org/ns/2008/pef\">");
		pw.println("	<head>");
		pw.println("		<meta xmlns:dc=\"http://purl.org/dc/elements/1.1/\">");
		if (!"".equals(title)) {
			pw.println("			<dc:title>"+title+"</dc:title>");
		}
		if (!"".equals(author)) {
			pw.println("			<dc:creator>"+author+"</dc:creator>");
		}
		if (!"".equals(language)) {
			pw.println("			<dc:language>"+language+"</dc:language>");
		}
		pw.println("			<dc:date>"+DATE_FORMAT.format(date)+"</dc:date>");
		pw.println("			<dc:format>application/x-pef+xml</dc:format>");
		if (!"".equals(identifier)) {
			pw.println("			<dc:identifier>"+identifier+"</dc:identifier>");
		}
		pw.println("		</meta>");
		pw.println("	</head>");
		pw.println("	<body>");
		pw.println("		<volume cols=\""+maxCols+"\" rows=\""+maxRows+"\" rowgap=\"0\" duplex=\""+duplex+"\">");
		pw.println("			<section>");

		read(lr, pw);
		pw.println("			</section>");
		pw.println("		</volume>");
		pw.println("	</body>");
		pw.println("</pef>");
		pw.flush();
		pw.close();
	}

	private void read(LineNumberReader lr, PrintWriter pw) throws IOException {
		maxRows=0;
		maxCols=0;
		int cRows=0;
		boolean pageClosed = true;
		int eofIndex = -1;
		cRows++;
		String line = lr.readLine();
		while (line!=null) {
			eofIndex = line.indexOf(0x1A);
			if (eofIndex>-1) {
				line = line.substring(0, eofIndex); //remove trailing characters beyond eof-mark (CTRL+Z)
			}
			if ("\f".equals(line)) { // if line consists of a single form feed character. Just close the page (don't add rows yet).
				// if page is already closed, this is an empty page
				if (pageClosed) {
					if (pw!=null) { pw.println("				<page>"); }
					pageClosed=false;
				}
				if (pw!=null) { pw.println("				</page>"); }
				pageClosed=true;
				cRows--; // don't count this row
				if (cRows>maxRows) { maxRows=cRows;	}
				cRows=0;
			} else {
				String[] pieces = line.split("\\f", -1); //split on form feed
				int i = 1;
				for (String p : pieces) {
					if (i>1) { // there were form feeds
						if (pw!=null) {	pw.println("				</page>");	}
						pageClosed=true;
						cRows--; // don't count this row
						if (cRows>maxRows) { maxRows=cRows;	}
						cRows=0;
					}
					if (pageClosed) {
						if (pw!=null) { pw.println("				<page>"); }
						pageClosed=false;
					}
					if (p.length()>maxCols) {
						maxCols=p.length();
					}
					// don't output if row contains form feeds and this segment equals ""
					if (!(pieces.length>1 && (i==pieces.length || i==1) && "".equals(p))) {
						if (pw!=null) {
							pw.print("					<row>");
							pw.print(converter.toBraille(p));
							pw.println("</row>");
						}
					}
					i++;
				}
			}
			if (eofIndex>-1) {
				// End of file reached. Stop reading.
				line = null;
			} else {
				line = lr.readLine();
				cRows++;
			}
		}
		lr.close();
		if (!pageClosed) {
			if (pw!=null) { pw.println("				</page>"); }
			pageClosed=true;	
			cRows--; // don't count this row
			if (cRows>maxRows) { maxRows=cRows;	}
			cRows=0;
		}
	}

}
