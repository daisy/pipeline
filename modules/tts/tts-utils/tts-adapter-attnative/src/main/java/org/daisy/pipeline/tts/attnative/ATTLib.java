package org.daisy.pipeline.tts.attnative;

import java.nio.ByteBuffer;

public class ATTLib {
	//callback mechanism from C++ to JAVA
	private static ATTLibListener mListener;

	static void setListener(ATTLibListener l) {
		mListener = l;
	}

	static void onRecvAudio(Object handler, Object audioBuffer, int size) {
		mListener.onRecvAudio(handler, (ByteBuffer) audioBuffer, size);
	}

	static void onRecvMark(Object handler, String name) {
		mListener.onRecvMark(handler, name);
	}

	//functions implemented in C++
	static native long openConnection(String host, int port, int sampleRate,
	        int bitsPerSample);

	static native int closeConnection(long connection);

	static native int speak(Object handler, long connection, byte[] text);

	static native String[] getVoiceNames(long connection);
}
