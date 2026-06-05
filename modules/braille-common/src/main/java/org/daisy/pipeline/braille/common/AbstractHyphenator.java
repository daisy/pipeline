package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import cz.vutbr.web.css.CSSProperty;

import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.SimpleInlineStyle;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.insertHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.Strings.splitInclDelimiter;
import org.daisy.pipeline.braille.common.util.Tuple2;
import org.daisy.pipeline.braille.css.CSSStyledText;

public abstract class AbstractHyphenator extends AbstractTransform implements Hyphenator {
	
	public FullHyphenator asFullHyphenator() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	public LineBreaker asLineBreaker() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/* ================== */
	/*       UTILS        */
	/* ================== */
	
	public static abstract class util {
		
		private final static char SHY = '\u00AD';
		private final static char ZWSP = '\u200B';
		private final static Pattern ON_SPACE_SPLITTER = Pattern.compile("\\s+");
		private final static Pattern COMPOUND_WORD_HYPHEN = Pattern.compile("[\\p{L}\\p{N}]-(?=[\\p{L}\\p{N}])");

		/**
		 * {@link FullHyphenator} that dispatches to other hyphenators based on language.
		 */
		public static abstract class LanguageBasedDispatchingFullHyphenator implements FullHyphenator {

			protected abstract Iterable<CSSStyledText> transform(Iterable<CSSStyledText> text, Locale language)
				throws NonStandardHyphenationException;

			public Iterable<CSSStyledText> transform(Iterable<CSSStyledText> text) throws NonStandardHyphenationException {
				// chunk up in chunks of same language
				Locale singleLang = null;
				boolean mixedLang = false; {
					boolean first = false;
					for (CSSStyledText t : text)
						if (first) {
							singleLang = t.getLanguage();
							first = false;
						} else if (!Objects.equals(singleLang, t.getLanguage())) {
							mixedLang = true;
							break;
						}
				}
				if (mixedLang) {
					List<CSSStyledText> result = new ArrayList<>();
					List<CSSStyledText> cur = null;
					Locale curLang = null;
					for (CSSStyledText t : text) {
						Locale lang = t.getLanguage();
						if (cur != null && !Objects.equals(curLang, lang)) {
							for (CSSStyledText tt : transform(cur, curLang)) result.add(tt);
							cur = null;
						}
						if (cur == null) cur = new ArrayList<>();
						cur.add(t);
						curLang = lang;
					}
					if (cur != null)
						for (CSSStyledText tt : transform(cur, curLang)) result.add(tt);
					return result;
				} else {
					return transform(text, singleLang);
				}
			}
		}

		public static abstract class DefaultFullHyphenator extends LanguageBasedDispatchingFullHyphenator {

			private final boolean keepStyle;

			protected DefaultFullHyphenator() {
				this(false);
			}

			/**
			 * @param keepStyle Whether to preserve {@code hyphens} properties in the output of
			 *                  {@link #transform(Iterable)}. This is useful when the output of a
			 *                  hyphenator needs to be fed into a second hyphenator.
			 */
			protected DefaultFullHyphenator(boolean keepStyle) {
				this.keepStyle = keepStyle;
			}

			/**
			 * Whether the length of the array returned by {@link
			 * #getHyphenationOpportunities(String, Locale)} is based on the number of code points
			 * in the input or the number of characters.
			 */
			protected abstract boolean isCodePointAware();

			/**
			 * Whether {@link #getHyphenationOpportunities(String, Locale)} takes into account the
			 * <code>language</code> argument.
			 */
			protected abstract boolean isLanguageAdaptive();

			/**
			 * Get hyphenation opportunities as a byte array (1 = SHY, 2 = ZWSP)
			 *
			 * @param textWithoutHyphens text that does not contain SHY and ZWSP characters (and from
			 *                           which no SHY and ZWSP characters were extracted either)
			 */
			protected abstract byte[] getHyphenationOpportunities(String textWithoutHyphens, Locale language)
				throws NonStandardHyphenationException;

			private final static char US = '\u001F';
			private final static Splitter SEGMENT_SPLITTER = Splitter.on(US);

			@Override
			public Iterable<CSSStyledText> transform(Iterable<CSSStyledText> text) throws NonStandardHyphenationException {
				if (!isLanguageAdaptive())
					return transform(text, null);
				else
					return super.transform(text);
			}

			/**
			 * Optimized version of {@link #transform(Iterable)} for cases where the input has a
			 * single segment.
			 */
			public String transform(String text, SimpleInlineStyle style, Locale language)
					throws NonStandardHyphenationException {
				Tuple2<String,byte[]> t = extractHyphens(text, isCodePointAware(), SHY, ZWSP);
				String textWithoutHyphens = t._1;
				byte[] manualHyphens = t._2;
				// insert zero-width space after hard hyphens ("-" followed and preceded by a letter
				// or number)
				byte[] hardHyphens; {
					int len = textWithoutHyphens.length();
					if (len > 1) {
						hardHyphens = new byte[len - 1]; {
							Matcher m = COMPOUND_WORD_HYPHEN.matcher(textWithoutHyphens);
							while (m.find())
								hardHyphens[m.start() + 1] = 4; }
						if (isCodePointAware())
							hardHyphens = extractHyphens(
								insertHyphens(textWithoutHyphens, hardHyphens, false, null, null, ZWSP),
								true, null, null, ZWSP)._2;
					} else
						hardHyphens = null;
				}
				if (hardHyphens != null) {
					if (manualHyphens == null)
						manualHyphens = hardHyphens;
					else
						for (int k = 0; k < hardHyphens.length - 1; k++)
							manualHyphens[k] |= hardHyphens[k];
				}
				Hyphens hyphenate = style != null ? style.getProperty("hyphens") : null;
				if (hyphenate == Hyphens.AUTO)
					return insertHyphens(textWithoutHyphens,
					                     transform(manualHyphens, textWithoutHyphens, language),
					                     isCodePointAware(),
					                     SHY, ZWSP, ZWSP)
					       .replace("" + ZWSP + ZWSP, "" + ZWSP);
				else if (hyphenate == null || hyphenate == Hyphens.MANUAL)
					return insertHyphens(textWithoutHyphens, manualHyphens, isCodePointAware(), SHY, ZWSP, ZWSP)
					       .replace("" + ZWSP + ZWSP, "" + ZWSP);
				else // hyphenate == Hyphens.NONE
				     // FIXME: better would be to only remove SHY and ZWSP within words, but the
				     // issue with this is that what constitutes a "word" is language dependent
				     // and finding words requires NLP
					return insertHyphens(textWithoutHyphens, manualHyphens, isCodePointAware(), null, null, ZWSP);
			}

			protected Iterable<CSSStyledText> transform(Iterable<CSSStyledText> text, Locale language) throws NonStandardHyphenationException {
				List<CSSStyledText> result = new ArrayList<>();
				List<CSSProperty> hyphenate = new ArrayList<>();
				boolean someHyphenate = false;
				for (CSSStyledText t : text) {
					if (!keepStyle)
						t = t.clone();
					SimpleInlineStyle style = t.getStyle();
					CSSProperty h = style != null ? style.getProperty("hyphens") : null;
					if (h == null) h = Hyphens.MANUAL;
					hyphenate.add(h);
					if (h == Hyphens.AUTO)
						someHyphenate = true;
					if (!keepStyle && style != null)
						style.removeProperty("hyphens");
					result.add(t);
				}
				if (result.size() == 0)
					return result;
				Tuple2<String,byte[]> t = extractHyphens(
					join(Iterables.transform(text, CSSStyledText::getText), US), isCodePointAware(), SHY, ZWSP);
				List<String> textWithoutHyphens = SEGMENT_SPLITTER.splitToList(t._1);
				t = extractHyphens(t._2, t._1, isCodePointAware(), null, null, null, US);
				String joinedTextWithoutHyphens = t._1;
				byte[] manualHyphensAndSegmentBoundaries = t._2;
				// insert zero-width space after hard hyphens ("-" followed and preceded by a letter
				// or number)
				byte[] hardHyphens; {
					int len = joinedTextWithoutHyphens.length();
					if (len > 1) {
						hardHyphens = new byte[len - 1]; {
							Matcher m = COMPOUND_WORD_HYPHEN.matcher(joinedTextWithoutHyphens);
							while (m.find())
								hardHyphens[m.start() + 1] = 4; }
						if (isCodePointAware())
							hardHyphens = extractHyphens(
								insertHyphens(joinedTextWithoutHyphens, hardHyphens, false, null, null, ZWSP),
								true, null, null, ZWSP)._2;
					} else
						hardHyphens = null;
				}
				if (hardHyphens != null) {
					if (manualHyphensAndSegmentBoundaries == null)
						manualHyphensAndSegmentBoundaries = hardHyphens;
					else
						for (int k = 0; k < hardHyphens.length - 1; k++)
							manualHyphensAndSegmentBoundaries[k] |= hardHyphens[k];
				}
				byte[] hyphensAndSegmentBoundaries = someHyphenate
					? transform(manualHyphensAndSegmentBoundaries, joinedTextWithoutHyphens, language)
					: manualHyphensAndSegmentBoundaries;
				List<String> textWithHyphensAuto =
					SEGMENT_SPLITTER.splitToList(
						insertHyphens(
							joinedTextWithoutHyphens, hyphensAndSegmentBoundaries, isCodePointAware(), SHY, ZWSP, ZWSP, US)
						.replace("" + ZWSP + ZWSP, "" + ZWSP));
				List<String> textWithHyphensManual =
					SEGMENT_SPLITTER.splitToList(
						insertHyphens(
							joinedTextWithoutHyphens, manualHyphensAndSegmentBoundaries, isCodePointAware(), SHY, ZWSP, ZWSP, US)
						.replace("" + ZWSP + ZWSP, "" + ZWSP));
				List<String> textWithHyphensNone =
					SEGMENT_SPLITTER.splitToList(
						insertHyphens(
							joinedTextWithoutHyphens, manualHyphensAndSegmentBoundaries, isCodePointAware(), null, null, ZWSP, US));
				int j = 0;
				for (int i = 0; i < result.size(); i++) {
					if (textWithoutHyphens.get(i).isEmpty())
						result.set(i, new CSSStyledText("",
						                                result.get(i).getStyle(),
						                                result.get(i).getLanguage(),
						                                result.get(i).getTextAttributes()));
					else {
						CSSProperty h = hyphenate.get(i);
						if (h == Hyphens.AUTO)
							result.set(i, new CSSStyledText(textWithHyphensAuto.get(j++),
							                                result.get(i).getStyle(),
							                                result.get(i).getLanguage(),
							                                result.get(i).getTextAttributes()));
						else if (h == Hyphens.MANUAL)
							result.set(i, new CSSStyledText(textWithHyphensManual.get(j++),
							                                result.get(i).getStyle(),
							                                result.get(i).getLanguage(),
							                                result.get(i).getTextAttributes()));
						else // h == Hyphens.NONE
							// FIXME: better would be to only remove SHY and ZWSP within words, but the
							// issue with this is that what constitutes a "word" is language dependent
							// and finding words requires NLP
							result.set(i, new CSSStyledText(textWithHyphensNone.get(j++),
							                                result.get(i).getStyle(),
							                                result.get(i).getLanguage(),
							                                result.get(i).getTextAttributes()));
					}
				}
				return result;
			}

			/**
			 * @param manualHyphens      SHY, ZWSP and US characters that were extracted from the original
			 *                           text, which resulted in <code>textWithoutHyphens</code>
			 * @param textWithoutHyphens text without SHY, ZWSP and US characters
			 */
			protected final byte[] transform(byte[] manualHyphens, String textWithoutHyphens, Locale language)
					throws NonStandardHyphenationException {
				if (textWithoutHyphens.length() == 0)
					return manualHyphens;
				boolean hasManualHyphens = false; {
					if (manualHyphens != null)
						for (byte b : manualHyphens)
							if (b == (byte)1 || b == (byte)2) {
								hasManualHyphens = true;
								break; }}
				if (hasManualHyphens) {
					// input contains SHY or ZWSP; hyphenate only the words without SHY or ZWSP
					// FIXME: for simplicity a "word" means a sequence of non white space here, but
					// a better definition is needed
					byte[] hyphens = Arrays.copyOf(manualHyphens, manualHyphens.length);
					boolean word = true;
					int pos = 0;
					for (String segment : splitInclDelimiter(textWithoutHyphens, ON_SPACE_SPLITTER)) {
						int len = isCodePointAware()
							? segment.codePointCount(0, segment.length())
							: segment.length();
						if (word && len > 0) {
							boolean wordHasManualHyphens = false; {
								for (int k = 0; k < len - 1; k++)
									if (hyphens[pos + k] != 0) {
										wordHasManualHyphens = true;
										break; }}
							if (!wordHasManualHyphens) {
								byte[] wordHyphens = getHyphenationOpportunities(segment, language);
								if (wordHyphens != null)
									for (int k = 0; k < len - 1; k++)
										hyphens[pos + k] |= wordHyphens[k];
							}
						}
						pos += len;
						word = !word;
					}
					return hyphens;
				} else {
					byte[] hyphens = getHyphenationOpportunities(textWithoutHyphens, language);
					if (manualHyphens != null) {
						if (hyphens == null)
							hyphens = manualHyphens;
						else
							for (int k = 0; k < hyphens.length; k++)
								hyphens[k] |= manualHyphens[k]; }
					return hyphens;
				}
			}
		}
		
		/**
		 * {@link FullHyphenator} that does not provide any hyphenation opportunities (only soft
		 * wrap opportunities outside words).
		 */
		public static class NoHyphenator extends DefaultFullHyphenator {
			
			public Iterable<CSSStyledText> transform(java.lang.Iterable<CSSStyledText> text) {
				if (COMPOUND_WORD_HYPHEN.matcher(
					    join(com.google.common.collect.Iterables.transform(text, CSSStyledText::getText))).find())
					return super.transform(text);
				else
					return text;
			}
			
			protected boolean isCodePointAware() { return true; }
			protected boolean isLanguageAdaptive() { return false; }
			
			protected byte[] getHyphenationOpportunities(String textWithoutManualHyphens, Locale language) {
				return null;
			}
		}
		
		// TODO: caching?
		public static abstract class DefaultLineBreaker implements LineBreaker {
			
			/**
			 * Break a sequence of non white space characters into two parts.
			 *
			 * This method is responsible for taking into account SHY or ZWSP within the input. SHY
			 * or ZWSP within an actual word (morphological unit) should be used as break
			 * opportunities without any additional hyphenation.
			 *
			 * This method should be overriden. The default behavior is that a word is only broken
			 * at SHY and ZWSP and after hard hyphens, or if the <code>force</code> argument is
			 * <code>true</code>. The <code>language</code> argument is ignored.
			 */
			protected Break breakWord(String word, Locale _language, int limit, boolean force) {
				if (word.length() <= limit)
					return new Break(word, limit, false);
				// break at SHY or ZWSP
				Tuple2<String,byte[]> t = extractHyphens(word, true, ZWSP, SHY);
				word = t._1;
				if (word.length() <= limit)
					return new Break(word, limit, false);
				byte[] manualHyphens = t._2;
				for (int k = manualHyphens.length - 1; k >= 0; k--)
					if (manualHyphens[k] != 0) {
						int br = word.offsetByCodePoints(0, k + 1);
						String next = word.substring(0, br);
						if (next.length() <= limit) {
							String remainder = word.substring(br);
							return new Break(next + remainder, next.length(), manualHyphens[k] == 2);
						}
					}
				// break after hard hyphens
				Matcher m = COMPOUND_WORD_HYPHEN.matcher(word);
				if (m.find()) {
					int len = m.start() + 2;
					if (len <= limit)
						return new Break(word, len, false);
				}
				if (force)
					return new Break(word, limit, false);
				return new Break(word, 0, false);
			}
			
			protected static class Break {
				private final String text;
				private final int position;
				private final boolean hyphen;
				public Break(String text, int position, boolean hyphen) {
					this.text = text;
					this.position = position;
					this.hyphen = hyphen;
				}
				private String firstLine() {
					return text.substring(0, position);
				}
				private String secondLine() {
					return text.substring(position);
				}
				@Override
				public String toString() {
					return firstLine() + "=" + secondLine();
				}
			}
			
			public LineIterator transform(final String text, final Locale language) {
				
				return new LineIterator() {
					
					String remainder = text;
					String remainderAtMark = text;
					boolean lineHasHyphen = false;
					boolean lineHasHyphenAtMark = false;
					boolean started = false;
					boolean startedAtMark = false;
					
					public String nextLine(int limit, boolean force) {
						return nextLine(limit, force, true);
					}
					
					public String nextLine(int limit, boolean force, boolean allowHyphens) {
						started = true;
						String line = "";
						lineHasHyphen = false;
						if (remainder != null) {
							if (remainder.length() <= limit) {
								line += remainder;
								remainder = null; }
							else {
								String r = "";
								int available = limit;
								boolean word = true;
								for (String segment : splitInclDelimiter(remainder, ON_SPACE_SPLITTER)) {
									if (available == 0)
										r += segment;
									else if (segment.length() <= available) {
										line += segment;
										available -= segment.length();
										word = !word; }
									else if (word && allowHyphens) {
										// segment may contain SHY or ZWSP; breakWord() is responsible for correctly taking them into account
										Break brokenWord = breakWord(segment, language, available, force && (available == limit));
										line += brokenWord.firstLine();
										lineHasHyphen = brokenWord.hyphen;
										r += brokenWord.secondLine();
										available = 0; }
									else {
										r += segment;
										available = 0; }}
								remainder = r.isEmpty() ? null : r; }}
						return line;
					}
					
					public boolean hasNext() {
						return remainder != null;
					}
					
					public boolean lineHasHyphen() {
						if (!started)
							throw new RuntimeException("nextLine must be called first.");
						return lineHasHyphen;
					}
					
					public String remainder() {
						return remainder;
					}
					
					public void mark() {
						remainderAtMark = remainder;
						lineHasHyphenAtMark = lineHasHyphen;
						startedAtMark = started;
					}
					
					public void reset() {
						remainder = remainderAtMark;
						lineHasHyphen = lineHasHyphenAtMark;
						started = startedAtMark;
					}
				};
			}
		}
	}
}
