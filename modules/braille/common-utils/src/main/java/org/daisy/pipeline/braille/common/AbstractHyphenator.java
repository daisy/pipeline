package org.daisy.pipeline.braille.common;

import java.util.regex.Pattern;

import static org.daisy.pipeline.braille.common.util.Strings.splitInclDelimiter;

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
		
		// TODO: caching?
		public static abstract class DefaultLineBreaker implements LineBreaker {
			
			protected abstract Break breakWord(String word, int limit, boolean force);
			
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
			
			private final static Pattern ON_SPACE_SPLITTER = Pattern.compile("\\s+");
			
			public LineIterator transform(final String text) {
				
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
										Break brokenWord = breakWord(segment, available, force && (available == limit));
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
