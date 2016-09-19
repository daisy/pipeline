package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NSC_EVENT_DATA_Bookmark extends Structure {
	public int uiSize;
	/** C type : void* */
	public Pointer pUserData;
	/** bookmark value */
	public int uiVal;
	/** position in bytes in whole audio signal */
	public int uiByteCount;

	public NSC_EVENT_DATA_Bookmark() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("uiSize", "pUserData", "uiVal", "uiByteCount");
	}

	/**
	 * @param pUserData C type : void*<br>
	 * @param uiVal bookmark value<br>
	 * @param uiByteCount position in bytes in whole audio signal
	 */
	public NSC_EVENT_DATA_Bookmark(int uiSize, Pointer pUserData, int uiVal, int uiByteCount) {
		super();
		this.uiSize = uiSize;
		this.pUserData = pUserData;
		this.uiVal = uiVal;
		this.uiByteCount = uiByteCount;
	}

	public static class ByReference extends NSC_EVENT_DATA_Bookmark implements
	        Structure.ByReference {

	};

	public static class ByValue extends NSC_EVENT_DATA_Bookmark implements Structure.ByValue {

	};

	//Added by Daisy Pipeline to allow casts
	public NSC_EVENT_DATA_Bookmark(Pointer p) {
		super(p);
	}
}
