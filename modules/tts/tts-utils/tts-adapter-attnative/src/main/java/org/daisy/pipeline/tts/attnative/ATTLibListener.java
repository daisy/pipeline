package org.daisy.pipeline.tts.attnative;

import java.nio.ByteBuffer;

public interface ATTLibListener {
	void onRecvAudio(Object handler, ByteBuffer audioBuffer, int size);

	void onRecvMark(Object handler, String name);
}
