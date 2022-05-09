package org.liblouis;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.HashMap;
import java.util.Map;

public interface DisplayTable {

	public enum IOMode {
		/**
		 * Representation of dots patterns according to display table.
		 */
		DEFAULT(0),
		/**
		 * Liblouis' "dotsIO" representation of dot patterns:
		 *
		 * - bit 16 always set
		 * - bit 15 = (virtual) dot F ... bit 9 = (virtual) dot 9
		 * - bit 8 = dot 8 ... bit 1 = dot 1
		 * - bits 32 to 17 are ignored
		 */
		DOTS_IO(4);

		private final int value;
		private IOMode(int value) {
			this.value = value;
		}
		public int value() {
			return value;
		}
	}

	/**
	 * Charset to decode Liblouis' representation of dot patterns, which can be the "dotsIO"
	 * representation or which can be according to a display table (depending on the mode).
	 */
	public Charset asCharset();

	/**
	 * Mode value to set in order to be able to use the {@link Charset} returned by {@link #asCharset()}.
	 */
	public IOMode getMode();

	/**
	 * Convert a dot pattern from Unicode braille (U+2800 to U+28FF) to the representation defined
	 * in this display table.
	 */
	public Character encode(Character c);

	/**
	 * Convert a dot pattern from the representation defined in this table to Unicode braille.
	 */
	public Character decode(Character c);

	/**
	 * Convert a braille string either Unicode braille (U+2800 to U+28FF) to the representation
	 * defined in this display table.
	 *
	 * @return a String with the same length as <code>s</code>.
	 */
	public String encode(String s);

	/**
	 * Convert a braille string from the representation defined in this table to Unicode braille.
	 *
	 * @return a Unicode braille string with the same length as <code>s</code>.
	 */
	public String decode(String s);

	public final class StandardDisplayTables {
		private StandardDisplayTables() {}
		/**
		 * Display table as defined by the translation table.
		 *
		 * This is an exception to the other tables in that the Charset corresponds with the
		 * "default" i/o mode of Liblouis.
		 */
		public static final DisplayTable DEFAULT = new DisplayTable() {
				Charset charset;
				@Override
				public Charset asCharset() {
					if (charset == null)
						charset = WideChar.SIZE == 2 ? Charset.forName("UTF-16LE") : Charset.forName("UTF-32LE");
					return charset;
				}
				@Override
				public IOMode getMode() {
					return IOMode.DEFAULT;
				}
				@Override
				public Character encode(Character c) {
					throw new UnsupportedOperationException();
				}
				@Override
				public Character decode(Character c) {
					throw new UnsupportedOperationException();
				}
				@Override
				public String encode(String s) {
					throw new UnsupportedOperationException();
				}
				@Override
				public String decode(String s) {
					throw new UnsupportedOperationException();
				}
			};
		public static final DisplayTable UNICODE = new UnicodeBrailleDisplayTable(Fallback.REPORT);
	}

	public final class Fallback {
		enum FallbackMethod {
			// IGNORE,
			REPLACE,
			REPORT,
			MASK
		}
		final FallbackMethod method;
		final Character replacement;
		private static Map<Character,Fallback> replacements = new HashMap<Character,Fallback>();
		private Fallback(FallbackMethod method) {
			this.method = method;
			this.replacement = null;
		}
		private Fallback(Character replacement) {
			this.method = FallbackMethod.REPLACE;
			this.replacement = replacement;
		}
		@Override
		public String toString() {
			return method + (method == FallbackMethod.REPLACE ? ("-" + replacement) : "");
		}

		/**
		 * Act according to {@link CharsetDecoder#onUnmappableCharacter(CodingErrorAction)} and
		 * {@link CharsetDecoder#replaceWith(String)} settings.
		 */
		public static final Fallback REPORT = new Fallback(FallbackMethod.REPORT);

		/**
		 * Always behave as if {@link CharsetDecoder#onUnmappableCharacter(CodingErrorAction)}
		 * was set to {@link CodingErrorAction#IGNORE}.
		 *//*
		public static final Fallback IGNORE = new Fallback(FallbackMethod.IGNORE);*/

		/**
		 * Ignore only the part of the dot pattern that can not be handled (e.g. everything that
		 * is not dots 1 to 6, or everything that is not dots 1 to 8).
		 */
		public static final Fallback MASK = new Fallback(FallbackMethod.MASK);

		/**
		 * Always behave as if {@link CharsetDecoder#onUnmappableCharacter(CodingErrorAction)}
		 * was set to {@link CodingErrorAction#REPLACE} and {@link
		 * CharsetDecoder#replaceWith(String)} was set to {@code replacement}.
		 */
		public static Fallback REPLACE(Character replacement) {
			if (replacement == null)
				throw new NullPointerException();
			Fallback f = replacements.get(replacement);
			if (f == null) {
				f = new Fallback(replacement);
				replacements.put(replacement, f);
			}
			return f;
		}
	}

	public class UnicodeBrailleDisplayTable implements DisplayTable {

		private final Charset charset;

		/**
		 * @param virtualDotsFallback Fallback to use for virtual dot patterns. Defaults to REPORT.
		 */
		public UnicodeBrailleDisplayTable(Fallback virtualDotsFallback) {
			this(virtualDotsFallback, null);
		}

		// FIXME: currently not used
		/**
		 * @param eightDotsFallback Fallback to use for dot patterns that contain dot 7 or 8. If
		 *                          null, the 2840-28FF range is used.
		 */
		private UnicodeBrailleDisplayTable(Fallback virtualDotsFallback,
		                                   Fallback eightDotsFallback) {
			if (virtualDotsFallback == null)
				virtualDotsFallback = Fallback.REPORT;
			charset = new UnicodeBrailleCharset(WideChar.SIZE, virtualDotsFallback);
		}

		@Override
		public Charset asCharset() {
			return charset;
		}

		@Override
		public IOMode getMode() {
			return IOMode.DOTS_IO;
		}

		@Override
		public Character encode(Character c) {
			return c;
		}

		@Override
		public Character decode(Character c) {
			return c;
		}

		@Override
		public String encode(String s) {
			return s;
		}

		@Override
		public String decode(String s) {
			return s;
		}

		private static class UnicodeBrailleCharset extends Charset {

			private final int charsize;
			private final Fallback virtualDotsFallback;

			private UnicodeBrailleCharset(int charsize, Fallback virtualDotsFallback) {
				// Note that two distinct instances could get the same name. Because the equals
				// method is solely based on the name (and final), we should therefore use
				// reference-equality instead of object-equality whenever two charsets need to be
				// compared.
				super("LOU-DOTSIO-UNICODE-" + (charsize * 8), null);
				this.charsize = charsize;
				this.virtualDotsFallback = virtualDotsFallback;
			}

			@Override
			public boolean contains(Charset cs) {
				return equals(cs);
			}

			@Override
			public CharsetDecoder newDecoder() {
				return new DotsIODecoder();
			}

			@Override
			public CharsetEncoder newEncoder() {
				return new DotsIOEncoder();
			}

			private class DotsIOEncoder extends CharsetEncoder {
				private DotsIOEncoder() {
					super(UnicodeBrailleCharset.this, charsize, charsize,
					      charsize == 2
					      ? new byte[]{(byte)0x00,(byte)0x80}
					      : new byte[]{(byte)0x00,(byte)0x80,(byte)0x00,(byte)0x00});
				}
				@Override
				protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
					int k = 0;
					while (in.hasRemaining()) {
						char c = in.get(k++);
						if (c < '\u2800' || c > '\u28ff')
							return CoderResult.unmappableForLength(1);
						if (out.remaining() < charsize)
							return CoderResult.OVERFLOW;
						for (int i = 2; i < charsize; i++)
							out.put((byte)0);
						out.put((byte)(c & '\u00ff'));
						out.put((byte)0x80);
						in.get();
					}
					return CoderResult.UNDERFLOW;
				}
			}

			private class DotsIODecoder extends CharsetDecoder {
				private DotsIODecoder() {
					super(UnicodeBrailleCharset.this, 1.f / charsize, 1);
					replaceWith("\u2800");
				}
				@Override
				protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
					int k = 0;
					while (in.remaining() >= charsize) {
						byte b1 = in.get(k * charsize + 1);
						if ((b1 & 0x80) == 0)
							return CoderResult.malformedForLength(charsize);
						byte b2 = in.get(k * charsize);
						Character c = (char)((b2 & 0xff) | '\u2800');
						if ((b1 & 0xff) != 0x80)
							// virtual dots present
							switch (virtualDotsFallback.method) {
							// case IGNORE:
							// 	c = null;
							// 	break;
							case REPLACE:
								c = virtualDotsFallback.replacement;
								break;
							case MASK:
								break;
							case REPORT:
							default:
								return CoderResult.unmappableForLength(charsize);
							}
						if (!out.hasRemaining())
							return CoderResult.OVERFLOW;
						if (c != null)
							out.put(c);
						// ignoring byte 3 and 4 if present
						for (int i = 0; i < charsize; i++)
							in.get();
						k++;
					}
					return CoderResult.UNDERFLOW;
				};
			}
		}
	}

	public static DisplayTable fromTable(String table) {
		return new DisplayTableFromTable(table, Fallback.REPORT);
	}

	public static DisplayTable fromTable(String table, Fallback virtualDotsFallback) {
		return new DisplayTableFromTable(table, virtualDotsFallback);
	}

	public class DisplayTableFromTable implements DisplayTable {

		private final String table;
		private final Fallback virtualDotsFallback;
		private Charset charset = null;

		private DisplayTableFromTable(String table, Fallback virtualDotsFallback) {
			this.table = table;
			this.virtualDotsFallback = virtualDotsFallback != null ? virtualDotsFallback : Fallback.REPORT;
		}

		@Override
		public Charset asCharset() {
			if (charset == null)
				charset = new CharsetFromTable(WideChar.SIZE, virtualDotsFallback);
			return charset;
		}

		@Override
		public String toString() {
			return table;
		}
		
		@Override
		public IOMode getMode() {
			return IOMode.DOTS_IO;
		}

		@Override
		public Character encode(Character c) {
			return encode("" + c).charAt(0);
		}

		@Override
		public Character decode(Character c) {
			return decode("" + c).charAt(0);
		}

		/* Note that this method also supports input characters in the range U+8000 to U+FFFF
		 * (Liblouis' "dotsIO" format, but the API does not allow it.
		 */
		@Override
		public String encode(String s) {
			int length = s.length();
			if (length == 0)
				return s;
			WideCharString inbuf;
			try {
				inbuf = Translator.getWideCharBuffer("text-in", length).write(s); }
			catch (IOException e) {
				throw new RuntimeException("should not happen", e); }
			WideCharString outbuf = Translator.getWideCharBuffer("text-out", length);
			int mode = 0; // mode argument not used
			if (Louis.getLibrary().lou_dotsToChar(table, inbuf, outbuf, length, mode) == 0)
				throw new RuntimeException("lou_dotsToChar failed"); // should not happen if table was compiled successfully
			try {
				return outbuf.read(length); }
			catch (IOException e) {
				throw new RuntimeException("should not happen", e); }
		}

		@Override
		public String decode(String s) {
			int length = s.length();
			if (length == 0)
				return s;
			WideCharString inbuf;
			try {
				inbuf = Translator.getWideCharBuffer("text-in", length).write(s); }
			catch (IOException e) {
				throw new RuntimeException("should not happen", e); }
			WideCharString outbuf = Translator.getWideCharBuffer("text-out", length);
			int ucBrl = 64;
			if (Louis.getLibrary().lou_charToDots(table, inbuf, outbuf, length, ucBrl) == 0)
				throw new RuntimeException("lou_charToDots failed"); // should not happen if table was compiled successfully
			try {
				return outbuf.read(length); }
			catch (IOException e) {
				throw new RuntimeException("should not happen", e); }
		}

		private class CharsetFromTable extends Charset {

			private final int charsize;
			private final Fallback virtualDotsFallback;

			private CharsetFromTable(int charsize, Fallback virtualDotsFallback) {
				// Note that two distinct instances could get the same name. Because the equals
				// method is solely based on the name (and final), we should therefore use
				// reference-equality instead of object-equality whenever two charsets need to be
				// compared.
				super("LOU-DOTSIO-ASCII-" + (charsize * 8), null);
				this.charsize = charsize;
				this.virtualDotsFallback = virtualDotsFallback;
			}

			@Override
			public boolean contains(Charset cs) {
				return equals(cs);
			}

			@Override
			public CharsetDecoder newDecoder() {
				return new DotsIODecoder();
			}

			@Override
			public CharsetEncoder newEncoder() {
				return new DotsIOEncoder();
			}

			private class DotsIOEncoder extends CharsetEncoder {
				private DotsIOEncoder() {
					super(CharsetFromTable.this, charsize, charsize,
					      charsize == 2
					      ? new byte[]{(byte)0x00,(byte)0x80}
					      : new byte[]{(byte)0x00,(byte)0x80,(byte)0x00,(byte)0x00});
				}
				@Override
				protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
					int k = 0;
					while (in.hasRemaining()) {
						char c = in.get(k++);
						c = DisplayTableFromTable.this.decode(c);
						// U+2800 could mean either that the character maps to the blank pattern, or
						// that it does not have a mapping. We must assume that it maps to the blank
						// pattern, in other words we're ignoring unknown characters.
						if (c < '\u2800' || c > '\u28ff')
							return CoderResult.unmappableForLength(1); // should not happen
						if (out.remaining() < charsize)
							return CoderResult.OVERFLOW;
						for (int i = 2; i < charsize; i++)
							out.put((byte)0);
						out.put((byte)(c & '\u00ff'));
						out.put((byte)0x80);
						in.get();
					}
					return CoderResult.UNDERFLOW;
				}
			}

			private class DotsIODecoder extends CharsetDecoder {
				private DotsIODecoder() {
					super(CharsetFromTable.this, 1.f / charsize, 1);
					replaceWith("\u2800");
				}
				@Override
				protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
					int k = 0;
					while (in.remaining() >= charsize) {
						byte b1 = in.get(k * charsize + 1);
						if ((b1 & 0x80) == 0)
							return CoderResult.malformedForLength(charsize);
						byte b2 = in.get(k * charsize);
						Character c = (char)((b2 & 0xff) | (b1 << 8));
						c = DisplayTableFromTable.this.encode(c);
						if (c == ' ') {
							// A space could mean either that the dot pattern maps to a space, or
							// that it does not have a mapping. If the dot pattern has no real dots,
							// only virtual dots, we map it to a space, otherwise we handle
							// according to the fallback method.
							if ((b2 & 0xff) != 0x0)
								switch (virtualDotsFallback.method) {
								case REPLACE:
									c = virtualDotsFallback.replacement;
									break;
								case MASK:
									// remove virtual dots
									c = (char)((b2 & 0xff) | '\u8000');
									c = DisplayTableFromTable.this.encode(c);
									if (c != ' ')
										break;
									else
										// if it's a space, we assume the base dot pattern has no
										// mapping either
										return CoderResult.unmappableForLength(charsize);
								case REPORT:
								default:
									return CoderResult.unmappableForLength(charsize);
								}
						}
						if (!out.hasRemaining())
							return CoderResult.OVERFLOW;
						if (c != null)
							out.put(c);
						// ignoring byte 3 and 4 if present
						for (int i = 0; i < charsize; i++)
							in.get();
						k++;
					}
					return CoderResult.UNDERFLOW;
				};
			}
		}
	}
}
