package org.daisy.pipeline.tts.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;

public class URITest {
	@Test
	public void absolutePath() throws URISyntaxException {
		URL url = ConfigReader.URIinsideConfig("/foo/bar.xml", new URI("file:///tmp/"));
		Assert.assertTrue("file:///foo/bar.xml".equals(url.toString())
		        || "file:/foo/bar.xml".equals(url.toString()));
	}

	@Test
	public void relativePath() throws URISyntaxException {
		URL url = ConfigReader.URIinsideConfig("foo/bar.xml", new URI("file:///tmp/"));
		Assert.assertTrue("file:///tmp/foo/bar.xml".equals(url.toString())
		        || "file:/tmp/foo/bar.xml".equals(url.toString()));
	}

	@Test
	public void regularURI() throws URISyntaxException {
		URL url = ConfigReader.URIinsideConfig("file:///foo/bar.xml", new URI("file:///tmp/"));
		Assert.assertTrue("file:///foo/bar.xml".equals(url.toString())
		        || "file:/foo/bar.xml".equals(url.toString()));
	}
}
