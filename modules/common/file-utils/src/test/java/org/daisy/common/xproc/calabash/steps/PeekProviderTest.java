package org.daisy.common.xproc.calabash.steps;

import org.daisy.common.xproc.calabash.steps.PeekProvider.Peek;
import org.junit.Assert;
import org.junit.Test;

public class PeekProviderTest {

	@Test
	public void test() {
		Assert.assertEquals("Test that base64 encoding works", "YXBwbGljYXRpb24vZXB1Yit6aXA=", Peek.encodeBase64("application/epub+zip".getBytes()));
	}

}
