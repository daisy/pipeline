package org.daisy.pipeline.cssinlining;

import junit.framework.Assert;

import org.junit.Test;

public class URITest {

	@Test
	public void relativeURI1() {
		String result = SpeechSheetAnalyser.makeURLabsolute("blabla url(dir/file) blabla",
		        "/root/");
		Assert.assertEquals("blabla url('/root/dir/file') blabla", result);
	}

	@Test
	public void relativeURI2() {
		String result = SpeechSheetAnalyser.makeURLabsolute("blabla url(./dir/file) blabla",
		        "/root/");
		Assert.assertEquals("blabla url('/root/./dir/file') blabla", result);
	}

	@Test
	public void absoluteURI1() {
		String result = SpeechSheetAnalyser.makeURLabsolute("blabla url('/dir/file') blabla",
		        "/root/");
		Assert.assertEquals("blabla url('/dir/file') blabla", result);
	}

	@Test
	public void absoluteURI2() {
		String result = SpeechSheetAnalyser.makeURLabsolute(
		        "blabla url('file:///dir/file') blabla", "/root/");
		Assert.assertEquals("blabla url('file:///dir/file') blabla", result);
	}
}
