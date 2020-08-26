package org.daisy.pipeline.tts.sapinative.impl;

public class SAPILib {
  
	static native long openConnection();
	
	static native int closeConnection(long connection);
	
	static native int speak(long connection, String voiceVendor, String voiceName, String text);

	/* in bytes*/
	static native int getStreamSize(long connection);
	
	/* returns the new offset */
	static native int readStream(long connection, byte[] dest, int offset);
	
	static native String[] getVoiceVendors();
	
	static native String[] getVoiceNames();
	
	static native String[] getBookmarkNames(long connection);
	
	/* in milliseconds */
	static native long[] getBookmarkPositions(long connection);
	
	static native int initialize(int sampleRate, int bitsPerSample);
	
	static native int dispose();
}
