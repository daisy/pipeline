package org.daisy.pipeline.tts.onecore;

import java.util.HashMap;
import java.util.Map;

public enum OnecoreLibResult {
	SAPINATIVE_OK(0, "Success"),
    UNSUPPORTED_FORMAT(1, "Unsupported format"),
    TOO_LONG_VOICE_NAME(2, "Voice name is too long"),
    TOO_LONG_VOICE_VENDOR(3, "Voice vendor is too long"),
    TOO_LONG_TEXT(4, "Text is too long"),
    UNSUPPORTED_AUDIO_FORMAT(5, "Unsupported audio format"),
    VOICE_NOT_FOUND(6, "Voice not found"),
    COULD_NOT_SET_VOICE(7, "Could not set voice"),
    COULD_NOT_SET_EVENT_INTERESTS(8, "Could not set event interests"),
    COULD_NOT_LISTEN_TO_EVENTS(9, "Could not listen to events"),
    COULD_NOT_INIT_COM(10, "Could not init COM server"),
    COULD_NOT_CREATE_CATEGORY(11, "Could not create category"),
    COULD_NOT_ENUM_CATEGORY(12, "Could not enumerate category"),
    COULD_NOT_COUNT_ENUM(13, "Could not count enum"),
    COULD_NOT_SPEAK_INVALIDARG(14, "Could not speak due to invalid arguments"),
    COULD_NOT_SPEAK_E_POINTER(15, "Could not speak due to pointer exception"),
    COULD_NOT_SPEAK_OUTOFMEMORY(16, "Could not speak due to out-of-memory exception"),
    COULD_NOT_SPEAK_INVALIDFLAGS(17, "Could not speak due to invalid flags"),
    COULD_NOT_SPEAK_BUSY(18, "Could not speak due to busy interface"),
    COULD_NOT_SPEAK_THIS_FORMAT(19, "Could not speak due to a format error"),
    COULD_NOT_SPEAK(20, "Could not speak due to an unknown issue"),
    COULD_NOT_SET_STREAM_FORMAT(21, "Could not set the stream format"),
    COULD_NOT_BIND_STREAM(22, "Could not bind the stream"),
	COULD_NOT_INIT_STREAM(23, "Could not initialize the memory stream"),
	COULD_NOT_BIND_OUTPUT(24, "Could not bind voice output to sapi stream");

	private final int code;
	private final String texte;
	
	OnecoreLibResult(int code, String texte) { this.code = code; this.texte = texte; }
	
	public int value() { return code; }
	public String texte() { return texte; }
	
	private static final Map<Integer, OnecoreLibResult> BY_CODE = new HashMap<>();
	static {
        for (OnecoreLibResult e : values()) {
        	BY_CODE.put(e.code, e);
        }
    }
	
	public static OnecoreLibResult valueOfCode(int code) {
		return BY_CODE.get(code);
	}
	
	
}
