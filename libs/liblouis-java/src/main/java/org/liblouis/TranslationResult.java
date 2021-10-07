package org.liblouis;

import java.io.IOException;
import java.nio.charset.UnmappableCharacterException;

import com.sun.jna.ptr.IntByReference;

public class TranslationResult {
	
	private String braille = null;
	private int[] characterAttributes = null;
	private int[] interCharacterAttributes = null;
	
	TranslationResult(WideCharString outbuf, IntByReference outlen, int[] inputPos,
	                  int[] characterAttributes, int[] interCharacterAttributes,
	                  DisplayTable displayTable) throws DisplayException {
		int len = outlen.getValue();
		try {
			this.braille = outbuf.read(len, displayTable); }
		catch (UnmappableCharacterException e) {
			throw new DisplayException(e.getMessage(), e); }
		catch (IOException e) {
			throw new RuntimeException("should not happen", e); }
		if (characterAttributes != null) {
			this.characterAttributes = new int[len];
			for (int outpos = 0; outpos < len; outpos++)
				this.characterAttributes[outpos] = characterAttributes[inputPos[outpos]]; }
		
		// This is more or less copied from lou_translatePrehyphenated. The difference is that here
		// we work with int arrays instead of byte arrays, and also the nil value is 0 here instead
		// of 48 (character '0').
		if (interCharacterAttributes != null && len > 0) {
			this.interCharacterAttributes = new int[len - 1];
			int inpos = 0;
			for (int outpos = 1; outpos < len; outpos++) {
				int new_inpos = inputPos[outpos];
				if (new_inpos < inpos)
					throw new RuntimeException();
				if (new_inpos > inpos)
					this.interCharacterAttributes[outpos - 1] = interCharacterAttributes[new_inpos - 1];
				else
					this.interCharacterAttributes[outpos - 1] = 0;
				inpos = new_inpos; }}
	}
	
	public String getBraille() {
		return braille;
	}

	public int[] getCharacterAttributes() {
		return characterAttributes;
	}
	
	public int[] getInterCharacterAttributes() {
		return interCharacterAttributes;
	}
}
