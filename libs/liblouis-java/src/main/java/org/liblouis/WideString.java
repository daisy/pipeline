package org.liblouis;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.IdentityHashMap;
import java.util.Map;

import com.sun.jna.Memory;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;

import org.liblouis.DisplayTable.StandardDisplayTables;

public final class WideString extends PointerType implements NativeMapped {
	
	private static Map<Charset,CharsetEncoder> encoders;
	private static Map<Charset,CharsetDecoder> decoders;
	
	private final int length;
	
	public WideString() {
		this(0);
	}
	
	WideString(int length) {
		this.length = length;
	}
	
	WideString(String value) {
		this(value.length());
		try {
			write(value); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
	}
	
	WideString(Pointer p, int offset, int length) {
		this(length);
		setPointer(p.share(offset * WideChar.SIZE));
	}
	
	/**
	 * Read as UTF-32 or UTF-16 string
	 *
	 * @param length The number of characters to read
	 * @return The Java string
	 * @throws IOException if length exceeds the maximum number of characters in this string
	 */
	String read(int length) throws CharacterCodingException, IOException {
		return read(length, StandardDisplayTables.DEFAULT);
	}
	
	/**
	 * Read as braille string (dotsIO mode)
	 *
	 * @param length The number of characters (braille cells) to read
	 * @return The Unicode braille Java string
	 * @throws UnmappableCharacterException if there are virtual dots in the output and no fallback was specified.
	 * @throws IOException if length exceeds the maximum number of characters in this string
	 */
	String readDots(int length) throws UnmappableCharacterException, IOException {
		return read(length, StandardDisplayTables.UNICODE);
	}
	
	String read(int length, DisplayTable displayTable) throws UnmappableCharacterException, IOException {
		Charset charset = displayTable.asCharset();
		if (decoders == null)
			decoders = new IdentityHashMap<Charset,CharsetDecoder>();
		CharsetDecoder decoder = decoders.get(charset);
		if (decoder == null) {
			decoder = charset.newDecoder()
			                 .onMalformedInput(CodingErrorAction.REPORT)
			                 .onUnmappableCharacter(CodingErrorAction.REPORT);
			decoders.put(charset, decoder);
		}
		return read(length, decoder);
	}
	
	// using CharsetDecoder because behavior of String(byte[]) is undefined when bytes can not be decoded
	private String read(int length, CharsetDecoder decoder) throws UnmappableCharacterException, IOException {
		synchronized (decoder) {
			if (length > length())
				throw new IOException("Maximum length is " + length());
			if (length == 0)
				return "";
			decoder.reset();
			ByteBuffer bytes = ByteBuffer.wrap(getPointer().getByteArray(0, length * WideChar.SIZE));
			CharBuffer chars = CharBuffer.allocate((int)(bytes.limit() * decoder.averageCharsPerByte()));
			for (;;) {
				CoderResult cr = bytes.hasRemaining()
					? decoder.decode(bytes, chars, true)
					: decoder.flush(chars);
				if (cr.isUnderflow())
					break;
				if (cr.isOverflow()) {
					CharBuffer b = CharBuffer.allocate(chars.limit() * 2);
					chars.flip();
					b.put(chars);
					chars = b;
					continue;
				}
				try {
					cr.throwException();
				} catch (MalformedInputException e) {
					throw new RuntimeException("Liblouis coding error", e);
				}
			}
			chars.flip();
			return chars.toString();
		}
	}
	
	/**
	 * Write as UTF-32 or UTF-16 string
	 *
	 * @param value The Java string to write
	 * @return This object
	 * @throws IOException if the supplied value is longer than the available space
	 */
	WideString write(String value) throws IOException {
		return write(value, StandardDisplayTables.DEFAULT);
	}
	
	/**
	 * Write as braille string (dotsIO mode)
	 *
	 * @param value The Unicode braille Java string to write
	 * @return This object
	 * @throws UnmappableCharacterException if the supplied value is not Unicode braille (contains characters outside of the 2800-28FF range)
	 * @throws IOException if the supplied value is longer than the available space
	 */
	WideString writeDots(String value) throws UnmappableCharacterException, IOException {
		return write(value, StandardDisplayTables.UNICODE);
	}
	
	private WideString write(String value, DisplayTable displayTable) throws UnmappableCharacterException, IOException {
		Charset charset = displayTable.asCharset();
		if (encoders == null)
			encoders = new IdentityHashMap<Charset,CharsetEncoder>();
		CharsetEncoder encoder = encoders.get(charset);
		if (encoder == null) {
			encoder = charset.newEncoder()
			                  .onMalformedInput(CodingErrorAction.REPORT)
			                  .onUnmappableCharacter(CodingErrorAction.REPORT);
			encoders.put(charset, encoder);
		}
		return write(value, encoder);
	}
	
	// using CharsetEncoder because behavior of String.getBytes() is undefined when characters can not be encoded
	private WideString write(String value, CharsetEncoder encoder) throws UnmappableCharacterException, IOException {
		synchronized (encoder) {
			// We are sure that the following condition is always met because the write(String,
			// DisplayTable) method is private.
			if (!(encoder.charset() == StandardDisplayTables.DEFAULT.asCharset()
			      || encoder.charset() == StandardDisplayTables.UNICODE.asCharset()))
				throw new IllegalStateException();
			// The following is true for the encoders of both StandardDisplayTables.DEFAULT and
			// StandardDisplayTables.UNICODE.
			int encodedLength = WideChar.SIZE * (WideChar.SIZE == 2
				? value.length()
				: value.codePoints().toArray().length);
			if (encodedLength > length * WideChar.SIZE)
				throw new IOException("Maximum string length is " + length());
			byte[] ba = new byte[encodedLength];
			if (ba.length > 0) {
				ByteBuffer bb = ByteBuffer.wrap(ba);
				CharBuffer cb = CharBuffer.wrap(value);
				encoder.reset();
				CoderResult cr = encoder.encode(cb, bb, true);
				try {
					if (!cr.isUnderflow())
						cr.throwException();
					cr = encoder.flush(bb);
					if (!cr.isUnderflow())
						cr.throwException(); }
				catch (BufferOverflowException e) {
					throw new RuntimeException("invalid Charset", e); }
				if (bb.hasRemaining())
					throw new RuntimeException("invalid Charset");
				getPointer().write(0, ba, 0, ba.length);
			}
			return this;
		}
	}
	
	@Override
	public Pointer getPointer() {
		if (super.getPointer() == null) {
			try {
				setPointer(new Memory(length * WideChar.SIZE)); }
			catch (Exception e) {
				throw new RuntimeException(e); }}
		return super.getPointer();
	}
	
	int length() {
		return length;
	}
	
	WideString substring(int beginIndex) {
		return substring(beginIndex, length);
	}
	
	WideString substring(int beginIndex, int endIndex) {
		if (beginIndex < 0 || endIndex > length || beginIndex > endIndex)
			throw new IndexOutOfBoundsException();
		return new WideString(getPointer(), beginIndex, endIndex - beginIndex);
	}
	
	@Override
	public String toString() {
		try {
			return read(length()); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
	}
}
