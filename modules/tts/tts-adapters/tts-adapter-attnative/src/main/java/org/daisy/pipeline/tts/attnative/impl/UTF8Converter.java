package org.daisy.pipeline.tts.attnative.impl;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

// This may be moved to some commmon util class
public class UTF8Converter {

	public static class UTF8Buffer {
		public int size;
		public byte[] buffer;
	}

	static CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();

	static public UTF8Buffer convertToUTF8(String toConvert, byte[] buffer) {
		UTF8Buffer result = new UTF8Buffer();
		result.size = 0;
		result.buffer = buffer;

		CharBuffer input = CharBuffer.wrap(toConvert);
		while (true) {
			ByteBuffer bbuf = ByteBuffer.wrap(result.buffer);
			bbuf.position(result.size); //size bytes have already been encoded
			CoderResult cr = encoder.encode(input, bbuf, true);
			result.size = result.buffer.length - bbuf.remaining();
			if (!cr.isOverflow() && bbuf.remaining() >= 2) {
				break; //enough space in bbuf
			}
			//not enough space
			byte[] newBuffer = new byte[result.buffer.length + 2
			        + input.remaining()];
			System.arraycopy(result.buffer, 0, newBuffer, 0, result.size);
			result.buffer = newBuffer;
		}

		//the null-terminator is added here to prevent from wasting
		//another buffer and another copy later in case the process needs
		//this null-terminator
		result.buffer[result.size] = 0;
		result.buffer[result.size + 1] = 0;

		return result;
	}
}
