package org.liblouis;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.liblouis.DisplayTable.StandardDisplayTables;
import org.liblouis.Louis.LouisLibrary;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public class Translator {
	
	public static final byte SHY = 1;
	public static final byte ZWSP = 2;
	
	private final String table;
	private DisplayTable displayTable = null;
	
	/**
	 * @param table The translation table or table list to compile.
	 * @throws CompilationException if the table could not be found or if it
	 * could not be compiled.
	 */
	public Translator(String table) throws CompilationException {
		Louis.log(Logger.Level.DEBUG, "Loading table %s", table);
		synchronized (Louis.errors) {
			Louis.errors.clear();
			if (Louis.getLibrary().lou_getTable(table) == Pointer.NULL)
				throw new CompilationException("Unable to compile table '" + table + "'", Louis.errors);
		}
		this.table = table;
	}
	
	/**
	 * @param table The translation table as a URL.
	 * @throws CompilationException if the table could not be compiled.
	 */
	public Translator(URL table) throws CompilationException {
		Louis.log(Logger.Level.DEBUG, "Loading table %s", table);
		this.table = Louis.getTableNameForURL(table);
		synchronized (Louis.errors) {
			Louis.errors.clear();
			if (Louis.getLibrary().lou_getTable(this.table) == Pointer.NULL)
				throw new CompilationException("Unable to compile table '" + table + "'", Louis.errors);
		}
	}
	
	/**
	 * @param query A table query
	 * @throws IllegalArgumentException if the query does not use the right syntax.
	 * @throws CompilationException if no match could be found or if the matched table could not be compiled.
	 */
	public static Translator find(String query) throws IllegalArgumentException, CompilationException {
		try {
			return Table.find(query).getTranslator();
		} catch (NoSuchElementException e) {
			throw new CompilationException(e);
		}
	}
	
	public String getTable() {
		return table;
	}
	
	private Set<Typeform> typeforms = null;
	
	public Set<Typeform> getSupportedTypeforms() {
		if (typeforms == null) {
			typeforms = new HashSet<Typeform>();
			short value = 1;
			for (String emphClass : Louis.getLibrary().lou_getEmphClasses(table)) {
				typeforms.add(new Typeform(emphClass, value, this));
				value *= 2;
			}
			typeforms = Collections.unmodifiableSet(typeforms);
		}
		return typeforms;
	}
	
	/**
	 * @param text The text to translate.
	 * @param typeform Array with typeform information about the text. Must have the same length as
	 *                 <code>text</code> (number of code points). May be null.
	 * @param characterAttributes Array with other information about the text that will be passed on
	 *                            to the output. May for example be used for numbering all
	 *                            characters in the input text in order to obtain a full mapping
	 *                            between input and output. Array must have the same length as
	 *                            <code>text</code> (number of code points). May be null.
	 * @param interCharacterAttributes Array with information about the positions between characters
	 *                                 that will be passed on to the output. May for example be used
	 *                                 to track hyphenation points (e.g. `0` for no hyphenation
	 *                                 point opportunity, `1` for soft hyphen and `2` for zero-width
	 *                                 space). Length must be equal to the <code>text</code> length
	 *                                 (number of code points) minus 1.
	 * @return A TranslationResult containing the braille translation, the output character
	 *         attributes (or <code>null</code> if <code>characterAttributes</code> was
	 *         <code>null</code>), and the output inter-character attributes (or <code>null</code>
	 *         if <code>interCharacterAttributes</code> was <code>null</code>).
	 * @throws TranslationException if the translation could not be completed.
	 * @throws DisplayException if the braille could not be encoded (due to virtual dots).
	 */
	public TranslationResult translate(String text,
	                                   Typeform[] typeform,
	                                   int[] characterAttributes,
	                                   int[] interCharacterAttributes)
			throws TranslationException, DisplayException {
		return translate(text, typeform, characterAttributes, interCharacterAttributes, StandardDisplayTables.DEFAULT);
	}
	
	private TranslationResult translate(String text,
	                                    short[] typeform,
	                                    int[] characterAttributes,
	                                    int[] interCharacterAttributes)
			throws TranslationException, DisplayException {
		return translate(text, typeform, characterAttributes, interCharacterAttributes, StandardDisplayTables.DEFAULT);
	}
	
	/**
	 * @param displayTable The display table used to encode the braille.
	 */
	public TranslationResult translate(String text,
	                                   Typeform[] typeform,
	                                   int[] characterAttributes,
	                                   int[] interCharacterAttributes,
	                                   DisplayTable displayTable)
			throws TranslationException, DisplayException {
		short[] tf = null;
		if (typeform != null) {
			tf = new short[typeform.length];
			for (int i = 0; i < typeform.length; i++) {
				if (typeform[i] == null)
					tf[i] = 0;
				else if (typeform[i].table != null && !typeform[i].table.equals(this))
					throw new TranslationException("Can not use a typeform defined in another table.");
				else tf[i] = typeform[i].value;
			}
		}
		return translate(text, tf, characterAttributes, interCharacterAttributes, displayTable);
	}
	
	private TranslationResult translate(String text,
	                                    short[] typeform,
	                                    int[] characterAttributes,
	                                    int[] interCharacterAttributes,
	                                    DisplayTable displayTable)
			throws TranslationException, DisplayException {
		int textLength = text.codePoints().toArray().length;
		if (WideChar.SIZE == 2 && textLength != text.length()) {
			// This means the Java char array contains surrogate pairs, so the UCS-2 encoded string
			// that is sent to Liblouis will also contain surrogate pairs.
			textLength = text.length();
			// Because Liblouis is unaware of surrogate pairs, handling the "typeform",
			// "characterAttributes" and "interCharacterAttributes" arguments correctly becomes a
			// bit of a challenge. For now we simply don't support it.
			if (typeform != null)
				throw new IllegalArgumentException(
					"Unicode characters above U+FFFF are not supported when typeform is specified");
			if (characterAttributes != null)
				throw new IllegalArgumentException(
					"Unicode characters above U+FFFF are not supported when characterAttributes is specified");
			if (interCharacterAttributes != null)
				throw new IllegalArgumentException(
					"Unicode characters above U+FFFF are not supported when interCharacterAttributes is specified");
		}
		if (typeform != null)
			if (typeform.length != textLength)
				throw new IllegalArgumentException(
					"typeform length must be equal to the text length (number of code points)");
		if (characterAttributes != null)
			if (characterAttributes.length != textLength)
				throw new IllegalArgumentException(
					"characterAttributes length must be equal to text length (number of code points)");
		if (interCharacterAttributes != null)
			if (interCharacterAttributes.length != textLength - 1)
				throw new IllegalArgumentException(
					"interCharacterAttributes length must be equal to text length (number of code points) minus 1");
		WideCharString inbuf;
		try {
			inbuf = getWideCharBuffer("text-in", textLength).write(text); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		WideCharString outbuf = getWideCharBuffer("text-out", textLength * OUTLEN_MULTIPLIER);
		IntByReference inlen = new IntByReference(textLength);
		IntByReference outlen = new IntByReference(outbuf.length());
		int[] inputPos = null;
		if (typeform != null)
			typeform = Arrays.copyOf(typeform, outbuf.length());
		if (characterAttributes != null || interCharacterAttributes != null)
			inputPos = getIntegerBuffer("inputpos", textLength * OUTLEN_MULTIPLIER);
		int mode = displayTable.getMode().value();
		synchronized (Louis.errors) {
			Louis.errors.clear();
			if (Louis.getLibrary().lou_translate(table, inbuf, inlen, outbuf, outlen, typeform,
			                                     null, null, inputPos, null, mode) == 0)
				throw new TranslationException("Unable to complete translation", Louis.errors);
		}
		return new TranslationResult(outbuf, outlen, inputPos, characterAttributes, interCharacterAttributes, displayTable);
	}
	
	public String backTranslate(String text) throws TranslationException {
		int textLength = WideChar.SIZE == 2
			? text.length()
			: text.codePoints().toArray().length;
		WideCharString inbuf;
		try {
			inbuf = getWideCharBuffer("text-in", textLength).write(text); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		WideCharString outbuf = getWideCharBuffer("text-out", textLength * OUTLEN_MULTIPLIER);
		IntByReference inlen = new IntByReference(textLength);
		IntByReference outlen = new IntByReference(outbuf.length());
		synchronized (Louis.errors) {
			Louis.errors.clear();
			if (Louis.getLibrary().lou_backTranslate(table, inbuf, inlen, outbuf, outlen,
					null, null, null, null, null, 0) == 0)
				throw new TranslationException("Unable to complete translation", Louis.errors);
		}
		try {
			return outbuf.read(outlen.getValue()); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
	}
	
	/**
	 * @param text The text to hyphenate. Can be multiple words.
	 * @return The hyphenation points. Possible values are `0` for no hyphenation point, `1` for a
	 *         hyphenation point (soft hyphen), or `2` for a zero-width space (which are inserted
	 *         after hard hyphens). Length is equal to the <code>text</code> length (number of code
	 *         points) minus 1.
	 */
	public byte[] hyphenate(String text) throws TranslationException {
		int inlen = text.codePoints().toArray().length;
		if (WideChar.SIZE == 2 && inlen != text.length()) {
			// This means the Java char array contains surrogate pairs, so the UCS-2 encoded string
			// that is sent to Liblouis will also contain surrogate pairs. Because Liblouis is
			// unaware of surrogate pairs, hyphenation becomes a bit of a challenge. For now we
			// simply don't support it.
			throw new IllegalArgumentException("Unicode characters above U+FFFF are not supported");
		}
		WideCharString inbuf;
		try {
			inbuf = getWideCharBuffer("text-in", inlen).write(text); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		byte[] hyphens = getByteBuffer("hyphens-out", inlen);
		for (int i = 0; i < inlen; i++) hyphens[i] = '0';
		
		// lou_translate handles single words only
		Matcher matcher = Pattern.compile("\\p{L}+").matcher(text);
		byte[] wordHyphens = getByteBuffer("hyphens-word", inlen);
		LouisLibrary louis = Louis.getLibrary();
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			synchronized (Louis.errors) {
				Louis.errors.clear();
				if (louis.lou_hyphenate(table, inbuf.substring(start), end - start, wordHyphens, 0) == 0)
					throw new TranslationException("Unable to complete hyphenation", Louis.errors);
			}
			for (int i = 0; i < end - start; i++) hyphens[start + i] = wordHyphens[i]; }
		
		byte[] hyphenPositions = readHyphens(new byte[inlen - 1], hyphens);
		
		// add a zero-width space after hard hyphens
		matcher = Pattern.compile("[\\p{L}\\p{N}]-(?=[\\p{L}\\p{N}])").matcher(text);
		while (matcher.find())
			hyphenPositions[matcher.start() + 1] = ZWSP;
		return hyphenPositions;
	}

	/**
	 * Use this translation table as a display table.
	 */
	public DisplayTable asDisplayTable() {
		if (displayTable == null)
			displayTable = DisplayTable.fromTable(table);
		return displayTable;
	}
	
	/*
	 * Number by which the input length should be multiplied to calculate
	 * the maximum output length. This default will handle the case where
	 * every input character is undefined in the translation table.
	 */
	private static final int OUTLEN_MULTIPLIER = WideChar.SIZE * 2 + 4;
	
	private static class Buffers {
		Map<String,WideCharString> WIDECHAR_BUFFERS = new HashMap<String,WideCharString>();
		Map<String,byte[]> BYTE_BUFFERS = new HashMap<String,byte[]>();
		Map<String,int[]> INT_BUFFERS = new HashMap<String,int[]>();
	}
	
	private static ThreadLocal<Buffers> buffers = new ThreadLocal<Buffers>() {
			@Override
			protected Buffers initialValue() {
				return new Buffers();
			}
		};
	
	static WideCharString getWideCharBuffer(String id, int minCapacity) {
		WideCharString buffer = buffers.get().WIDECHAR_BUFFERS.get(id);
		if (buffer == null || buffer.length() < minCapacity) {
			buffer = new WideCharString(minCapacity * 2);
			buffers.get().WIDECHAR_BUFFERS.put(id, buffer); }
		return buffer;
	}
		
	private static byte[] getByteBuffer(String id, int minCapacity) {
		byte[] buffer = buffers.get().BYTE_BUFFERS.get(id);
		if (buffer == null || buffer.length < minCapacity) {
			buffer = new byte[minCapacity * 2];
			buffers.get().BYTE_BUFFERS.put(id, buffer); }
		return buffer;
	}
	
	private static int[] getIntegerBuffer(String id, int minCapacity) {
		int[] buffer = buffers.get().INT_BUFFERS.get(id);
		if (buffer == null || buffer.length < minCapacity) {
			buffer = new int[minCapacity * 2];
			buffers.get().INT_BUFFERS.put(id, buffer); }
		return buffer;
	}

	/*
	 * Convert a hyphen array from the form [0,1,0] to the form ['0','0','1','0']
	 */
	@SuppressWarnings("unused")
	private static byte[] writeHyphens(byte[] hyphenPositions, byte[] buffer) {
		buffer[0] = '0';
		for (int i = 0; i < hyphenPositions.length; i++)
			buffer[i+1] = (byte)(hyphenPositions[i] + 48);
		return buffer;
	}
	
	/*
	 * Convert a hyphen array from the form ['0','0','1','0'] to the form [0,1,0]
	 */
	private static byte[] readHyphens(byte[] hyphenPositions, byte[] buffer) {
		for (int i = 0; i < hyphenPositions.length; i++)
			hyphenPositions[i] = (byte)(buffer[i+1] - 48);
		return hyphenPositions;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "{table=" + table + "}";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash + table.hashCode();
		return hash;
	}
	
	@Override
	public boolean equals(Object object) {
		if (this == object)
			return true;
		if (object == null)
			return false;
		if (object.getClass() != Translator.class)
			return false;
		Translator that = (Translator)object;
		if (!this.table.equals(that.table))
			return false;
		return true;
	}
}
