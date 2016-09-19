package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public class NSC_SRVINFO_DATA extends Structure {
	public int nServerType;
	public int nServerStatus;
	public int nServerVersion;
	public int nMaxNbVoice;
	public int nAuthRateCtrl;
	public int nAutMaxNbChannel;
	public int nAuthMaxRTRate;
	public int nCurRTRate;
	public int nCurNbChannel;

	public NSC_SRVINFO_DATA() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("nServerType", "nServerStatus", "nServerVersion", "nMaxNbVoice",
		        "nAuthRateCtrl", "nAutMaxNbChannel", "nAuthMaxRTRate", "nCurRTRate",
		        "nCurNbChannel");
	}

	public NSC_SRVINFO_DATA(int nServerType, int nServerStatus, int nServerVersion,
	        int nMaxNbVoice, int nAuthRateCtrl, int nAutMaxNbChannel, int nAuthMaxRTRate,
	        int nCurRTRate, int nCurNbChannel) {
		super();
		this.nServerType = nServerType;
		this.nServerStatus = nServerStatus;
		this.nServerVersion = nServerVersion;
		this.nMaxNbVoice = nMaxNbVoice;
		this.nAuthRateCtrl = nAuthRateCtrl;
		this.nAutMaxNbChannel = nAutMaxNbChannel;
		this.nAuthMaxRTRate = nAuthMaxRTRate;
		this.nCurRTRate = nCurRTRate;
		this.nCurNbChannel = nCurNbChannel;
	}

	public static class ByReference extends NSC_SRVINFO_DATA implements Structure.ByReference {

	};

	public static class ByValue extends NSC_SRVINFO_DATA implements Structure.ByValue {

	};
}
