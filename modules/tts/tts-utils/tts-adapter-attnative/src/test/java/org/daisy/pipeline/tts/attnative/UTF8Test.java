package org.daisy.pipeline.tts.attnative;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class UTF8Test {
	@Test
	public void dataIntegrity() throws UnsupportedEncodingException {
		String str = "𝄞𝄞𝄞𝄞 水水水水水 𝄞水𝄞水𝄞水𝄞水 test 国Ø家Ť标准 ĜæŘ ß ŒÞ ๕";
		UTF8Converter.UTF8Buffer b = UTF8Converter.convertToUTF8(str,
		        new byte[1]);
		Assert.assertTrue(b.size > 10);
		Assert.assertTrue(b.buffer.length > 10);

		byte[] clean = new byte[b.size];
		System.arraycopy(b.buffer, 0, clean, 0, clean.length);

		String inverse = new String(clean, "UTF-8");
		Assert.assertEquals(str, inverse);
	}
}
