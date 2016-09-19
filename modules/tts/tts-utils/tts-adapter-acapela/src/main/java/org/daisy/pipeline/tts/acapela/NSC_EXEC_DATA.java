package org.daisy.pipeline.tts.acapela;

import java.util.Arrays;
import java.util.List;

import org.daisy.pipeline.tts.acapela.NscubeLibrary.PNSC_FNSPEECH_DATA;
import org.daisy.pipeline.tts.acapela.NscubeLibrary.PNSC_FNSPEECH_EVENT;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public class NSC_EXEC_DATA extends Structure {
	/** C type : PNSC_FNSPEECH_DATA* */
	public PNSC_FNSPEECH_DATA pfnSpeechData;
	/** C type : PNSC_FNSPEECH_EVENT* */
	public PNSC_FNSPEECH_EVENT pfnSpeechEvent;
	public NativeLong ulEventFilter;
	public int bEventSynchroReq;
	/** C type : NSC_SOUND_DATA */
	public NSC_SOUND_DATA vsSoundData;
	/** C type : void* */
	public Pointer pAppInstanceData;

	public NSC_EXEC_DATA() {
		super();
	}

	protected List<?> getFieldOrder() {
		return Arrays.asList("pfnSpeechData", "pfnSpeechEvent", "ulEventFilter",
		        "bEventSynchroReq", "vsSoundData", "pAppInstanceData");
	}

	/**
	 * @param pfnSpeechData C type : PNSC_FNSPEECH_DATA*<br>
	 * @param pfnSpeechEvent C type : PNSC_FNSPEECH_EVENT*<br>
	 * @param vsSoundData C type : NSC_SOUND_DATA<br>
	 * @param pAppInstanceData C type : void*
	 */
	public NSC_EXEC_DATA(PNSC_FNSPEECH_DATA pfnSpeechData, PNSC_FNSPEECH_EVENT pfnSpeechEvent,
	        NativeLong ulEventFilter, int bEventSynchroReq, NSC_SOUND_DATA vsSoundData,
	        Pointer pAppInstanceData) {
		super();
		this.pfnSpeechData = pfnSpeechData;
		this.pfnSpeechEvent = pfnSpeechEvent;
		this.ulEventFilter = ulEventFilter;
		this.bEventSynchroReq = bEventSynchroReq;
		this.vsSoundData = vsSoundData;
		this.pAppInstanceData = pAppInstanceData;
	}

	public static class ByReference extends NSC_EXEC_DATA implements Structure.ByReference {

	};

	public static class ByValue extends NSC_EXEC_DATA implements Structure.ByValue {

	};
}
