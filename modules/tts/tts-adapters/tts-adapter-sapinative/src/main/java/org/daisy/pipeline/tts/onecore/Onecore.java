package org.daisy.pipeline.tts.onecore;

import org.daisy.pipeline.tts.Voice;

import java.io.IOException;

public class Onecore {

	public static native int initialize();

	public static native Voice[] getVoices() throws IOException;

	public static native NativeSynthesisResult speak(String voiceVendor, String voiceName, String text) throws IOException;

	public static native int dispose();

}
