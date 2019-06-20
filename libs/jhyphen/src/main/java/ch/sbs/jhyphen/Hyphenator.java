package ch.sbs.jhyphen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * The primary API entry point
 * @author Bert Frees
 */
public class Hyphenator {
	
	public static final byte SHY = 1;
	public static final byte ZWSP = 2;
	
	// Don't allocate new memory for each word
	private static ByteBuffer wordHyphens = ByteBuffer.allocate(50);
	
	/**
	 * Maps dictionary files to charsets
	 */
	private final static Map<File,Charset> charsets = new HashMap<File,Charset>();
	
	/**
	 * The hyphenation dictionary
	 */
	private final Pointer dictionary;
	
	/**
	 * The encoding of the hyphenation dictionary, e.g. ISO-8859-1 for German
	 */
	private final Charset charset;
	
	/**
	 * Default constructor
	 * @param dictionaryFile The path to the hyphenation dictionary file,
	 *        e.g. /usr/share/hyphen/hyph_de_DE.dic
	 * @throws FileNotFoundException if the dictionary file cannot be found.
	 * @throws CompilationException if the encoding of the file is not supported.
	 */
	public Hyphenator(File dictionaryFile) throws CompilationException, FileNotFoundException {
		if (!dictionaryFile.exists())
			throw new FileNotFoundException("Dictionary file at " +
					dictionaryFile.getAbsolutePath() + " doesn't exist.");
		try {
			charset = getCharset(dictionaryFile);
		} catch(UnsupportedCharsetException e) {
			throw new CompilationException(e);
		}
		dictionary = Hyphen.getLibrary().hnj_hyphen_load(dictionaryFile.getAbsolutePath());
	}
	
	/**
	 * Returns the fully hyphenated string.
	 * The given hyphen characters are inserted at all possible hyphenation points.
	 * @param text The string to be hyphenated
	 * @param shy The character to be used as soft hyphen.
	 * @param zwsp The character to be used as zero-width space.
	 * @return The hyphenated string
	 * @throws StandardHyphenationException if the word contains non-standard hyphenation
	 *         points, in which case the method {@link #hyphenate(String, int, Character,
	 *         Character)} should be used instead.
	 */
	public String hyphenate(String text, Character shy, Character zwsp) throws StandardHyphenationException {
		if (shy == null && zwsp == null)
			return text;
		byte[] hyphens = hyphenate(text);
		StringBuffer hyphenatedText = new StringBuffer();
		int i;
		for (i = 0; i < hyphens.length; i++) {
			hyphenatedText.append(text.charAt(i));
			if (shy != null && hyphens[i] == SHY)
				hyphenatedText.append(shy);
			else if (zwsp != null && hyphens[i] == ZWSP)
				hyphenatedText.append(zwsp);
		}
		hyphenatedText.append(text.charAt(i));
		return hyphenatedText.toString();
	}
	
	/**
	 * Returns all possible hyphenation opportunities within a string
	 * @param text The string to be hyphenated
	 * @return A byte array which represents the break opportunities. The length of the
	 *         hyphen array is the length of the input string minus 1. A hyphen at index i
	 *         corresponds to characters i and i+1 of the string. Possible values are `0`
	 *         for no hyphenation point, `1` for a hyphenation point (soft hyphen), or `2`
	 *         for a zero-width space (which are inserted after hard hyphens).
	 * @throws StandardHyphenationException if the text contains non-standard hyphenation
	 *         points, in which case the method {@link #hyphenate(String, int, Character,
	 *         Character)} should be used instead.
	 */
	public byte[] hyphenate(String text) throws StandardHyphenationException  {
		
		// TODO: what if word already contains soft hyphens?
		
		Matcher matcher = Pattern.compile("['\\p{L}]+").matcher(text);
		StringBuffer hyphenBuffer = new StringBuffer();
		int pos = 0;
		
		// iterate words
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			while (pos++ < start)
				hyphenBuffer.append('0');
			String word = text.substring(start, end);
			
			// libhyphen requires that word is lowercase
			word = word.toLowerCase();
			byte[] wordBytes = encode(word);
			int wordSize = wordBytes.length;
			if (wordSize > wordHyphens.capacity())
				wordHyphens = ByteBuffer.allocate(wordSize * 2);
			PointerByReference repPointer = new PointerByReference(Pointer.NULL);
			PointerByReference posPointer = new PointerByReference(Pointer.NULL);
			PointerByReference cutPointer = new PointerByReference(Pointer.NULL);
			Hyphen.getLibrary().hnj_hyphen_hyphenate2(dictionary, wordBytes, wordSize, wordHyphens, null,
			                                          repPointer, posPointer, cutPointer);
			if (repPointer.getValue() != Pointer.NULL)
				throw new StandardHyphenationException("Text contains non-standard hyphenation points.");
				
			// TODO: assert that last element of wordHyphens is not a hyphen
			hyphenBuffer.append(new String(wordHyphens.array(), 0, word.length()));
			pos = end;
		}
		while (pos < text.length()) {
			hyphenBuffer.append('0');
			pos++;
		}
		hyphenBuffer.deleteCharAt(pos-1);
		byte[] hyphens = new byte[hyphenBuffer.length()];
		CharacterIterator iter = new StringCharacterIterator(hyphenBuffer.toString());
		int i = 0;
		for(char c = iter.first(); c != CharacterIterator.DONE; c = iter.next())
			hyphens[i++] = (c & 1) > 0 ? SHY : 0;
		
		// add a zero-width space after hard hyphens ("-" followed and preceded by a letter or number)
		matcher = Pattern.compile("[\\p{L}\\p{N}]-(?=[\\p{L}\\p{N}])").matcher(text);
		while (matcher.find())
			hyphens[matcher.start()+1] = ZWSP;
		return hyphens;
	}
	
	/* For testing */
	protected String hyphenate(String text, int lineLength, Character hyphen, Character noHyphen) {
		Break b = hyphenate(text, lineLength);
		String hyphenatedText = "";
		hyphenatedText += b.getText().substring(0, b.getBreakPosition());
		if (b.hasHyphen()) {
			if (hyphen != null)
				hyphenatedText += hyphen; }
		else if (noHyphen != null)
			hyphenatedText += noHyphen;
		hyphenatedText += b.getText().substring(b.getBreakPosition());
		return hyphenatedText;
	}
	
	/**
	 * Hyphenate a string at a preferred position.
	 * @param text The string to be hyphenated.
	 * @param lineLength The maximum number of characters the string may contain before
	 *                   the break.
	 * @return The hyphenated string as a tuple of a string, a break position, and a break
	 *         type. In the case of standard hyphenation the returned string is equal to
	 *         the input string, in the case of non-standard hyphenation it is not
	 *         equal. The break position denotes the number of characters before the
	 *         break. It is less than or equal to 'lineLength', and may be 0. The break
	 *         type is a boolean that denotes whether a hyphen character is to be inserted
	 *         before the line break or not. Only positions within words are considered
	 *         when searching for break opportunities. SHY or ZWSP characters or spaces in
	 *         the input are not considered. Breaking at these points is left up to the
	 *         caller.
	 */
	public Break hyphenate(String text, int lineLength) {
		if (text.length() <= lineLength)
			return new NoBreak(text);
		Break lastOkBreak = null;
		
		// iterate words
		int p = 0;
		boolean isWord = false;
	  outer: for (String s : splitInclDelimiter(text, Pattern.compile("['\\p{L}]+"))) {
			if (isWord) {
				String word = s;
				int wordStart = p;
				
				// libhyphen requires that word is lowercase
				word = word.toLowerCase();
				byte[] wordBytes = encode(word);
				int wordSize = wordBytes.length;
				if (wordSize > wordHyphens.capacity())
					wordHyphens = ByteBuffer.allocate(wordSize * 2);
				PointerByReference repPointer = new PointerByReference(Pointer.NULL);
				PointerByReference posPointer = new PointerByReference(Pointer.NULL);
				PointerByReference cutPointer = new PointerByReference(Pointer.NULL);
				Hyphen.getLibrary().hnj_hyphen_hyphenate2(dictionary, wordBytes, wordSize, wordHyphens, null,
				                                          repPointer, posPointer, cutPointer);
			
				// TODO: assert that last element of wordHyphens is not a hyphen
				String hyphenString = new String(wordHyphens.array(), 0, word.length());
				String[] rep;
				int[] pos;
				int[] cut;
				if (repPointer.getValue() != Pointer.NULL
				    && posPointer.getValue() != Pointer.NULL
				    && cutPointer.getValue() != Pointer.NULL) {
				
					// will this also free the memory later or do I need to do this explicitly?
					rep = repPointer.getValue().getStringArray(0L, wordSize, charset.name());
					pos = posPointer.getValue().getIntArray(0, wordSize);
					cut = cutPointer.getValue().getIntArray(0, wordSize); }
				else {
					rep = new String[wordSize];
					pos = new int[wordSize];
					cut = new int[wordSize]; }
				CharacterIterator hyphens = new StringCharacterIterator(hyphenString);
				int posInWord = 0;
				for (char c = hyphens.first(); c != CharacterIterator.DONE; c = hyphens.next()) {
					posInWord++;
					if ((c & 1) > 0) {
						Break b;
						if (rep != null && rep[posInWord - 1] != null)
							b = new NonStandardBreak(
								text, wordStart + posInWord - pos[posInWord - 1], cut[posInWord - 1], rep[posInWord - 1]);
						else
							b = new StandardBreak(text, wordStart + posInWord);
						if (b.getBreakPosition() > lineLength)
							break outer;
						if (lastOkBreak == null || b.compareTo(lastOkBreak) > 0)
							lastOkBreak = b;
					}
				}
			} else if (!s.isEmpty()){
				Matcher m = Pattern.compile("([\\p{L}\\p{N}]|^)-(?=([\\p{L}\\p{N}]|$))").matcher(s);
				while (m.find()) {
					String g = m.group();
					if (g.charAt(0) == '-' && p == 0)
						continue;
					if (g.charAt(g.length() - 1) == '-' && p + s.length() == text.length())
						continue;
					Break b = new BreakAfterHyphen(text, p + m.start() + g.indexOf('-') + 1);
					if (b.getBreakPosition() > lineLength)
						break outer;
					if (lastOkBreak == null || b.compareTo(lastOkBreak) > 0)
						lastOkBreak = b;
				}
			}
			p += s.length();
			isWord = !isWord;
		}
		if (lastOkBreak != null)
			return lastOkBreak;
		else
			return new EmptyLine(text);
	}
	
	public interface Break extends Comparable<Break> {
		public String getText();
		public int getBreakPosition();
		public boolean hasHyphen();
	}
	
	private static abstract class ABreak implements Break {
		
		protected String text;
		protected int position;
		protected boolean hyphen;
		
		public String getText() {
			return text;
		}
		
		public int getBreakPosition() {
			return position;
		}
		
		public boolean hasHyphen() {
			return hyphen;
		}
		
		public int compareTo(Break that) {
			if (this.getBreakPosition() > that.getBreakPosition())
				return 1;
			else if (this.getBreakPosition() < that.getBreakPosition())
				return -1;
			else if (!this.hasHyphen() && that.hasHyphen())
				return 1;
			else if (this.hasHyphen() && !that.hasHyphen())
				return -1;
			else
				return 0;
		}
		
		@Override
		public String toString() {
			String s = getText().substring(0, getBreakPosition());
			if (hasHyphen())
				s += "-";
			return s;
		}
	}
	
	private static class NoBreak extends ABreak {
		private NoBreak(String text) {
			this.text = text;
			position = text.length();
			hyphen = false;
		}
	}
	
	private static class EmptyLine extends ABreak {
		private EmptyLine(String text) {
			this.text = text;
			position = 0;
			hyphen = false;
		}
	}
	
	private static class BreakAfterHyphen extends ABreak {
		private BreakAfterHyphen(String text, int position) {
			this.text = text;
			this.position = position;
			hyphen = false;
		}
	}
	
	private static class StandardBreak extends ABreak {
		private StandardBreak(String text, int position) {
			this.text = text;
			this.position = position;
			hyphen = true;
		}
	}
	
	private static class NonStandardBreak extends ABreak {
		private NonStandardBreak(String text, int repStart, int repLength, String repString) {
			int breakPosInRep = repString.indexOf('=');
			if (breakPosInRep < 0)
				throw new HyphenationException("Table error");
			this.text = text.substring(0, repStart)
				+ repString.substring(0, breakPosInRep)
				+ repString.substring(breakPosInRep + 1)
				+ text.substring(repStart + repLength);
			position = repStart + breakPosInRep;
			hyphen = true;
		}
	}
	
	/**
	 * Free memory
	 */
	public void close() {
		Hyphen.getLibrary().hnj_hyphen_free(dictionary);
	}
	
	/**
	 * Encodes an input string into a byte array using the same encoding as the hyphenation dictionary.
	 * A dummy character "?" (0x3F in case of ISO-8859-1) is inserted when a character can not be encoded.
	 * @param str The string to be encoded
	 * @return A byte array
	 */
	private byte[] encode(String str) {
		return charset.encode(str).array();
	}
	
	/**
	 * Reads the first line of the dictionary file which is the encoding
	 * @param dictionaryFile The dictionary file
	 * @return The encoding
	 * @throws FileNotFoundException if the dictionary file cannot be found.
	 * @throws UnsupportedCharsetException if the encoding of the file is not supported.
	 */
	private static Charset getCharset(File dictionaryFile)
			throws UnsupportedCharsetException, FileNotFoundException {
		
		Charset cs = charsets.get(dictionaryFile);
		if (cs == null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(dictionaryFile));
				String charsetName = reader.readLine();
				charsetName = charsetName.replaceAll("\\s+", "");
				cs = Charset.forName(charsetName);
				charsets.put(dictionaryFile, cs);
			} catch (IOException e) {
				throw new RuntimeException("Could not read first line of file");
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return cs;
	}
	
	private static Iterable<String> splitInclDelimiter(final String text, final Pattern delimiterPattern) {
		return new Iterable<String>() {
			public final Iterator<String> iterator() {
				return new Iterator<String>() {
					Matcher m = delimiterPattern.matcher(text);
					int i = 0;
					String nextNext = null;
					private String computeNext() {
						if (nextNext != null) {
							String n = nextNext;
							nextNext = null;
							return n; }
						if (m.find()) {
							String n = text.substring(i, m.start());
							nextNext = m.group();
							i = m.end();
							return n; }
						else if (i >= 0) {
							String n = text.substring(i);
							i = -1;
							return n; }
						else
							return null;
					}
					String next = null;
					boolean done = false;
					public boolean hasNext() {
						if (done)
							return false;
						if (next != null)
							return true;
						next = computeNext();
						if (next == null)
							done = true;
						return hasNext();
					}
					public String next() throws NoSuchElementException {
						if (done)
							throw new NoSuchElementException();
						if (next != null) {
							String n = next;
							next = null;
							return n; }
						next = computeNext();
						if (next == null)
							done = true;
						return next();
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
}
