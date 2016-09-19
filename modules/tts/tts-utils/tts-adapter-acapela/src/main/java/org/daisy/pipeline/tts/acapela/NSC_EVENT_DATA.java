package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class NSC_EVENT_DATA extends Structure {
	public int uiSize;
	/** C type : unsigned char[1] */
	public byte[] bData = new byte[1];

	public NSC_EVENT_DATA() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("uiSize", "bData");
	}

	/** @param bData C type : unsigned char[1] */
	public NSC_EVENT_DATA(int uiSize, byte bData[]) {
		super();
		this.uiSize = uiSize;
		if ((bData.length != this.bData.length))
			throw new IllegalArgumentException("Wrong array size !");
		this.bData = bData;
	}

	public static class ByReference extends NSC_EVENT_DATA implements Structure.ByReference {

	};

	public static class ByValue extends NSC_EVENT_DATA implements Structure.ByValue {

	};
}
