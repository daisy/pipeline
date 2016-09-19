package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class NSC_FINDVOICE_DATA extends Structure {
	public int nGender;
	public int nLanguage;
	public int nInitialSampleFreq;
	public int nInitialCoding;
	public int nOutputSampleFreq;
	public int nOutputCoding;
	/** C type : char[255] */
	public byte[] cSpeakerName = new byte[255];
	/** C type : char[255] */
	public byte[] cDisplayName = new byte[255];
	/** C type : char[255] */
	public byte[] cVoiceName = new byte[255];

	public NSC_FINDVOICE_DATA() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("nGender", "nLanguage", "nInitialSampleFreq", "nInitialCoding",
		        "nOutputSampleFreq", "nOutputCoding", "cSpeakerName", "cDisplayName",
		        "cVoiceName");
	}

	/**
	 * @param cSpeakerName C type : char[255]<br>
	 * @param cDisplayName C type : char[255]<br>
	 * @param cVoiceName C type : char[255]
	 */
	public NSC_FINDVOICE_DATA(int nGender, int nLanguage, int nInitialSampleFreq,
	        int nInitialCoding, int nOutputSampleFreq, int nOutputCoding, byte cSpeakerName[],
	        byte cDisplayName[], byte cVoiceName[]) {
		super();
		this.nGender = nGender;
		this.nLanguage = nLanguage;
		this.nInitialSampleFreq = nInitialSampleFreq;
		this.nInitialCoding = nInitialCoding;
		this.nOutputSampleFreq = nOutputSampleFreq;
		this.nOutputCoding = nOutputCoding;
		if ((cSpeakerName.length != this.cSpeakerName.length))
			throw new IllegalArgumentException("Wrong array size !");
		this.cSpeakerName = cSpeakerName;
		if ((cDisplayName.length != this.cDisplayName.length))
			throw new IllegalArgumentException("Wrong array size !");
		this.cDisplayName = cDisplayName;
		if ((cVoiceName.length != this.cVoiceName.length))
			throw new IllegalArgumentException("Wrong array size !");
		this.cVoiceName = cVoiceName;
	}

	public static class ByReference extends NSC_FINDVOICE_DATA implements
	        Structure.ByReference {

	};

	public static class ByValue extends NSC_FINDVOICE_DATA implements Structure.ByValue {

	};
}
