package org.daisy.pipeline.braille.common;

import static java.lang.Math.min;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import static com.google.common.collect.Iterators.concat;
import static com.google.common.collect.Iterators.peekingIterator;
import com.google.common.collect.ImmutableList;
import static com.google.common.collect.Lists.charactersOf;
import com.google.common.collect.PeekingIterator;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermInteger;

import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.dotify.api.translator.UnsupportedMetricException;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;

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
			 * href="http://braillespecs.github.io/braille-css/master/index.html#dfn-braille-character">braille
			 * characters</a> and <a
			 * href="http://braillespecs.github.io/braille-css/master/index.html#dfn-white-space-characters">white
			 * space</a>) and perform line breaking within words (hyphenate). It MAY also collapse
			 * white space and perform line breaking outside or at the boundaries of words
			 * (according to the CSS rules), but it doesn't need to because the result will be
			 * passed though a white space processing and line breaking stage anyway. Preserved
			 * spaces MUST be converted to NBSP characters. Line breaking may be achieved "directly"
			 * by indicating that the end of a line has been reached, by returning an empty string
			 * when the next() method is called again (with a `limit` &gt; 0). The "allowHyphens"
			 * argument must be respected in this case. A hyphen character at the end the line MUST
			 * be inserted when hyphenating. If it's a soft hyphen (SHY) it will be substituted with
			 * a real hyphen automatically. If line breaking is not done directly, it is left up to
			 * the line breaking stage. Preserved line breaks MUST be converted to LS characters in
			 * this case, and other break characters (SHY, ZWSP) MUST be included for all break
			 * opportunities, including those within words, regardless of the "allowHyphens"
			 * argument.
			 */
			protected abstract BrailleStream translateAndHyphenate(Iterable<CSSStyledText> text);
			
			protected interface BrailleStream extends Cloneable {
				public boolean hasNext();
				public String next(int limit, boolean force, boolean allowHyphens);
				public Character peek();
				public String remainder();
				public Object clone();
			}
			
			public LineIterator transform(final Iterable<CSSStyledText> text) throws TransformationException {
				
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
							throw new TransformationException("word-spacing must be constant, but both "
							                                  + wordSpacing + " and " + spacing + " specified"); }
					if (wordSpacing < 0) wordSpacing = 1; }
				return new LineIterator(translateAndHyphenate(text), blankChar, hyphenChar, wordSpacing);
			}
			
			protected static class FullyHyphenatedAndTranslatedString implements BrailleStream {
				private String next;
				public FullyHyphenatedAndTranslatedString(String string) {
					next = string;
				}
				public boolean hasNext() {
					return (next != null && !next.isEmpty());
				}
				public String next(int limit, boolean force, boolean allowHyphens) {
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
				@Override
				public Object clone() {
					try {
						return super.clone();
					} catch (CloneNotSupportedException e) {
						throw new InternalError("coding error");
					}
				}
			}
			
			public static class LineIterator implements BrailleTranslator.LineIterator, Cloneable {
				
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
				 * @see <a href="http://braillespecs.github.io/braille-css/#h3_line-breaking">Braille CSS – § 9.4 Line Breaking</a>
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
				 * - and while the remaining input starts with SPACE, LF, CR, TAB, NBSP, BRAILLE PATTERN BLANK, SHY, ZWSP or LS
				 */
				private void fillRow(int limit, boolean force, boolean allowHyphens) {
					int bufSize = charBuffer.length();
				  loop: while (true) {
						if (inputBuffer == null || !inputBuffer.hasNext()) {
							inputBuffer = null;
							if (!inputStream.hasNext()) {} // end of stream
							else if (bufSize < limit) {
								String next = inputStream.next(limit - bufSize, force && (bufSize == 0), allowHyphens);
								if (next.isEmpty()) {} // row full according to input feed
								else
									inputBuffer = peekingIterator(charactersOf(next).iterator()); }
							if (inputBuffer == null) {
								if (!inputStream.hasNext()) { // end of stream
									if (bufSize > 0)
										wrapInfo.set(bufSize - 1, (byte)(wrapInfo.get(bufSize - 1) | SOFT_WRAP_WITHOUT_HYPHEN));
									return; }
								switch (inputStream.peek()) {
								case SHY:    case TAB:
								case ZWSP:   case BLANK:
								case SPACE:  case NBSP:
								case LF:     case LS:
								case CR:
									String next = inputStream.next(1, true, allowHyphens);
									if (next.isEmpty())
										throw new RuntimeException("coding error");
									inputBuffer = peekingIterator(charactersOf(next).iterator());
									break;
								default:
									return; }}}
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
				
				/**
				 * @param limit specifies the maximum number of characters allowed in the result
				 * @param force specifies if the translator should force a break at the limit
				 *              if no natural break point is found
				 * @param wholeWordsOnly specifies that the row may not end on a break point inside a word.
				 * @return returns the translated string preceding the row break, including a translated hyphen
				 *                 at the end if needed.
				 */
				public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
					fillRow(limit, force, !wholeWordsOnly);
					int bufSize = charBuffer.length();
					
					// charBuffer may be empty (even if hasNext() was true)
					if (bufSize == 0)
						return "";
					
					// always break at preserved line breaks
					for (int i = 0; i < min(bufSize, limit); i++) {
						if (wrapInfo.get(i) == HARD_WRAP) {
							int cut = i + 1;
							
							// strip trailing SPACE/LF/CR/TAB/BLANK/NBSP (all except NBSP are already collapsed into one)
							while (cut > 0 && charBuffer.charAt(cut - 1) == blankChar) cut--;
							
							// preserve if at beginning of stream
							if (cut == 0)
								cut = i + 1;
							String rv = charBuffer.substring(0, cut);
							
							// strip leading SPACE/LF/CR/TAB/BLANK in remaining text (are already collapsed into one)
							cut = i + 1;
							while (cut < bufSize && wrapInfo.get(cut) == SOFT_WRAP_AFTER_SPACE) cut++;
							flushBuffers(cut);
							return rv; }}
					
					// no need to break if remaining text is shorter than line
					if (bufSize < limit) {
						String rv = charBuffer.substring(0, bufSize);
							
						// replace soft hyphen with real hyphen
						if (wrapInfo.get(rv.length() - 1) == SOFT_WRAP_WITH_HYPHEN)
							rv += hyphenChar;
						charBuffer.setLength(0);
						wrapInfo.clear();
						
						// strip trailing SPACE/LF/CR/TAB/BLANK/NBSP (all except NBSP are already collapsed into one)
						int cut = bufSize;
						while (cut > 0 && rv.charAt(cut - 1) == blankChar) cut--;
						
						// preserve if at beginning of stream or end of stream
						if (cut > 0 && cut < bufSize && hasNext())
							rv = rv.substring(0, cut);
						return rv; }
					
					// return nothing if limit is 0 (limit should not be less than 0, but check anyway)
					if (limit <= 0) {
						
						// strip leading SPACE/LF/CR/TAB/BLANK in remaining text (are already collapsed into one)
						int cut = 0;
						while (cut < bufSize && wrapInfo.get(cut) == SOFT_WRAP_AFTER_SPACE) cut++;
						flushBuffers(cut);
						return ""; }
					
					// break at SPACE or ZWSP
					// FIXME: ignore ZWSP if it comes from hyphenation (how to find out?) and wholeWordsOnly==true
					if ((wrapInfo.get(limit - 1) & SOFT_WRAP_WITHOUT_HYPHEN) == SOFT_WRAP_WITHOUT_HYPHEN) {
						int cut = limit;
						
						// strip trailing SPACE/LF/CR/TAB/BLANK/NBSP (all except NBSP are already collapsed into one)
						while (cut > 0 && charBuffer.charAt(cut - 1) == blankChar) cut--;
						
						// strip leading SPACE/LF/CR/TAB/BLANK in remaining text (are already collapsed into one)
						int cut2 = limit;
						while (cut2 < bufSize && wrapInfo.get(cut2) == SOFT_WRAP_AFTER_SPACE) cut2++;
						String rv = charBuffer.substring(0, cut2);
						flushBuffers(cut2);
						
						// preserve if at beginning of stream or end of stream and not overflowing
						if (cut > 0 && cut < cut2 && hasNext())
							rv = rv.substring(0, cut);
						else if (cut2 > limit)
							rv = rv.substring(0, limit);
						return rv; }
					
					// try to break later if the overflowing characters are blank
					for (int i = limit + 1; i - 1 < bufSize && charBuffer.charAt(i - 1) == blankChar; i++)
						if ((wrapInfo.get(i - 1) & SOFT_WRAP_WITHOUT_HYPHEN) == SOFT_WRAP_WITHOUT_HYPHEN) {
							
							// strip trailing SPACE/LF/CR/TAB/BLANK/NBSP (all except NBSP are already collapsed into one)
							int cut = limit;
							while (cut > 0 && charBuffer.charAt(cut - 1) == blankChar) cut--;
							
							// strip leading SPACE/LF/CR/TAB/BLANK in remaining text (are already collapsed into one)
							int cut2 = i;
							while (cut2 < bufSize && wrapInfo.get(cut2) == SOFT_WRAP_AFTER_SPACE) cut2++;
							String rv = charBuffer.substring(0, cut2);
							flushBuffers(cut2);
							
							// preserve if at end of stream and not overflowing
							if (cut < cut2 && hasNext())
								rv = rv.substring(0, cut);
							else if (cut2 > limit)
								rv = rv.substring(0, limit);
							return rv; }
					
					// try to break sooner
					for (int i = limit - 1; i > 0; i--) {
						
						// break at SPACE, ZWSP or SHY
						// FIXME: ignore ZWSP if it comes from hyphenation (how to find out?) and wholeWordsOnly==true
						if ((wrapInfo.get(i - 1) & SOFT_WRAP_WITHOUT_HYPHEN) == SOFT_WRAP_WITHOUT_HYPHEN
						    || (!wholeWordsOnly && (wrapInfo.get(i - 1) == SOFT_WRAP_WITH_HYPHEN))) {
							int cut = i;
							String rv;
							
							// insert hyphen glyph at SHY
							if (wrapInfo.get(cut - 1) == 0x1) {
								rv = charBuffer.substring(0, cut);
								rv += hyphenChar; }
							else {
								
								// strip trailing SPACE/LF/CR/TAB/BLANK/NBSP (all except NBSP are already collapsed into one)
								while (cut > 0 && charBuffer.charAt(cut - 1) == blankChar) cut--;
								
								// preserve if at beginning of stream
								if (cut == 0)
									cut = i;
								rv = charBuffer.substring(0, cut); }
							
							// strip leading SPACE/LF/CR/TAB/BLANK in remaining text (are already collapsed into one)
							// FIXME: breaks cases where space after SHY is not from letter-spacing
							cut = i;
							while(cut < bufSize && charBuffer.charAt(cut) == blankChar) cut++;
							flushBuffers(cut);
							return rv; }}
					
					// force hard break
					if (force) {
						String rv = charBuffer.substring(0, limit);
						flushBuffers(limit);
						return rv; }
					
					return "";
				}
				
				/**
				 * @return returns true if there are characters not yet extracted with nextTranslatedRow
				 */
				public boolean hasNext() {
					return charBuffer.length() > 0
						|| (inputBuffer != null && inputBuffer.hasNext())
						|| inputStream.hasNext();
				}
				
				/**
				 * @return returns the characters not yet extracted with nextTranslatedRow
				 */
				public String getTranslatedRemainder() {
					
					// Note that I could use clone() here, but the current code is slightly more efficient
					BrailleStream save_inputStream = inputStream;
					inputStream = emptyBrailleStream;
					List<Character> save_inputBuffer = inputBuffer != null ? ImmutableList.copyOf(inputBuffer) : null;
					if (save_inputBuffer != null) {
						if (save_inputStream.hasNext()) {
							inputBuffer = peekingIterator(
									concat(save_inputBuffer.iterator(), charactersOf(save_inputStream.remainder()).iterator()));
						} else {
							inputBuffer = peekingIterator(save_inputBuffer.iterator());
						}
					} else if (save_inputStream.hasNext()) {
						inputBuffer = peekingIterator(charactersOf(save_inputStream.remainder()).iterator());
					} else {
						inputBuffer = null;
					}
					StringBuilder save_charBuffer = charBuffer;
					charBuffer = new StringBuilder(charBuffer.toString());
					ArrayList<Byte> save_wrapInfo = wrapInfo;
					wrapInfo = new ArrayList<Byte>(wrapInfo);
					boolean save_lastCharIsSpace = lastCharIsSpace;
					lastCharIsSpace = false;
					fillRow(Integer.MAX_VALUE, true, false);
					String remainder = charBuffer.toString();
					inputStream = save_inputStream;
					inputBuffer = save_inputBuffer != null ? peekingIterator(save_inputBuffer.iterator()) : null;
					charBuffer = save_charBuffer;
					wrapInfo = save_wrapInfo;
					lastCharIsSpace = save_lastCharIsSpace;
					return remainder;
				}
				
				/**
				 * @return returns the number of characters in getTranslatedRemainder
				 */
				public int countRemaining() {
					return getTranslatedRemainder().length();
				}
				
				/**
				 * @return returns a copy of this result in the current state
				 */
				public BrailleTranslatorResult copy() {
					return (BrailleTranslatorResult)clone();
				}
				
				@Override
				// FIXME: could this be done more efficiently/lazily?
				public Object clone() {
					LineIterator clone; {
						try {
							clone = (LineIterator)super.clone();
						} catch (CloneNotSupportedException e) {
							throw new InternalError("coding error");
						}
					}
					clone.inputStream = (BrailleStream)inputStream.clone();
					if (inputBuffer != null) {
						List<Character> list = ImmutableList.copyOf(inputBuffer);
						inputBuffer = peekingIterator(list.iterator());
						clone.inputBuffer = peekingIterator(list.iterator());
					}
					clone.charBuffer = new StringBuilder(charBuffer.toString());
					clone.wrapInfo = new ArrayList<Byte>(wrapInfo);
					return clone;
				}
				
				public boolean supportsMetric(String metric) {
					return false;
				}
				
				public double getMetric(String metric) {
					throw new UnsupportedMetricException("Metric not supported: " + metric);
				}
				
				private static final BrailleStream emptyBrailleStream = new BrailleStream() {
					public boolean hasNext() { return false; }
					public String next(int limit, boolean force, boolean allowHyphens) { throw new NoSuchElementException(); }
					public Character peek() { throw new NoSuchElementException(); }
					public String remainder() { throw new NoSuchElementException(); }
					@Override
					public Object clone() { return this; }
				};
			}
		}
	}
}
