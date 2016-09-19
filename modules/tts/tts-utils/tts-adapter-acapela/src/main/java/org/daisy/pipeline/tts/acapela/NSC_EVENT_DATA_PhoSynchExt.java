package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NSC_EVENT_DATA_PhoSynchExt extends Structure {
	public int uiSize;
	/** C type : void* */
	public Pointer pUserData;
	/**
	 * Acapela phoneme<br> C type : char[2 * 2 + 1]
	 */
	public byte[] cEngine_Phoneme = new byte[2 * 2 + 1];
	/**
	 * IPA phoneme<br> C type : unsigned short[2]
	 */
	public short[] uiIpa_Phoneme = new short[2];
	/** Viseme */
	public short uiViseme;
	/** phoneme duration in ms */
	public int uiDuration;
	/** position in bytes in whole audio signal */
	public int uiByteCount;

	public NSC_EVENT_DATA_PhoSynchExt() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("uiSize", "pUserData", "cEngine_Phoneme", "uiIpa_Phoneme",
		        "uiViseme", "uiDuration", "uiByteCount");
	}

	/**
	 * @param pUserData C type : void*<br>
	 * @param cEngine_Phoneme Acapela phoneme<br> C type : char[2 * 2 + 1]<br>
	 * @param uiIpa_Phoneme IPA phoneme<br> C type : unsigned short[2]<br>
	 * @param uiViseme Viseme<br>
	 * @param uiDuration phoneme duration in ms<br>
	 * @param uiByteCount position in bytes in whole audio signal
	 */
	public NSC_EVENT_DATA_PhoSynchExt(int uiSize, Pointer pUserData, byte cEngine_Phoneme[],
	        short uiIpa_Phoneme[], short uiViseme, int uiDuration, int uiByteCount) {
		super();
		this.uiSize = uiSize;
		this.pUserData = pUserData;
		if ((cEngine_Phoneme.length != this.cEngine_Phoneme.length))
			throw new IllegalArgumentException("Wrong array size !");
		this.cEngine_Phoneme = cEngine_Phoneme;
		if ((uiIpa_Phoneme.length != this.uiIpa_Phoneme.length))
			throw new IllegalArgumentException("Wrong array size !");
		this.uiIpa_Phoneme = uiIpa_Phoneme;
		this.uiViseme = uiViseme;
		this.uiDuration = uiDuration;
		this.uiByteCount = uiByteCount;
	}

	public static class ByReference extends NSC_EVENT_DATA_PhoSynchExt implements
	        Structure.ByReference {

	};

	public static class ByValue extends NSC_EVENT_DATA_PhoSynchExt implements
	        Structure.ByValue {

	};
}
