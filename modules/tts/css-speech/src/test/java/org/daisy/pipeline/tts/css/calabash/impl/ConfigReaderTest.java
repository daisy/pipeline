package org.daisy.pipeline.tts.css.calabash.impl;

import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.sax.SAXSource;

import junit.framework.Assert;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.config.ConfigReader;
import org.junit.Test;
import org.xml.sax.InputSource;

public class ConfigReaderTest {

	static Processor Proc = new Processor(false);
	static String docDirectory = "file:///doc/";

	public static CSSConfigExtension initConfigExtension(String xmlstr)
	        throws SaxonApiException {
		DocumentBuilder builder = Proc.newDocumentBuilder();
		SAXSource source = new SAXSource(new InputSource(new StringReader("<config>" + xmlstr
		        + "</config>")));
		source.setSystemId(docDirectory + "uri");
		XdmNode document = builder.build(source);

		CSSConfigExtension ext = new CSSConfigExtension();
		new ConfigReader(Proc, document, ext);
		return ext;
	}

	@Test
	public void CSSabsoluteURI() throws SaxonApiException, URISyntaxException {
		CSSConfigExtension cr = initConfigExtension("<css href=\"file:///uri1\"/><css href=\"file:///uri2\"/>");
		Set<URI> uris = new HashSet<URI>(cr.getCSSstylesheetURIs());

		Assert.assertEquals(2, uris.size());
		Assert.assertTrue(uris.contains(new URI("file:///uri1")));
		Assert.assertTrue(uris.contains(new URI("file:///uri2")));
	}

	@Test
	public void CSSrelativePath() throws SaxonApiException, URISyntaxException {
		CSSConfigExtension cr = initConfigExtension("<css href=\"foo/bar/path.css\"/>");

		Collection<URI> res = cr.getCSSstylesheetURIs();
		Assert.assertFalse(res.isEmpty());

		String uri = res.iterator().next().toString();
		Assert.assertEquals(new URI(docDirectory + "foo/bar/path.css"), new URI(uri));
	}

	@Test
	public void embeddedCSS() throws SaxonApiException {
		CSSConfigExtension cr = initConfigExtension("<css>css-content1</css><css>css-content2</css>");

		Set<String> contents = new HashSet<String>(cr.getEmbeddedCSS());

		Assert.assertEquals(2, contents.size());
		Assert.assertTrue(contents.contains("css-content1"));
		Assert.assertTrue(contents.contains("css-content2"));
	}
}
