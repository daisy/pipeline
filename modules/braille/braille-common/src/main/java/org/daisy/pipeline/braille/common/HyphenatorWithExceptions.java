package org.daisy.pipeline.braille.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.TreeMap;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Iterables;

import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.SimpleInlineStyle;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultFullHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.util.Strings;
import org.daisy.pipeline.braille.common.util.Tuple2;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HyphenatorWithExceptions extends AbstractHyphenator {

	private final Hyphenator hyphenator;
	private final Reader exceptionsFile;
	private final Map<String,byte[]> exceptionWords;

	public HyphenatorWithExceptions(Hyphenator hyphenator, Reader exceptionsFile) throws IOException {
		this.hyphenator = hyphenator;
		this.exceptionsFile = exceptionsFile;
		exceptionWords = compileExceptionsFile(exceptionsFile);
	}

	private final static char SHY = '\u00AD';
	private final static char ZWSP = '\u200B';
	// for simplicity a word means a sequence of letters
	private final static Pattern WORD = Pattern.compile("\\p{L}[\\p{L}" + SHY + ZWSP + "]*\\p{L}");
	private final static Pattern EXCEPTION_WORD = Pattern.compile("\\p{L}[\\p{L}-]*\\p{L}");

	private FullHyphenator exceptionProcessor = null;
	private FullHyphenator fullHyphenator = null;
	private LineBreaker lineBreaker = null;

	@Override
	public FullHyphenator asFullHyphenator() {
		if (fullHyphenator == null) {
			FullHyphenator backingHyphenator = hyphenator.asFullHyphenator();
			if (exceptionProcessor == null)
				exceptionProcessor = new DefaultFullHyphenator(true) { // to preserve `hyphens: auto' in the output
						@Override
						protected boolean isCodePointAware() {
							return true;
						}
						@Override
						protected boolean isLanguageAdaptive() {
							return false;
						}
						@Override
						protected byte[] getHyphenationOpportunities(String textWithoutHyphens, Locale _language)
								throws NonStandardHyphenationException {
							byte[] result = new byte[textWithoutHyphens.codePointCount(0, textWithoutHyphens.length()) - 1];
							int i = 0;
							boolean isWord = false;
							for (String w : Strings.splitInclDelimiter(textWithoutHyphens, WORD)) {
								int l = w.codePointCount(0, w.length());
								if (i > 0 && l > 0)
									result[i++] = 0;
								if (isWord) {
									byte[] r = exceptionWords.get(w);
									if (r != null)
										for (int j = 0; j < l - 1; j++)
											result[i++] = r[j];
									else
										for (int j = 0; j < l - 1; j++)
											result[i++] = 0;
								} else {
									for (int j = 0; j < l - 1; j++)
										result[i++] = 0;
								}
								isWord = !isWord;
							}
							return result;
						}
				};
			fullHyphenator = new FullHyphenator() {
					@Override
					public Iterable<CSSStyledText> transform(Iterable<CSSStyledText> text) throws NonStandardHyphenationException {
						if (Iterables.any(text, t -> t.getStyle() != null && t.getStyle().getProperty("hyphens") == Hyphens.AUTO)) {
							// check whether text contains words that are present in the dictionary
							// (words that already contain SHY or ZWSP are not counted)
							boolean containsExceptionWords = false; {
								boolean isWord = false;
								for (String w : Strings.splitInclDelimiter(Strings.join(Iterables.transform(text, CSSStyledText::getText)), WORD)) {
									if (isWord && exceptionWords.containsKey(w)) {
										containsExceptionWords = true;
										break; }
									isWord = !isWord; }}
							if (containsExceptionWords)
								// insert soft hyphens into the words (they will take priority over automatic hyphenation)
								text = exceptionProcessor.transform(text);
						}
						return backingHyphenator.transform(text);
					}
					@Override
					public String transform(String text, SimpleInlineStyle style, Locale language)
							throws NonStandardHyphenationException {
						if (style != null && style.getProperty("hyphens") == Hyphens.AUTO) {
							// check whether text contains words that are present in the dictionary
							// (words that already contain SHY or ZWSP are not counted)
							boolean containsExceptionWords = false; {
								boolean isWord = false;
								for (String w : Strings.splitInclDelimiter(text, WORD)) {
									if (isWord && exceptionWords.containsKey(w)) {
										containsExceptionWords = true;
										break; }
									isWord = !isWord; }}
							if (containsExceptionWords)
								// insert soft hyphens into the words (they will take priority over automatic hyphenation)
								text = exceptionProcessor.transform(text, style, language);
						}
						return backingHyphenator.transform(text, style, language);
					}
					@Override
					public String toString() {
						return HyphenatorWithExceptions.this.toString();
					}
				};
		}
		return fullHyphenator;
	}

	/**
	 * @throws UnsupportedOperationException if the underlying hyphenator's {@link asLineBreaker()} method throws an
	 *                                       {@link UnsupportedOperationException}
	 */
	public LineBreaker asLineBreaker() throws UnsupportedOperationException {
		if (lineBreaker == null) {
			LineBreaker backingHyphenator = hyphenator.asLineBreaker(); // if this throws a UnsupportedOperationException,
			                                                            // it means that hyphenator.asFullHyphenator(), and
			                                                            // therefore asFullHyphenator(), will never throw a
			                                                            // NonStandardHyphenationException, so it is
			                                                            // allowed for asLineBreaker() to throw a
			                                                            // UnsupportedOperationException
			// LineBreaker that breaks words at SHY at ZWSP. It is expected that ZWSP have already been inserted after
			// hard hyphens.
			LineBreaker standardLineBreaker = new DefaultLineBreaker() {};
			SimpleInlineStyle HYPHENS_AUTO = new SimpleInlineStyle("hyphens: auto");
			lineBreaker = new DefaultLineBreaker() {
					@Override
					public LineIterator transform(String text, Locale language) {
						// try to perform full hyphenation
						try {
							return standardLineBreaker.transform(
								HyphenatorWithExceptions.this.asFullHyphenator().transform(text, HYPHENS_AUTO, language),
								null);
						} catch (NonStandardHyphenationException e) {
							// try to process all exception words in the input before applying the main hyphenator
							// (soft hyphens inserted by the first pass will take priority)
							if (backingHyphenator != null) {
								// check whether text contains words that are present in the dictionary
								// (words that already contain SHY or ZWSP are not counted)
								boolean containsExceptionWords = false; {
									boolean isWord = false;
									for (String w : Strings.splitInclDelimiter(text, WORD)) {
										if (isWord && exceptionWords.containsKey(w)) {
											containsExceptionWords = true;
											break; }
										isWord = !isWord; }}
								if (containsExceptionWords)
									try {
										text = exceptionProcessor.transform(text, HYPHENS_AUTO, null);
									} catch (NonStandardHyphenationException ee) {
										throw new IllegalStateException("coding error");
									}
								return backingHyphenator.transform(text, language);
							} else {
								// fall back to word-by-word processing (see breakWord())
								// first handle compound word hyphens
								text = text.replaceAll("(?<=[\\p{L}\\p{N}])-(?=[\\p{L}\\p{N}])", "-" + ZWSP);
								return super.transform(text, language);
							}
						}
					}
					@Override
					protected Break breakWord(String word, Locale language, int limit, boolean force) {
						if (word.length() <= limit)
							return new Break(word, limit, false);
						String line = "";
						boolean lineHasHyphen = false;
						String remainder = "";
						// further break up sequence of non white space characters
						boolean isWord = false;
						for (String w : Strings.splitInclDelimiter(word, WORD)) {
							int available = limit - line.length() - remainder.length();
							if (available <= 0) {
								if (line.isEmpty()) {
									remainder = word;
									break;
								}
								remainder += w;
							// check if segment contains SHY or ZWSP or hard hyphen
							} else if (w.indexOf(SHY) >= 0 || w.indexOf(ZWSP) >= 0) {
								LineIterator lines = standardLineBreaker.transform(String.format("x%sx", w), null);
								String next = lines.nextLine(available + 1, false);
								if (!next.isEmpty() && lines.hasNext()) {
									line += (remainder + next.substring(1));
									lineHasHyphen = lines.lineHasHyphen();
									remainder = lines.remainder();
									remainder = remainder.substring(0, remainder.length() - 1);
								} else {
									remainder += (next + lines.remainder());
									remainder = remainder.substring(1, remainder.length() - 1);
								}
							} else if (isWord) {
								LineIterator lines = null; {
									String fullyHyphenated = null;
									// check whether word is present in the dictionary
									if (exceptionWords.containsKey(w)) {
										// try full hyphenation of word
										try {
											fullyHyphenated = exceptionProcessor.transform(w, HYPHENS_AUTO, null);
										} catch (NonStandardHyphenationException e) {
											throw new IllegalStateException("coding error");
										}
									} else {
										// use main hyphenator if word does not contain SHY or ZWSP
										if (backingHyphenator != null)
											lines = backingHyphenator.transform(word, language);
										else {
											try {
												fullyHyphenated = hyphenator.asFullHyphenator().transform(w, HYPHENS_AUTO, null);
											} catch (NonStandardHyphenationException e) {
												// if hyphenator.asLineBreaker() is null, hyphenator.asFullHyphenator() must
												// never throw a NonStandardHyphenationException
												throw new IllegalStateException("coding error");
											}
										}
									}
									if (lines == null) {
										if (fullyHyphenated == null)
											throw new IllegalStateException("coding error");
										lines = standardLineBreaker.transform(fullyHyphenated, null);
									}
								}
								String next = lines.nextLine(available, false);
								if (!next.isEmpty() && lines.hasNext()) {
									line += (remainder + next);
									lineHasHyphen = lines.lineHasHyphen();
									remainder = lines.remainder();
								} else {
									remainder += (next + lines.remainder());
								}
							} else {
								remainder += w;
							}
							isWord = !isWord;
						}
						// if a break point was found, return it
						if (!line.isEmpty())
							return new Break(line + remainder, line.length(), lineHasHyphen);
						if (force)
							// force break
							return new Break(word, limit, false);
						return new Break(word, 0, false);
					}
					@Override
					public String toString() {
						return HyphenatorWithExceptions.this.toString();
					}
				};
		}
		return lineBreaker;
	}

	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("HyphenatorWithExceptions")
			.add("hyphenator", hyphenator)
			.add("exceptions", exceptionsFile);
	}

	private static Map<String,byte[]> compileExceptionsFile(Reader file) throws IOException {
		Map<String,byte[]> map = new TreeMap<>();
		try (BufferedReader reader = new BufferedReader(file)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!EXCEPTION_WORD.matcher(line).matches()) {
					logger.warn("Invalid word in exceptions list: " + line);
					continue;
				}
				Tuple2<String,byte[]> t = Strings.extractHyphens(line, true, '-');
				map.put(t._1, t._2);
			}
		}
		return map;
	}

	private static final Logger logger = LoggerFactory.getLogger(HyphenatorWithExceptions.class);
}
