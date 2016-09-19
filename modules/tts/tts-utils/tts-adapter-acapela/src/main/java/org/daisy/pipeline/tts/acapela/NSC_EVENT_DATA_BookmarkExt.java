package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NSC_EVENT_DATA_BookmarkExt extends Structure {
	public int uiSize;
	/** C type : void* */
	public Pointer pUserData;
	/**
	 * bookmark value<br> C type : char[50]
	 */
	public byte[] szVal = new byte[50];
	/** position in bytes in whole audio signal */
	public int uiByteCount;

	public NSC_EVENT_DATA_BookmarkExt() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("uiSize", "pUserData", "szVal", "uiByteCount");
	}

	/**
	 * @param pUserData C type : void*<br>
	 * @param szVal bookmark value<br> C type : char[50]<br>
	 * @param uiByteCount position in bytes in whole audio signal
	 */
	public NSC_EVENT_DATA_BookmarkExt(int uiSize, Pointer pUserData, byte szVal[],
	        int uiByteCount) {
		super();
		this.uiSize = uiSize;
		this.pUserData = pUserData;
		if ((szVal.length != this.szVal.length))
			throw new IllegalArgumentException("Wrong array size !");
		this.szVal = szVal;
		this.uiByteCount = uiByteCount;
	}

	public static class ByReference extends NSC_EVENT_DATA_BookmarkExt implements
	        Structure.ByReference {

	};

	public static class ByValue extends NSC_EVENT_DATA_BookmarkExt implements
	        Structure.ByValue {

	};

	//Added by Daisy Pipeline to allow casts
	public NSC_EVENT_DATA_BookmarkExt(Pointer p) {
		super(p);
	}
}
