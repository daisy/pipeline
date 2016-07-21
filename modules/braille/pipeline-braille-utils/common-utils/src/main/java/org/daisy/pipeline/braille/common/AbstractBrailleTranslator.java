package org.daisy.pipeline.braille.common;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import static com.google.common.collect.Iterators.concat;
import static com.google.common.collect.Iterators.peekingIterator;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.charactersOf;
import com.google.common.collect.PeekingIterator;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermInteger;

import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.dotify.api.translator.UnsupportedMetricException;

import org.slf4j.Logger;

public abstract class AbstractBrailleTranslator extends AbstractTransform implements BrailleTranslator {
	
	public FromStyledTextToBraille fromStyledTextToBraille() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public LineBreakingFromStyledText lineBreakingFromStyledText() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		public static abstract class DefaultLineBreaker implements LineBreakingFromStyledText {
			
			private final char blankChar;
			
			// FIXME: determine hyphenChar from CSS (hyphenate-character property)
			private final char hyphenChar;
			private final Logger logger;
			
			public DefaultLineBreaker() {
				this(null);
			}
			
			public DefaultLineBreaker(Logger logger) {
				this('\u2800', '\u2824', logger);
			}
			
			public DefaultLineBreaker(char blankChar, char hyphenChar, Logger logger) {
				this.blankChar = blankChar;
				this.hyphenChar = hyphenChar;
				this.logger = logger;
			}
			
			/**
			 * This method MUST translate to braille (the result may contain only <a
			 * href="http://snaekobbi.github.io/braille-css-spec/master/index.html#dfn-braille-character">braille
			 * characters</a> and <a
			 * href="http://snaekobbi.github.io/braille-css-spec/master/index.html#dfn-white-space-characters">white
			 * space</a>) and perform line breaking within words (hyphenate). It MAY also collapse
			 * white space and perform line breaking outside or at the boundaries of words
			 * (according to the CSS rules), but it doesn't need to because it will be followed by
			 * white space processing and line breaking anyway. Preserved spaces MUST be converted
			 * to NBSP characters. Preserved line breaks MAY be converted to LS characters. Line
			 * breaking can be achieved either directly by indicating that the end of a line has
			 * been reached (by returning an empty string for a `limit` greater than 0), or
			 * indirectly by including break characters (SHY, ZWSP, LS) in the result. Hyphen
			 * characters MAY be inserted but that may also be left up to the additional line
			 * breaking (by including SHY characters).
			 */
			protected abstract BrailleStream translateAndHyphenate(Iterable<CSSStyledText> text);
			
			protected interface BrailleStream {
				public boolean hasNext();
				public String next(int limit, boolean force);
				public Character peek();
				public String remainder();
				public void mark();
				public void reset();
			}
			
			public LineIterator transform(final Iterable<CSSStyledText> text) {
				
				// FIXME: determine wordSpacing of individual segments
				int wordSpacing; {
					wordSpacing = -1;
					for (CSSStyledText st : text) {
						SimpleInlineStyle style = st.getStyle();
						int spacing = 1;
						if (style != null) {
							CSSProperty val = style.getProperty("word-spacing");
							if (val != null) {
								if (val == WordSpacing.length) {
									spacing = style.getValue(TermInteger.class, "word-spacing").getIntValue();
									if (spacing < 0) {
										if (logger != null)
											logger.warn("word-spacing: {} not supported, must be non-negative", val);
										spacing = 1; }}
									
								// FIXME: assuming style is mutable and text.iterator() does not create copies
								style.removeProperty("word-spacing"); }}
						if (wordSpacing < 0)
							wordSpacing = spacing;
						else if (wordSpacing != spacing)
							throw new RuntimeException("word-spacing must be constant, but both "
							                           + wordSpacing + " and " + spacing + " specified"); }
					if (wordSpacing < 0) wordSpacing = 1; }
				return new LineIterator(translateAndHyphenate(text), blankChar, hyphenChar, wordSpacing);
			}
			
			protected static class FullyHyphenatedAndTranslatedString implements BrailleStream {
				private String next;
				private String mark;
				public FullyHyphenatedAndTranslatedString(String string) {
					next = mark = string;
				}
				public boolean hasNext() {
					return (next != null);
				}
				public String next(int limit, boolean force) {
					if (next == null)
						throw new NoSuchElementException();
					else {
						String n = next;
						next = null;
						return n; }
				}
				public Character peek() {
					if (next == null)
						throw new NoSuchElementException();
					else if (next.isEmpty())
						return null;
					else
						return next.charAt(0);
				}
				public String remainder() {
					if (next == null)
						throw new NoSuchElementException();
					else
						return next;
				}
				public void mark() {
					mark = next;
				}
				public void reset() {
					next = mark;
				}
			}
			
			public static class LineIterator implements BrailleTranslator.LineIterator {
				
				private final char blankChar;
				private final char hyphenChar;
				private final int wordSpacing;
				
				public LineIterator(String fullyHyphenatedAndTranslatedString, char blankChar, char hyphenChar, int wordSpacing) {
					this(new FullyHyphenatedAndTranslatedString(fullyHyphenatedAndTranslatedString), blankChar, hyphenChar, wordSpacing);
				}
				
				public LineIterator(BrailleStream inputStream, char blankChar, char hyphenChar, int wordSpacing) {
					this.inputStream = inputStream;
					this.blankChar = blankChar;
					this.hyphenChar = hyphenChar;
					this.wordSpacing = wordSpacing;
				}
				
				private BrailleStream inputStream;
				private PeekingIterator<Character> inputBuffer = null;
				private StringBuilder charBuffer = new StringBuilder();
				
				/**
				 * Array with soft wrap opportunity info
				 * - SPACE, LF, CR, TAB and ZWSP create normal soft wrap opportunities
				 * - SHY create soft wrap opportunities that insert a hyphen glyph
				 * - normal soft wrap opportunities override soft wrap opportunities that insert a hyphen glyph
				 *
				 * @see <a href="http://snaekobbi.github.io/braille-css-spec/#h3_line-breaking">Braille CSS – § 9.4 Line Breaking</a>
				 */
				private ArrayList<Byte> wrapInfo = new ArrayList<Byte>();
				private final static byte NO_SOFT_WRAP = (byte)0x0;
				private final static byte SOFT_WRAP_WITH_HYPHEN = (byte)0x1;
				private final static byte SOFT_WRAP_WITHOUT_HYPHEN = (byte)0x3;
				private final static byte SOFT_WRAP_AFTER_SPACE = (byte)0x7;
				private final static byte HARD_WRAP = (byte)0x15;
				
				private final static char SHY = '\u00ad';
				private final static char ZWSP = '\u200b';
				private final static char SPACE = ' ';
				private final static char CR = '\r';
				private final static char LF = '\n';
				private final static char TAB = '\t';
				private final static char NBSP = '\u00a0';
				private final static char BLANK = '\u2800';
				private final static char LS = '\u2028';
				
				private boolean lastCharIsSpace = false;
				
				/**
				 * Fill the character (charBuffer) and soft wrap opportunity (wrapInfo) buffers while normalising and collapsing spaces
				 * - until the buffers are at least 'limit' long
				 * - or until the current row is full according to the input feed (BrailleStream)
				 * - and while the remaining input starts with SPACE, LF, CR, TAB, NBSP or BRAILLE PATTERN BLANK
				 */
				private void fillRow(int limit, boolean force) {
					int bufSize = charBuffer.length();
				  loop: while (true) {
						if (inputBuffer == null || !inputBuffer.hasNext()) {
							if (limit != 0 && bufSize >= limit)
								return;
							else if (inputStream.hasNext()) {
								String next = limit == 0 ? inputStream.remainder() : inputStream.next(limit - bufSize, force && (bufSize == 0));
								if (next.isEmpty()) // row full according to input feed
									return;
								else
									inputBuffer = peekingIterator(charactersOf(next).iterator()); }
							else
								return; }
						char next = inputBuffer.peek();
						switch (next) {
						case SHY:
							if (bufSize > 0)
								wrapInfo.set(bufSize - 1, (byte)(wrapInfo.get(bufSize - 1) | SOFT_WRAP_WITH_HYPHEN));
							lastCharIsSpace = false;
							break;
						case ZWSP:
							if (bufSize > 0)
								wrapInfo.set(bufSize - 1, (byte)(wrapInfo.get(bufSize - 1) | SOFT_WRAP_WITHOUT_HYPHEN));
							lastCharIsSpace = false;
							break;
						case SPACE:
						case LF:
						case CR:
						case TAB:
						case BLANK:
							if (lastCharIsSpace)
								break;
							if (bufSize > 0)
								wrapInfo.set(bufSize - 1, (byte)(wrapInfo.get(bufSize - 1) | SOFT_WRAP_WITHOUT_HYPHEN));
							for (int i = 0; i < wordSpacing; i++) {
								charBuffer.append(blankChar);
								bufSize ++;
								wrapInfo.add(SOFT_WRAP_AFTER_SPACE);
							}
							lastCharIsSpace = true;
							break;
						case NBSP:
							charBuffer.append(blankChar);
							bufSize ++;
							wrapInfo.add(NO_SOFT_WRAP);
							lastCharIsSpace = false;
							break;
						case LS:
							if (bufSize > 0)
								wrapInfo.set(bufSize - 1, (byte)(wrapInfo.get(bufSize - 1) | HARD_WRAP));
							lastCharIsSpace = true;
							break;
						default:
							if (bufSize >= limit) break loop;
							charBuffer.append(next);
							bufSize ++;
							wrapInfo.add(NO_SOFT_WRAP);
							lastCharIsSpace = false; }
						inputBuffer.next(); }
				}
				
				/**
				 * Flush the first 'size' elements of the character and soft wrap opportunity buffers
				 * Assumes that 'size &lt;= charBuffer.length()'
				 */
				private void flushBuffers(int size) {
					charBuffer = new StringBuilder(charBuffer.substring(size));
					wrapInfo = new ArrayList<Byte>(wrapInfo.subList(size, wrapInfo.size()));
				}
				
				public String nextTranslatedRow(int limit, boolean force) {
					fillRow(limit, force);
					int bufSize = charBuffer.length();
					
					// always break at preserved line breaks
					for (int i = 0; i < bufSize; i++) {
						if (wrapInfo.get(i) == HARD_WRAP) {
							String rv = charBuffer.substring(0, i + 1);
							flushBuffers(i + 1);
							return rv; }}
					
					// no need to break if remaining text is shorter than line
					if (bufSize < limit) {
						String rv = charBuffer.toString();
						charBuffer.setLength(0);
						wrapInfo.clear();
						return rv; }
					
					if (bufSize == limit) {
						Character next = (inputBuffer != null && inputBuffer.hasNext()) ? inputBuffer.peek()
							: inputStream.hasNext() ? inputStream.peek() : null;
						boolean wrapWithoutHyphen; {
							wrapWithoutHyphen = false;
							if (next == null)
								wrapWithoutHyphen = true;
							else
								switch (next) {
								case ZWSP:
								case SPACE:
								case LF:
								case CR:
								case TAB:
								case BLANK:
								case LS:
									wrapWithoutHyphen = true; }}
						if (wrapWithoutHyphen) {
							String rv = charBuffer.toString();
							charBuffer.setLength(0);
							wrapInfo.clear();
							return rv; }}
					
					// return nothing if limit is 0
					if (limit == 0) {
						
						// strip leading SPACE in remaining text
						while (limit < bufSize && wrapInfo.get(limit) == SOFT_WRAP_AFTER_SPACE) limit++;
						flushBuffers(limit);
						return ""; }
					
					// break at SPACE or ZWSP
					if ((wrapInfo.get(limit - 1) & SOFT_WRAP_WITHOUT_HYPHEN) == SOFT_WRAP_WITHOUT_HYPHEN) {
						String rv = charBuffer.substring(0, limit);
						
						// strip leading SPACE in remaining text
						while (limit < bufSize && wrapInfo.get(limit) == SOFT_WRAP_AFTER_SPACE) limit++;
						flushBuffers(limit);
						return rv; }
					
					// try to break later if the overflowing characters are blank
					for (int i = limit + 1; i - 1 < bufSize && charBuffer.charAt(i - 1) == blankChar; i++)
						if ((wrapInfo.get(i - 1) & SOFT_WRAP_WITHOUT_HYPHEN) == SOFT_WRAP_WITHOUT_HYPHEN) {
							String rv = charBuffer.substring(0, limit);
							flushBuffers(i);
							return rv; }
					
					// try to break sooner
					for (int i = limit - 1; i > 0; i--) {
						
						// break at SPACE, ZWSP or SHY
						if (wrapInfo.get(i - 1) > 0) {
							String rv = charBuffer.substring(0, i);
							
							// insert hyphen glyph at SHY
							if (wrapInfo.get(i - 1) == 0x1)
								rv += hyphenChar;
							
							// strip leading SPACE in remaining text
							// NB: breaks cases where space after SHY is not from letter-spacing
							while(i < bufSize && charBuffer.charAt(i) == blankChar) i++;
							flushBuffers(i);
							return rv; }}
					
					// force hard break
					if (force) {
						String rv = charBuffer.substring(0, limit);
						flushBuffers(limit);
						return rv; }
					
					return "";
				}
				
				// FIXME: there must be a better way to do this!!!
				public boolean hasNext() {
					if (charBuffer.length() > 0)
						return true;
					inputStream.mark();
					List<Character> save_inputBuffer = inputBuffer != null ? copyOf(inputBuffer) : null;
					inputBuffer = save_inputBuffer != null ? peekingIterator(save_inputBuffer.iterator()) : null;
					StringBuilder save_charBuffer = charBuffer;
					charBuffer = new StringBuilder();
					ArrayList<Byte> save_wrapInfo = wrapInfo;
					wrapInfo = new ArrayList<Byte>();
					boolean save_lastCharIsSpace = lastCharIsSpace;
					lastCharIsSpace = false;
					fillRow(3, true);
					boolean hasNext = charBuffer.length() > 0;
					inputStream.reset();
					inputBuffer = save_inputBuffer != null ? peekingIterator(save_inputBuffer.iterator()) : null;
					charBuffer = save_charBuffer;
					wrapInfo = save_wrapInfo;
					lastCharIsSpace = save_lastCharIsSpace;
					return hasNext;
				}
				
				public String getTranslatedRemainder() {
					BrailleStream save_inputStream = inputStream;
					inputStream = emptyBrailleStream;
					List<Character> save_inputBuffer = inputBuffer != null ? copyOf(inputBuffer) : null;
					inputBuffer = save_inputBuffer != null ?
						peekingIterator(concat(save_inputBuffer.iterator(), charactersOf(save_inputStream.remainder()).iterator())) :
						peekingIterator(charactersOf(save_inputStream.remainder()).iterator());
					StringBuilder save_charBuffer = charBuffer;
					charBuffer = new StringBuilder(charBuffer.toString());
					ArrayList<Byte> save_wrapInfo = wrapInfo;
					wrapInfo = new ArrayList<Byte>(wrapInfo);
					boolean save_lastCharIsSpace = lastCharIsSpace;
					lastCharIsSpace = false;
					fillRow(Integer.MAX_VALUE, true);
					String remainder = charBuffer.toString();
					inputStream = save_inputStream;
					inputBuffer = save_inputBuffer != null ? peekingIterator(save_inputBuffer.iterator()) : null;
					charBuffer = save_charBuffer;
					wrapInfo = save_wrapInfo;
					lastCharIsSpace = save_lastCharIsSpace;
					return remainder;
				}
				
				public int countRemaining() {
					return getTranslatedRemainder().length();
				}
				
				public boolean supportsMetric(String metric) {
					return false;
				}
				
				public double getMetric(String metric) {
					throw new UnsupportedMetricException("Metric not supported: " + metric);
				}
				
				private static final BrailleStream emptyBrailleStream = new BrailleStream() {
					public boolean hasNext() { return false; }
					public String next(int limit, boolean force) { throw new NoSuchElementException(); }
					public Character peek() { throw new NoSuchElementException(); }
					public String remainder() { throw new NoSuchElementException(); }
					public void mark() {}
					public void reset() {}
				};
			}
		}
	}
}
