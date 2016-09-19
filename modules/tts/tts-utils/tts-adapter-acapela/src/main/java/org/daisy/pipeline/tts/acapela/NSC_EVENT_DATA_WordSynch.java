package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NSC_EVENT_DATA_WordSynch extends Structure {
	public int uiSize;
	/** C type : void* */
	public Pointer pUserData;
	/** position in bytes of first char in text */
	public int uiWordPos;
	/** position in bytes in whole audio signal */
	public int uiByteCount;

	public NSC_EVENT_DATA_WordSynch() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("uiSize", "pUserData", "uiWordPos", "uiByteCount");
	}

	/**
	 * @param pUserData C type : void*<br>
	 * @param uiWordPos position in bytes of first char in text<br>
	 * @param uiByteCount position in bytes in whole audio signal
	 */
	public NSC_EVENT_DATA_WordSynch(int uiSize, Pointer pUserData, int uiWordPos,
	        int uiByteCount) {
		super();
		this.uiSize = uiSize;
		this.pUserData = pUserData;
		this.uiWordPos = uiWordPos;
		this.uiByteCount = uiByteCount;
	}

	public static class ByReference extends NSC_EVENT_DATA_WordSynch implements
	        Structure.ByReference {

	};

	public static class ByValue extends NSC_EVENT_DATA_WordSynch implements Structure.ByValue {

	};
}
