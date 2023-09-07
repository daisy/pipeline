package org.daisy.pipeline.tts.onecore;

import org.daisy.pipeline.tts.Voice;

import java.io.IOException;

public class SAPI {

	public static native int initialize(int sampleRate, short bitsPerSample);

	public static native Voice[] getVoices() throws IOException;

	public static native NativeSynthesisResult speak(String voiceVendor, String voiceName, String text, int sampleRate, short bitsPerSample) throws IOException;

	public static native int dispose();


}
