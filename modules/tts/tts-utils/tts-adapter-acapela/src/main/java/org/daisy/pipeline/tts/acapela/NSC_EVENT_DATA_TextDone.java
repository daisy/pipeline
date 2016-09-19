package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NSC_EVENT_DATA_TextDone extends Structure {
	public int uiSize;
	/** C type : void* */
	public Pointer pUserData;

	public NSC_EVENT_DATA_TextDone() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("uiSize", "pUserData");
	}

	/** @param pUserData C type : void* */
	public NSC_EVENT_DATA_TextDone(int uiSize, Pointer pUserData) {
		super();
		this.uiSize = uiSize;
		this.pUserData = pUserData;
	}

	public static class ByReference extends NSC_EVENT_DATA_TextDone implements
	        Structure.ByReference {

	};

	public static class ByValue extends NSC_EVENT_DATA_TextDone implements Structure.ByValue {

	};
}
