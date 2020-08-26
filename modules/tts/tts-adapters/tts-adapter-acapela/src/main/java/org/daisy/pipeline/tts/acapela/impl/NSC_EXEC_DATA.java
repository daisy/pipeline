package org.daisy.pipeline.tts.acapela.impl;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
import org.daisy.pipeline.tts.acapela.impl.NscubeLibrary.PNSC_FNSPEECH_DATA;
import org.daisy.pipeline.tts.acapela.impl.NscubeLibrary.PNSC_FNSPEECH_EVENT;
/**
 * <i>native declaration : nscube_forjna.h</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class NSC_EXEC_DATA extends Structure {
	/** C type : PNSC_FNSPEECH_DATA* */
	public PNSC_FNSPEECH_DATA pfnSpeechData;
	/** C type : PNSC_FNSPEECH_EVENT* */
	public PNSC_FNSPEECH_EVENT pfnSpeechEvent;
	public int ulEventFilter;
	public int bEventSynchroReq;
	/** C type : NSC_SOUND_DATA */
	public NSC_SOUND_DATA vsSoundData;
	/** C type : void* */
	public Pointer pAppInstanceData;
	public NSC_EXEC_DATA() {
		super();
	}
	protected List<? > getFieldOrder() {
		return Arrays.asList("pfnSpeechData", "pfnSpeechEvent", "ulEventFilter", "bEventSynchroReq", "vsSoundData", "pAppInstanceData");
	}
	/**
	 * @param pfnSpeechData C type : PNSC_FNSPEECH_DATA*<br>
	 * @param pfnSpeechEvent C type : PNSC_FNSPEECH_EVENT*<br>
	 * @param vsSoundData C type : NSC_SOUND_DATA<br>
	 * @param pAppInstanceData C type : void*
	 */
	public NSC_EXEC_DATA(PNSC_FNSPEECH_DATA pfnSpeechData, PNSC_FNSPEECH_EVENT pfnSpeechEvent, int ulEventFilter, int bEventSynchroReq, NSC_SOUND_DATA vsSoundData, Pointer pAppInstanceData) {
		super();
		this.pfnSpeechData = pfnSpeechData;
		this.pfnSpeechEvent = pfnSpeechEvent;
		this.ulEventFilter = ulEventFilter;
		this.bEventSynchroReq = bEventSynchroReq;
		this.vsSoundData = vsSoundData;
		this.pAppInstanceData = pAppInstanceData;
	}
	public NSC_EXEC_DATA(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends NSC_EXEC_DATA implements Structure.ByReference {
		
	};
	public static class ByValue extends NSC_EXEC_DATA implements Structure.ByValue {
		
	};
}