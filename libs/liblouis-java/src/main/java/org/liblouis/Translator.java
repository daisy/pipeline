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
	
	/**
	 * @param table The translation table or table list to compile.
	 * @throws CompilationException if the table could not be found or if it
	 * could not be compiled.
	 */
	public Translator(String table) throws CompilationException {
		Louis.log(Logger.Level.DEBUG, "Loading table %s", table);
		if (Louis.getLibrary().lou_getTable(table) == Pointer.NULL)
			throw new CompilationException("Unable to compile table '" + table + "'");
		this.table = table;
	}
	
	/**
	 * @param table The translation table as a URL.
	 * @throws CompilationException if the table could not be compiled.
	 */
	public Translator(URL table) throws CompilationException {
		Louis.log(Logger.Level.DEBUG, "Loading table %s", table);
		this.table = Louis.getTableNameForURL(table);
		if (Louis.getLibrary().lou_getTable(this.table) == Pointer.NULL)
			throw new CompilationException("Unable to compile table '" + table + "'");
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
	 *                 <code>text</code>. May be null.
	 * @param characterAttributes Array with other information about the text that will be passed on
	 *                            to the output. May for example be used for numbering all
	 *                            characters in the input text in order to obtain a full mapping
	 *                            between input and output. Array must have the same length as
	 *                            <code>text</code>. May be null.
	 * @param interCharacterAttributes Array with information about the positions between characters
	 *                                 that will be passed on to the output. May for example be used
	 *                                 to track hyphenation points (e.g. `0` for no hyphenation
	 *                                 point opportunity, `1` for soft hyphen and `2` for zero-width
	 *                                 space). Length must be equal to the <code>text</code> length
	 *                                 minus 1.
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
		if (typeform != null)
			if (typeform.length != text.length())
				throw new IllegalArgumentException("typeform length must be equal to text length");
		if (characterAttributes != null)
			if (characterAttributes.length != text.length())
				throw new IllegalArgumentException("characterAttributes length must be equal to text length");
		if (interCharacterAttributes != null)
			if (interCharacterAttributes.length != text.length() - 1)
				throw new IllegalArgumentException("interCharacterAttributes length must be equal to text length minus 1");
		WideString inbuf;
		try {
			inbuf = getWideCharBuffer("text-in", text.length()).write(text); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		WideString outbuf = getWideCharBuffer("text-out", text.length() * OUTLEN_MULTIPLIER);
		IntByReference inlen = new IntByReference(text.length());
		IntByReference outlen = new IntByReference(outbuf.length());
		int[] inputPos = null;
		if (typeform != null)
			typeform = Arrays.copyOf(typeform, outbuf.length());
		if (characterAttributes != null || interCharacterAttributes != null)
			inputPos = getIntegerBuffer("inputpos", text.length() * OUTLEN_MULTIPLIER);
		int mode = displayTable != StandardDisplayTables.DEFAULT ? 4 : 0;
		if (Louis.getLibrary().lou_translate(table, inbuf, inlen, outbuf, outlen, typeform,
		                                     null, null, inputPos, null, mode) == 0)
			throw new TranslationException("Unable to complete translation");
		return new TranslationResult(outbuf, outlen, inputPos, characterAttributes, interCharacterAttributes, displayTable);
	}
	
	public String backTranslate(String text) throws TranslationException {
		WideString inbuf;
		try {
			inbuf = getWideCharBuffer("text-in", text.length()).write(text); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		WideString outbuf = getWideCharBuffer("text-out", text.length() * OUTLEN_MULTIPLIER);
		IntByReference inlen = new IntByReference(text.length());
		IntByReference outlen = new IntByReference(outbuf.length());
		
		if (Louis.getLibrary().lou_backTranslate(table, inbuf, inlen, outbuf, outlen,
				null, null, null, null, null, 0) == 0)
			throw new TranslationException("Unable to complete translation");
		try {
			return outbuf.read(outlen.getValue()); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
	}
	
	/**
	 * @param text The text to hyphenate. Can be multiple words.
	 * @return The hyphenation points. Possible values are `0` for no hyphenation point, `1` for a
	 *         hyphenation point (soft hyphen), or `2` for a zero-width space (which are inserted
	 *         after hard hyphens). Length is equal to the <code>text</code> length minus 1.
	 */
	public byte[] hyphenate(String text) throws TranslationException {
		WideString inbuf;
		try {
			inbuf = getWideCharBuffer("text-in", text.length()).write(text); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		int inlen = text.length();
		byte[] hyphens = getByteBuffer("hyphens-out", inlen);
		for (int i = 0; i < inlen; i++) hyphens[i] = '0';
		
		// lou_translate handles single words only
		Matcher matcher = Pattern.compile("\\p{L}+").matcher(text);
		byte[] wordHyphens = getByteBuffer("hyphens-word", inlen);
		LouisLibrary louis = Louis.getLibrary();
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			if (louis.lou_hyphenate(table, inbuf.substring(start), end - start, wordHyphens, 0) == 0)
				throw new TranslationException("Unable to complete hyphenation");
			for (int i = 0; i < end - start; i++) hyphens[start + i] = wordHyphens[i]; }
		
		byte[] hyphenPositions = readHyphens(new byte[text.length() - 1], hyphens);
		
		// add a zero-width space after hard hyphens
		matcher = Pattern.compile("[\\p{L}\\p{N}]-(?=[\\p{L}\\p{N}])").matcher(text);
		while (matcher.find())
			hyphenPositions[matcher.start() + 1] = ZWSP;
		return hyphenPositions;
	}
	
	/**
	 * Convert a braille string from either Unicode braille or Liblouis' dotsIO format to the
	 * charset defined by the (display) table.
	 */
	public String display(String braille) throws TranslationException {
		WideString inbuf;
		try {
			inbuf = getWideCharBuffer("text-in", braille.length()).write(braille); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		int length = braille.length();
		WideString outbuf = getWideCharBuffer("text-out", braille.length() * OUTLEN_MULTIPLIER);
		if (Louis.getLibrary().lou_dotsToChar(table, inbuf, outbuf, length, 0) == 0)
			throw new TranslationException("Unable to complete translation");
		try {
			return outbuf.read(length); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
	}
	
	/*
	 * Number by which the input length should be multiplied to calculate
	 * the maximum output length. This default will handle the case where
	 * every input character is undefined in the translation table.
	 */
	private static final int OUTLEN_MULTIPLIER = WideChar.SIZE * 2 + 4;
	
	private static Map<String,WideString> WIDECHAR_BUFFERS = new HashMap<String,WideString>();
	private static Map<String,byte[]> BYTE_BUFFERS = new HashMap<String,byte[]>();
	private static Map<String,int[]> INT_BUFFERS = new HashMap<String,int[]>();
	
	private static WideString getWideCharBuffer(String id, int minCapacity) {
		WideString buffer = WIDECHAR_BUFFERS.get(id);
		if (buffer == null || buffer.length() < minCapacity) {
			buffer = new WideString(minCapacity * 2);
			WIDECHAR_BUFFERS.put(id, buffer); }
		return buffer;
	}
		
	private static byte[] getByteBuffer(String id, int minCapacity) {
		byte[] buffer = BYTE_BUFFERS.get(id);
		if (buffer == null || buffer.length < minCapacity) {
			buffer = new byte[minCapacity * 2];
			BYTE_BUFFERS.put(id, buffer); }
		return buffer;
	}
	
	private static int[] getIntegerBuffer(String id, int minCapacity) {
		int[] buffer = INT_BUFFERS.get(id);
		if (buffer == null || buffer.length < minCapacity) {
			buffer = new int[minCapacity * 2];
			INT_BUFFERS.put(id, buffer); }
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
