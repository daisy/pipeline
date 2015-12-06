package  org.daisy.dotify.common.text;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Provides a simple tool to read text files with multiple fields on each row,
 * such as csv-files.
 * @author Joel Håkansson
 *
 */
public class TextFileReader implements Closeable {
	private final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	private final static String DEFAULT_EXPRESSION = ",\\s*";
	private final static int DEFAULT_LIMIT = 0;
	private LineNumberReader lnr;
	private Pattern pattern;
	private int limit;
	private int currentLine;
	
	public static class Builder {
		private final InputStream is;
		private Charset cs = DEFAULT_CHARSET;
		private String regex = DEFAULT_EXPRESSION;
		private int limit = DEFAULT_LIMIT;
		
		public Builder(InputStream value) {
			this.is = value;
		}
		
		public Builder charset(Charset value) {
			this.cs = value;
			return this;
		}
		
		/**
		 * Sets the regular expression of this builder.
		 * @param value the expression to use
		 * @return returns this builder
		 * @throws PatternSyntaxException If the expression's syntax is invalid
		 */
		public Builder regex(String value) {
			//validate expression by compiling
			Pattern.compile(value);
			this.regex = value;
			return this;
		}
		
		public Builder limit(int value) {
			this.limit = value;
			return this;
		}
		
		public TextFileReader build() {
			return new TextFileReader(this);
		}
	}
	
	/**
	 * Creates a new TextFileReader with the default encoding and field separator.
	 * @param is the input stream to read.
	 */
	public TextFileReader(InputStream is) {
		this(is, DEFAULT_CHARSET);
	}
	
	/**
	 * Creates a new TextFileReader with the default field separator.
	 * @param is the input stream to read.
	 * @param cs the charset to use
	 */
	public TextFileReader(InputStream is, Charset cs) {
		this(is, cs, DEFAULT_EXPRESSION, DEFAULT_LIMIT);
	}
	
	/**
	 * 
	 * @param is
	 * @param cs
	 * @param regex
	 * @param limit
	 */
	public TextFileReader(InputStream is, Charset cs, String regex, int limit) {
		if (is==null) {
			throw new NullPointerException();
		}
		this.lnr = new LineNumberReader(new InputStreamReader(is, cs));
		this.pattern = Pattern.compile(regex);
		this.limit = limit;
		this.currentLine = 0;
	}
	
	private TextFileReader(Builder builder) {
		this(builder.is, builder.cs, builder.regex, builder.limit);
	}

	/**
	 * Gets the next line in the stream.
	 * @return returns next line, or null if there are no more lines
	 * @throws IOException
	 */
	public LineData nextLine() throws IOException {
		String line = lnr.readLine();
		currentLine++;
		while (line != null) {
			line = line.trim();
			if (line.startsWith("#") || line.length() == 0) {
				line = lnr.readLine();
				currentLine++;
			} else {
				//line.split(regex, limit)				
				return new LineData(line, pattern.split(line, limit), currentLine);
			}
		}
		return null;
	}
	
	@Override
	public void close() throws IOException {
		lnr.close();
	}
	
	public class LineData {
		private final String line;
		private final String[] fields;
		private final int lineNumber;
		
		private LineData(String line, String[] fields, int lineNumber) {
			this.line = line;
			this.fields = fields;
			this.lineNumber = lineNumber;
		}

		/**
		 * Gets the line as it was read.
		 * @return returns the entire line as a string.
		 */
		public String getLine() {
			return line;
		}

		/**
		 * Gets the fields
		 * @return returns the fields
		 */
		public String[] getFields() {
			return fields;
		}

		/**
		 * Gets the line number where the data was read.
		 * @return returns the line number
		 */
		public int getLineNumber() {
			return lineNumber;
		}

	}

}
