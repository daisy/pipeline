package org.daisy.common.xslt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

public class XslTransformTest implements URIResolver {

	private static class Env {
		XdmNode source;
		InputStream stylesheet;
		Configuration config;
	}

	@After
	public void cleanUp() {
		xsltTarget = null;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		return new StreamSource(new StringReader(xsltTarget));
	}

	private Processor Proc = new Processor(false);

	private Env initEnv(String xmlstr, String stylsheet) throws SaxonApiException {
		Env env = new Env();

		DocumentBuilder builder = Proc.newDocumentBuilder();
		SAXSource source = new SAXSource(new InputSource(new StringReader(xmlstr)));

		env.source = builder.build(source);
		env.config = Proc.getUnderlyingConfiguration();
		env.stylesheet = new ByteArrayInputStream(stylsheet.getBytes());

		return env;
	}

	@Test
	public void simpleIdTransform() throws SaxonApiException {
		String xmlstr = "<root>text1<span a=\"attr\">text2</span>text3</root>";
		String identity = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:template match=\"/\">"
		        + "<xsl:copy-of select=\".\"/>"
		        + "</xsl:template>" + "</xsl:stylesheet>";

		Env env = initEnv(xmlstr, identity);

		XslTransformCompiler compiler = new XslTransformCompiler(env.config);
		ThreadUnsafeXslTransformer tr = compiler.compileStylesheet(env.stylesheet)
		        .newTransformer();

		Assert.assertEquals(xmlstr.replaceAll("\\s", ""), tr.transform(env.source).toString()
		        .replaceAll("\\s", ""));
	}

	@Test
	public void parametrizedTransform() throws SaxonApiException {
		String xmlstr = "<root>text1<span a=\"attr\">text2</span>text3</root>";
		String paramkey = "paramkey";
		String paramval = "paramval";
		String sheet = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:param name=\""
		        + paramkey
		        + "\"/>"
		        + "<xsl:template match=\"/\">"
		        + "<root attr=\"{$"
		        + paramkey
		        + "}\">"
		        + "<xsl:copy-of select=\"/*\"/>"
		        + "</root></xsl:template>" + "</xsl:stylesheet>";

		Env env = initEnv(xmlstr, sheet);

		XslTransformCompiler compiler = new XslTransformCompiler(env.config);
		ThreadUnsafeXslTransformer tr = compiler.compileStylesheet(env.stylesheet)
		        .newTransformer();

		Map<String, Object> params = new TreeMap<String, Object>();
		params.put(paramkey, paramval);

		String expected = "<root attr=\"" + paramval + "\">" + xmlstr + "</root>";

		Assert.assertEquals(expected.replaceAll("\\s", ""), tr.transform(env.source, params)
		        .toString().replaceAll("\\s", ""));
	}

	@Test
	public void textTransformation1() throws SaxonApiException {
		String xmlstr = "<root/>";
		String identity = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:output method=\"text\" omit-xml-declaration=\"yes\" indent=\"no\"/>"
		        + "<xsl:template match=\"/\">"
		        + "<xsl:text>some_text</xsl:text>"
		        + "</xsl:template>" + "</xsl:stylesheet>";

		Env env = initEnv(xmlstr, identity);

		XslTransformCompiler compiler = new XslTransformCompiler(env.config);
		ThreadUnsafeXslTransformer tr = compiler.compileStylesheet(env.stylesheet)
		        .newTransformer();

		Assert.assertEquals("some_text", tr.transformToString(env.source)
		        .replaceAll("\\s", ""));
	}

	@Test
	public void textTransformation2() throws SaxonApiException {
		String xmlstr = "<root>text1<span a=\"attr\">text2</span>text3</root>";
		String identity = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:template match=\"/\">"
		        + "<xsl:copy-of select=\".\"/>"
		        + "</xsl:template>" + "</xsl:stylesheet>";

		Env env = initEnv(xmlstr, identity);

		XslTransformCompiler compiler = new XslTransformCompiler(env.config);
		ThreadUnsafeXslTransformer tr = compiler.compileStylesheet(env.stylesheet)
		        .newTransformer();

		String expected = "<?xmlversion=\"1.0\" encoding=\"UTF-8\"?>" + xmlstr;
		Assert.assertEquals(expected.replaceAll("\\s", ""), tr.transformToString(env.source)
		        .replaceAll("\\s", ""));
	}

	@Test
	public void recycledTransform() throws SaxonApiException {
		String xmlstr = "<root>text1<span a=\"attr\">text2</span>text3</root>";
		String paramkey = "paramkey";
		String paramval = "paramval";
		String sheet = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:param name=\""
		        + paramkey
		        + "\" select=\"'default'\"/>"
		        + "<xsl:template match=\"/\">"
		        + "<root attr=\"{$"
		        + paramkey
		        + "}\">"
		        + "<xsl:copy-of select=\"/*\"/>"
		        + "</root></xsl:template>"
		        + "</xsl:stylesheet>";

		Env env = initEnv(xmlstr, sheet);

		XslTransformCompiler compiler = new XslTransformCompiler(env.config);
		ThreadUnsafeXslTransformer tr = compiler.compileStylesheet(env.stylesheet)
		        .newTransformer();

		Map<String, Object> params = new TreeMap<String, Object>();
		params.put(paramkey, paramval);
		tr.transform(env.source, params); //first transform setting the param

		String expected = "<root attr=\"default\">" + xmlstr + "</root>";

		Assert.assertEquals(expected.replaceAll("\\s", ""), tr.transform(env.source)
		        .toString().replaceAll("\\s", ""));
	}

	@Test
	public void multithreaded() throws SaxonApiException, InterruptedException {
		String xmlstr = "<root>text1<span a=\"attr\">text2</span>text3</root>";
		final String paramkey = "paramkey";
		String sheet = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:param name=\""
		        + paramkey
		        + "\"/>"
		        + "<xsl:template match=\"/\">"
		        + "<root attr=\"{$"
		        + paramkey
		        + "}\">"
		        + "<xsl:copy-of select=\"/*\"/>"
		        + "</root></xsl:template>" + "</xsl:stylesheet>";

		final Env env = initEnv(xmlstr, sheet);

		XslTransformCompiler compiler = new XslTransformCompiler(env.config);
		final CompiledStylesheet cs = compiler.compileStylesheet(env.stylesheet);

		Thread[] threads = new Thread[500];
		final String[] result = new String[threads.length];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				@Override
				public void start() {
					Map<String, Object> params = new TreeMap<String, Object>();
					params.put(paramkey, String.valueOf(j));
					try {
						result[j] = cs.newTransformer().transform(env.source, params)
						        .toString();
					} catch (SaxonApiException e) {
						//result[j] is null
					}
				}
			};
		}
		for (Thread th : threads)
			th.start();

		for (Thread th : threads)
			th.join();

		for (int i = 0; i < result.length; ++i) {
			Assert.assertTrue(result[i] != null);
			String expected = "<root attr=\"" + String.valueOf(i) + "\">" + xmlstr + "</root>";
			Assert.assertEquals(expected.replaceAll("\\s", ""), result[i]
			        .replaceAll("\\s", ""));
		}
	}

	@Test
	public void identityInclude() throws SaxonApiException {
		xsltTarget = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:template match=\"/\">"
		        + "<xsl:copy-of select=\".\"/>"
		        + "</xsl:template>" + "</xsl:stylesheet>";

		String xmlstr = "<root>text1<span a=\"attr\">text2</span>text3</root>";
		String identity = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">"
		        + "<xsl:include href=\"identity.xsl\"/></xsl:stylesheet>";

		Env env = initEnv(xmlstr, identity);

		XslTransformCompiler compiler = new XslTransformCompiler(env.config, this);
		ThreadUnsafeXslTransformer tr = compiler.compileStylesheet(env.stylesheet)
		        .newTransformer();

		Assert.assertEquals(xmlstr.replaceAll("\\s", ""), tr.transform(env.source).toString()
		        .replaceAll("\\s", ""));
	}

	private String xsltTarget;
}
