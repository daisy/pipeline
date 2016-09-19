package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NSC_SOUND_DATA extends Structure {
	public int uiSize;
	/** C type : unsigned char* */
	public Pointer pSoundBuffer;

	public NSC_SOUND_DATA() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("uiSize", "pSoundBuffer");
	}

	/** @param pSoundBuffer C type : unsigned char* */
	public NSC_SOUND_DATA(int uiSize, Pointer pSoundBuffer) {
		super();
		this.uiSize = uiSize;
		this.pSoundBuffer = pSoundBuffer;
	}

	public static class ByReference extends NSC_SOUND_DATA implements Structure.ByReference {

	};

	public static class ByValue extends NSC_SOUND_DATA implements Structure.ByValue {

	};
}
