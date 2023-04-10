package org.daisy.pipeline.braille.common;

import static java.lang.Math.min;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Iterators.concat;
import static com.google.common.collect.Iterators.peekingIterator;
import com.google.common.collect.ImmutableList;
import static com.google.common.collect.Lists.charactersOf;
import com.google.common.collect.PeekingIterator;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSProperty.HyphenateCharacter;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.translator.UnsupportedMetricException;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Tuple2;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.slf4j.Logger;

public abstract class AbstractBrailleTranslator extends AbstractTransform implements BrailleTranslator {
	
	private final BrailleConverter brailleCharset;

	protected AbstractBrailleTranslator() {
		this(null);
	}

	/**
	 * @param brailleCharset Used by default implementation of {@link #lineBreakingFromStyledText()}
	 *                       to encode hyphen character when specified through "hyphenate-character"
	 *                       property.
	 */
	protected AbstractBrailleTranslator(BrailleConverter brailleCharset) {
		this.brailleCharset = brailleCharset;
	}

	public FromStyledTextToBraille fromStyledTextToBraille() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	private LineBreakingFromStyledText lineBreakingFromStyledText = null;
	
	public LineBreakingFromStyledText lineBreakingFromStyledText() throws UnsupportedOperationException {
		// default implementation based on fromStyledTextToBraille()
		if (lineBreakingFromStyledText == null) {
			final FromStyledTextToBraille fromStyledTextToBraille = fromStyledTextToBraille();
			Character blankChar = brailleCharset == null
				? '\u2800'
				: brailleCharset.toText("\u2800").toCharArray()[0];
			Character defaultHyphenChar = brailleCharset == null
				? '\u2824'
				: brailleCharset.toText("\u2824").toCharArray()[0];
			lineBreakingFromStyledText = new util.DefaultLineBreaker(blankChar, defaultHyphenChar, brailleCharset, null) {
					protected BrailleStream translateAndHyphenate(Iterable<CSSStyledText> styledText, int from, int to) {
						List<String> braille = new ArrayList<>();
						Iterator<CSSStyledText> style = styledText.iterator();
						for (String s : fromStyledTextToBraille.transform(styledText)) {
							SimpleInlineStyle st = style.next().getStyle();
							if (st != null) {
								if (st.getProperty("hyphens") == Hyphens.NONE) {
									s = s.replaceAll("[\u00AD\u200B]","");
									st.removeProperty("hyphens"); }
								CSSProperty ws = st.getProperty("white-space");
								if (ws != null) {
									if (ws == WhiteSpace.PRE_WRAP)
										s = s.replaceAll("[\\x20\t\\u2800]+", "$0\u200B")
											.replaceAll("[\\x20\t\\u2800]", "\u00A0");
									if (ws == WhiteSpace.PRE_WRAP || ws == WhiteSpace.PRE_LINE)
										s = s.replaceAll("[\\n\\r]", "\u2028");
									st.removeProperty("white-space"); }}
							braille.add(s);
						}
						StringBuilder brailleString = new StringBuilder();
						int fromChar = 0;
						int toChar = to >= 0 ? 0 : -1;
						for (String s : braille) {
							brailleString.append(s);
							if (--from == 0)
								fromChar = brailleString.length();
							if (--to == 0)
								toChar = brailleString.length();
						}
						return new FullyHyphenatedAndTranslatedString(brailleString.toString(), fromChar, toChar);
					}
				};
		}
		return lineBreakingFromStyledText;
	}
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		public static abstract class DefaultLineBreaker implements LineBreakingFromStyledText {
			
			private final static char SHY = '\u00ad';   // soft hyphen
			private final static char ZWSP = '\u200b';  // zero-width space
			private final static char SPACE = ' ';      // space
			private final static char CR = '\r';        // carriage return
			private final static char LF = '\n';        // line feed
			private final static char TAB = '\t';       // tab
			private final static char NBSP = '\u00a0';  // no-break space
			private final static char BLANK = '\u2800'; // blank braille pattern
			private final static char LS = '\u2028';    // line separator (preserved line breaks)
			private final static char RS = '\u001E';    // (for segmentation)
			
			private final char blankChar;
			
			private final char defaultHyphenChar;
			private final BrailleConverter brailleCharset;
			private final Logger logger;
			
			public DefaultLineBreaker() {
				this(null);
			}
			
			public DefaultLineBreaker(Logger logger) {
				this('\u2800', '\u2824', logger);
			}
			
			public DefaultLineBreaker(char blankChar, char defaultHyphenChar, Logger logger) {
				this(blankChar, defaultHyphenChar, null, logger);
			}

			/**
			 * @param blankChar         character to use as blank pattern. Must already be encoded in the correct
			 *                          braille charset.
			 * @param defaultHyphenChar hyphen character to use in case of "hyphenate-character: auto". Must
			 *                          already be encoded in the correct braille charset.
			 * @param brailleCharset    for encoding hyphen character when specified through "hyphenate-character"
			 *                          property.
			 */
			public DefaultLineBreaker(char blankChar, char defaultHyphenChar, BrailleConverter brailleCharset, Logger logger) {
				this.blankChar = blankChar;
				this.defaultHyphenChar = defaultHyphenChar;
				this.brailleCharset = brailleCharset;
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
			 * spaces MUST be converted to NBSP characters. Line breaking may be "explicit" by not
			 * providing more characters than those that may be put on the current line. The
			 * "allowHyphens" argument must be respected, and a hyphen character at the end the line
			 * MUST be inserted when hyphenating. If it's a soft hyphen (SHY) it will be substituted
			 * with a real hyphen automatically. If line breaking is "implicit", it is left up to
			 * the line breaking stage. Preserved line breaks MUST be converted to LS characters in
			 * this case, and other break characters (SHY, ZWSP) MUST be included for all break
			 * opportunities, including those within words, regardless of the "allowHyphens"
			 * argument. There is a break opportunity after each string returned by the {@link
			 * BrailleStream}.
			 */
			protected abstract BrailleStream translateAndHyphenate(Iterable<CSSStyledText> text, int from, int to);
			
			protected interface BrailleStream extends Cloneable {
				public boolean hasNext();
				/**
				 * @param limit The available space left on the current line. The returned string
				 *              does not have to fit in this space, but normally does in case of
				 *              "explicit" line breaking. If the returned string is longer, it will
				 *              be broken.
				 */
				public String next(int limit, boolean force, boolean allowHyphens);
				public Character peek();
				public String remainder();
				public Object clone();
				/**
				 * Whether the already streamed text, or the preceding segment if we are at the
				 * beginning of the stream, ended with a space.
				 *
				 * This method is only mandatory when we are the beginning of the stream. After the
				 * {@link #next(int, boolean, boolean)} method has been called,
				 * <code>hasPrecedingSpace</code> may throw a {@link UnsupportedOperationException}.
				 */
				public boolean hasPrecedingSpace();
			}
			
			public BrailleTranslator.LineIterator transform(final Iterable<CSSStyledText> text, int from, int to)
					throws TransformationException {
				List<Character> hyphenChars = new ArrayList<>();
				int wordSpacing; {
					wordSpacing = -1;
					for (CSSStyledText st : text) {
						SimpleInlineStyle style = st.getStyle();
						char hyphenChar = defaultHyphenChar;
						int spacing = 1;
						if (style != null) {
							CSSProperty val = style.getProperty("hyphenate-character");
							if (val != null) {
								if (val == HyphenateCharacter.braille_string) {
									String s = style.getValue(TermString.class, "hyphenate-character").getValue();
									if (s.length() == 1) {
										hyphenChar = s.charAt(0);
										if (brailleCharset != null)
											hyphenChar = brailleCharset.toText("" + hyphenChar).toCharArray()[0];
									} else
										logger.warn("The 'hyphenate-character' property must be a single character, "
										            + "but got {}", s); }
								style.removeProperty("hyphenate-character"); }
							val = style.getProperty("word-spacing");
							if (val != null) {
								if (val == WordSpacing.length) {
									spacing = style.getValue(TermInteger.class, "word-spacing").getIntValue();
									if (spacing < 0) {
										if (logger != null)
											logger.warn("word-spacing: {} not supported, must be non-negative", val);
										spacing = 1; }}
								
								// FIXME: assuming style is mutable and text.iterator() does not create copies
								style.removeProperty("word-spacing"); }}
						hyphenChars.add(hyphenChar);
						if (wordSpacing < 0)
							wordSpacing = spacing;
						else if (wordSpacing != spacing)
							throw new TransformationException("word-spacing must be constant, but both "
							                                  + wordSpacing + " and " + spacing + " specified"); }
					if (wordSpacing < 0) wordSpacing = 1;
				}
				List<BrailleTranslator.LineIterator> lineIterators = new ArrayList<>();
				Character hyphenChar = null;
				if (to < 0) to = hyphenChars.size();
				int i = from;
				while (i < to) {
					Character nextHyphenChar = hyphenChars.get(i);
					if (hyphenChar != nextHyphenChar) {
						if (i > from) {
							lineIterators.add(
								new LineIterator(translateAndHyphenate(text, from, i), blankChar, hyphenChar, wordSpacing));
							from = i; }
						hyphenChar = nextHyphenChar; }
					i++; }
				if (i > from)
					lineIterators.add(
						new LineIterator(translateAndHyphenate(text, from, i), blankChar, hyphenChar, wordSpacing));
				return CompoundBrailleTranslator.concatLineIterators(lineIterators);
			}
			
			protected static class FullyHyphenatedAndTranslatedString implements BrailleStream {
				private String next;
				private String lastWordPart; // if the last word continues in the next segment
				private String lastWordOtherPart;
				private final boolean precedingSpace;
				private final boolean alwaysEmpty;
				private final char hyphenChar;
				// SPACE, TAB, LF, CR, BLANK or LS
				private final static Pattern WORD_BOUNDARY = Pattern.compile("[\\x20\t\\n\\r\\u2800\u2028]");
				public FullyHyphenatedAndTranslatedString(String string) {
					this(string, 0, -1);
				}
				public FullyHyphenatedAndTranslatedString(String string, int from, int to) {
					this(string, from, to, SHY); // LineIterator converts SHY at the end of a line to a hyphen character
				}
				public FullyHyphenatedAndTranslatedString(String string, int from, int to, char hyphenChar) {
					// if there are no preceding segments, assume that we are at the beginning of a line,
					// so leading space can be stripped
					// FIXME: we can not make this assumption! see for example FormatterCoreContext,
					// which translates a space in order to obtain a value to use as margin
					// character, and it expects it to be a non-empty string
					//if (from == 0) precedingSpace = true; else
					precedingSpace = DefaultLineBreaker.hasPrecedingSpace(string, from);
					int len = string.length();
					if (from < 0 || from > len)
						throw new IllegalArgumentException();
					next = string.substring(from);
					lastWordPart = lastWordOtherPart = null;
					if (to >= 0 && to != len) {
						if (to > len || to < from)
							throw new IllegalArgumentException();
						to -= from;
						len -= from;
						int lastWordStart = lastIndexOf((" " + next.substring(0, to)), WORD_BOUNDARY);
						if (lastWordStart == to) {
							next = next.substring(0, to);
						} else {
							int lastWordEnd = to + indexOf((next.substring(to, len) + " "), WORD_BOUNDARY);
							if (lastWordEnd == to) {
								next = next.substring(0, to);
							} else {
								lastWordPart = next.substring(lastWordStart, to);
								lastWordOtherPart = next.substring(to, lastWordEnd);
								next = next.substring(0, lastWordStart);
								// ZWSP is not considered a WORD_BOUNDARY, but a word should not start with a ZWSP
								lastWordPart = lastWordPart.replaceAll("^\u200b*", "");
								if (lastWordPart.isEmpty())
									lastWordPart = lastWordOtherPart = null;
							}
						}
					} else {
						// remove SHY at end of stream because it would be converted to hyphen character
						next = next.replaceAll("\u00ad$", "");
					}
					if (next.replaceAll("[\u00ad\u200b]","").isEmpty())
						next = null;
					alwaysEmpty = (next == null);
					this.hyphenChar = hyphenChar;
				}
				public boolean hasNext() {
					return next != null || lastWordPart != null;
				}
				public String next(int limit, boolean force, boolean allowHyphens) {
					if (next != null) {
						String n = next;
						next = null;
						return n; }
					else if (lastWordPart != null) {
						if (force) {
							String n = lastWordPart;
							lastWordPart = null;
							return n; }
						Tuple2<String,byte[]> t = extractHyphens(lastWordPart + RS + lastWordOtherPart, false, SHY, ZWSP, RS);
						String lastWord = t._1;
						// if last word fits
						if (lastWord.length() <= limit) {
							String n = lastWordPart;
							lastWordPart = null;
							return n; }
						// else if the word can be hyphenated
						// assuming that if allowHyphens is true for this segment it will be true for the next segment
						else if (allowHyphens) {
							byte[] hyphens = t._2;
							boolean inFirstPart = false;
							String nextLastWordPart = null;
							for (int i = limit; i > 0; i--) {
								// FIXME: don't hard-code the number 4
								if ((hyphens[i - 1] & 4) == 4) {
									inFirstPart = true;
									nextLastWordPart = lastWord.substring(0, i); }
								// FIXME: don't hard-code these numbers
								if ((((hyphens[i - 1] & 1) == 1) && (i < limit)) || ((hyphens[i - 1] & 2) == 2)) {
									if (!inFirstPart) {
										// break point in second part of word, or in first part and first part does not fit on line
										// in both cases the implicit line breaking will break correctly
										String n = lastWordPart;
										lastWordPart = null;
										return n; }
									else {
										// break point in first part of word and first part fits on line
										// need to switch to "explicit" mode because otherwise the first part would not be broken
										String n = lastWord.substring(0, i);
										// FIXME: don't hard-code the number 1
										if ((hyphens[i - 1] & 1) == 1)
											n += hyphenChar;
										lastWordPart = nextLastWordPart.substring(i);
										if (lastWordPart.isEmpty()) lastWordPart = null;
										return n; }}}}
						// else move the word to the next line
						return "";
					}
					else
						throw new NoSuchElementException();
				}
				public Character peek() {
					if (next != null)
						return next.charAt(0);
					else if (lastWordPart != null)
						return lastWordPart.charAt(0);
					else
						throw new NoSuchElementException();
				}
				public String remainder() {
					if (next == null && lastWordPart == null)
						throw new NoSuchElementException();
					StringBuilder remainder = new StringBuilder();
					if (next != null)
						remainder.append(next);
					if (lastWordPart != null)
						remainder.append(lastWordPart);
					return remainder.toString();
				}
				public boolean hasPrecedingSpace() {
					if (next == null && !alwaysEmpty)
						throw new UnsupportedOperationException();
					else
						return precedingSpace;
				}
				@Override
				public Object clone() {
					try {
						return super.clone();
					} catch (CloneNotSupportedException e) {
						throw new InternalError("coding error");
					}
				}
				/**
				 * Version of {@link String#indexOf(int)} that searched for pattern instead of character.
				 */
				private static int indexOf(String string, Pattern pattern) {
					Matcher m = pattern.matcher(string);
					if (m.find())
						return m.start();
					else
						return -1;
				}
				/**
				 * Version of {@link String#lastIndexOf(int)} that searched for pattern instead of character.
				 */
				private static int lastIndexOf(String string, Pattern pattern) {
					Matcher m = pattern.matcher(string);
					int lastIndex = -1;
					while (m.find())
						lastIndex = m.start();
					return lastIndex;
				}
			}
			
			public static class LineIterator implements BrailleTranslator.LineIterator, Cloneable {
				
				private final char blankChar;
				private final char hyphenChar;
				private final int wordSpacing;
				
				public LineIterator(String fullyHyphenatedAndTranslatedString, char blankChar, char hyphenChar, int wordSpacing) {
					this(fullyHyphenatedAndTranslatedString, 0, -1, blankChar, hyphenChar, wordSpacing);
				}
				
				public LineIterator(String fullyHyphenatedAndTranslatedString, int from, int to,
				                    char blankChar, char hyphenChar, int wordSpacing) {
					this(new FullyHyphenatedAndTranslatedString(fullyHyphenatedAndTranslatedString, from, to, hyphenChar),
					     blankChar, hyphenChar, wordSpacing);
				}
				
				public LineIterator(BrailleStream inputStream, char blankChar, char hyphenChar, int wordSpacing) {
					this.inputStream = inputStream;
					this.blankChar = blankChar;
					this.hyphenChar = hyphenChar;
					this.wordSpacing = wordSpacing;
					this.lastCharIsSpace = inputStream.hasPrecedingSpace();
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
				// byte at index i corresponds with boundary between characters i and i+1 of charBuffer
				// or end of string if i == charBuffer.length()
				private ArrayList<Byte> wrapInfo = new ArrayList<Byte>();
				// from lowest to highest precedence:
				private final static byte NO_SOFT_WRAP = (byte)0x0;
				private final static byte SOFT_WRAP_WITH_HYPHEN = (byte)0x1;     //    x
				private final static byte SOFT_WRAP_WITHOUT_HYPHEN = (byte)0x3;  //   xx
				private final static byte SOFT_WRAP_AFTER_SPACE = (byte)0x7;     //  xxx
				private final static byte HARD_WRAP = (byte)0x15;                // xxxx
				
				// for collapsing spaces
				private boolean lastCharIsSpace = false;
				private int forcedBreakCount = 0;
				
				/**
				 * Fill the character (charBuffer) and soft wrap opportunity (wrapInfo) buffers while normalising and collapsing spaces
				 * - until the buffers are at least 'limit' long
				 * - or until the current row is full according to the input feed (BrailleStream)
				 * - and while the remaining input starts with SPACE, LF, CR, TAB, NBSP, BRAILLE PATTERN BLANK, SHY, ZWSP or LS
				 */
				private void fillRow(int limit, boolean force, boolean allowHyphens) {
					int bufSize = charBuffer.length();
					while (true) {
						if (inputBuffer != null && !inputBuffer.hasNext()) {
							// there is a break opportunity after each string returned by the stream
							// we keep soft hyphens at the end of the string (also if we are at the very end of the stream)
							if (bufSize > 0 && wrapInfo.get(bufSize - 1) != SOFT_WRAP_WITH_HYPHEN)
								wrapInfo.set(bufSize - 1, (byte)(wrapInfo.get(bufSize - 1) | SOFT_WRAP_WITHOUT_HYPHEN));
							inputBuffer = null;
						}
						if (inputBuffer == null) {
							if (!inputStream.hasNext()) // end of stream
								return;
							if (bufSize < limit) {
								String next = inputStream.next(limit - bufSize, force && (bufSize == 0), allowHyphens);
								if (next.isEmpty()) // row full according to input feed
									return;
								inputBuffer = peekingIterator(charactersOf(next).iterator()); }
							if (inputBuffer == null) {
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
							if (bufSize > 0 && (wrapInfo.get(bufSize - 1) & HARD_WRAP) != HARD_WRAP)
								wrapInfo.set(bufSize - 1, (byte)(wrapInfo.get(bufSize - 1) | HARD_WRAP));
							else {
								// add a blank to attach the HARD_WRAP info to
								// otherwise we can't preserve empty lines
								charBuffer.append(blankChar);
								bufSize ++;
								wrapInfo.add(HARD_WRAP);
							}
							lastCharIsSpace = true;
							break;
						default:
							if (bufSize >= limit) return;
							charBuffer.append(next);
							bufSize ++;
							wrapInfo.add(NO_SOFT_WRAP);
							lastCharIsSpace = false; }
						inputBuffer.next();
					}
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
						forcedBreakCount++;
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
					int save_forcedBreakCount = forcedBreakCount;
					forcedBreakCount = 0;
					fillRow(Integer.MAX_VALUE, true, false);
					String remainder = charBuffer.toString();
					inputStream = save_inputStream;
					inputBuffer = save_inputBuffer != null ? peekingIterator(save_inputBuffer.iterator()) : null;
					charBuffer = save_charBuffer;
					wrapInfo = save_wrapInfo;
					lastCharIsSpace = save_lastCharIsSpace;
					forcedBreakCount = save_forcedBreakCount;
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
				
				// FIXME: support METRIC_HYPHEN_COUNT
				public boolean supportsMetric(String metric) {
					return METRIC_FORCED_BREAK.equals(metric);
				}
				
				public double getMetric(String metric) {
					if (METRIC_FORCED_BREAK.equals(metric))
						return forcedBreakCount;
					else
						throw new UnsupportedMetricException("Metric not supported: " + metric);
				}
				
				private static final BrailleStream emptyBrailleStream = new BrailleStream() {
					public boolean hasNext() { return false; }
					public String next(int limit, boolean force, boolean allowHyphens) { throw new NoSuchElementException(); }
					public Character peek() { throw new NoSuchElementException(); }
					public String remainder() { throw new NoSuchElementException(); }
					public boolean hasPrecedingSpace() { return false; }
					@Override
					public Object clone() { return this; }
				};
			}
			
			/**
			 * Whether there is a space immediately before the substring starting at <code>before</code>.
			 */
			protected static boolean hasPrecedingSpace(String string, int before) {
				while (before > 0)
					switch (string.charAt(--before)) {
					case SPACE:
					case BLANK:
					case CR:
					case LF:
					case TAB:
					case LS:
						return true;
					case ZWSP:
						continue;
					default:
						return false;
					}
				return false;
			}
		}
	}
}
