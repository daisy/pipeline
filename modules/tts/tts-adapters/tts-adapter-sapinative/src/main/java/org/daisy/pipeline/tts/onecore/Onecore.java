package org.daisy.pipeline.tts.onecore;

public class Onecore {

	public static native long openConnection();

	public static native int closeConnection(long connection);

	public static native int speak(long connection, String voiceVendor, String voiceName, String text);
	/* in bytes*/
	public static native int getStreamSize(long connection);

	/* returns the new offset */
	public static native int readStream(long connection, byte[] dest, int offset);

	public static native String[] getVoiceVendors();

	public static native String[] getVoiceNames();

	public static native String[] getVoiceLocales();

	public static native String[] getVoiceGenders();

	public static native String[] getVoiceAges();

	public static native String[] getBookmarkNames(long connection);

	/* in milliseconds */
	public static native long[] getBookmarkPositions(long connection);

	public static native int initialize();

	public static native int dispose();

}
