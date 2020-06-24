package org.liblouis;

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

	/**
	 * Charset to decode Liblouis' custom representation of dot patterns in "dotsIO" mode:
	 *
	 * - bit 16 always set
	 * - bit 15 = (virtual) dot F ... bit 9 = (virtual) dot 9
	 * - bit 8 = dot 8 ... bit 1 = dot 1
	 * - bits 32 to 17 are ignored
	 */
	public Charset asCharset();

	public final class StandardDisplayTables {
		private StandardDisplayTables() {}
		/**
		 * Display table as defined by the translation table.
		 *
		 * This is an exception to the other tables in that the Charset corresponds with the
		 * "normal" i/o mode of Liblouis.
		 */
		public static final DisplayTable DEFAULT = new DisplayTable() {
				Charset charset;
				@Override
				public Charset asCharset() {
					if (charset == null)
						charset = WideChar.SIZE == 2 ? Charset.forName("UTF-16LE") : Charset.forName("UTF-32LE");
					return charset;
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
		 * is not dots 1 to 6).
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

		private class UnicodeBrailleCharset extends Charset {

			final int charsize;
			final Fallback virtualDotsFallback;

			UnicodeBrailleCharset(int charsize, Fallback virtualDotsFallback) {
				// Note that two distinct instances could get the same name. Because the equals
				// method is solely based on the name (and final), we should therefore use
				// reference-equality instead of object-equality whenever two charsets need to be
				// compared.
				super("LOU-DOTSIO-" + (charsize * 8), null);
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

			class DotsIOEncoder extends CharsetEncoder {
				DotsIOEncoder() {
					super(UnicodeBrailleCharset.this, charsize, charsize,
					      charsize == 2
					      ? new byte[]{(byte)0x00,(byte)0x80}
					      : new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x80});
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

			class DotsIODecoder extends CharsetDecoder {
				DotsIODecoder() {
					super(UnicodeBrailleCharset.this, 1.f / charsize, 1);
					replaceWith("\u2800");
				}
				@Override
				protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
					int k = 0;
					while (in.remaining() >= charsize) {
						k++;
						byte b1 = in.get(k * charsize - 1);
						if ((b1 & 0x80) == 0)
							return CoderResult.malformedForLength(charsize);
						byte b2 = in.get(k * charsize - 2);
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
					}
					return CoderResult.UNDERFLOW;
				};
			}
		}
	}
}
