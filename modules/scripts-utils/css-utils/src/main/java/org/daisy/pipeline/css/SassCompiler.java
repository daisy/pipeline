package org.daisy.pipeline.css;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;
import com.google.common.io.CharStreams;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.importer.Import;
import io.bit3.jsass.importer.Importer;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;

import org.daisy.common.file.URIs;
import org.daisy.common.file.URLs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SassCompiler {

	private final Importer importer;
	private final StreamSourceURIResolver resolver;
	private final Map<String,String> env;

	/**
	 * @param env SASS variables. The map is allowed to be mutated by the caller after the
	 *            SassCompiler is created.
	 */
	public SassCompiler(final URIResolver resolver, Map<String,String> env) {
		this.resolver = new StreamSourceURIResolver(resolver);
		this.env = env;
		importer = new Importer() {
				public Collection<Import> apply(String url, Import previous) {
					URI uri = URIs.asURI(url);
					URI base = previous.getAbsoluteUri();
					logger.debug("Importing SASS style sheet: " + uri + " (base = " + base + ")");
					try {
						try {
							StreamSource resolved = SassCompiler.this.resolver.resolve(uri.toString(), base.toString());
							URI abs = URIs.asURI(resolved.getSystemId());
							logger.debug("Resolved to: " + abs);
							try {
								return ImmutableList.of(
									new Import(uri, abs,
									           byteSource(resolved.getInputStream())
									           .asCharSource(StandardCharsets.UTF_8).read())); }
							catch (RuntimeException e) {
								throw new IOException(e); }}
						catch (TransformerException e) {
							throw new IOException(e); }}
					catch (IOException e) {
						if (!url.endsWith(".scss"))
							return apply(url + ".scss", previous);
						else
							throw new RuntimeException("Failed to import " + uri + " (base = " + base + ")", e); }
				}
			};
	}

	static final String scssNumber = "\\d*\\.\\d+";
	static final String scssColor = "(#[\\da-zA-Z]+|(rgb|hsl)a?\\([^)]*\\))";
	static final String scssBadStringChars = "!\"#$'()*+,\\.\\/:<=>?@\\[\\\\\\]^`{|}~-";
	static final String scssNumberColorString = "\\s*("+ scssNumber
	                                                +"|"+ scssColor
	                                                +"|[^\\s"+scssBadStringChars+"]+"
	                                                +"|\"([^\"]|\\\")*\""
	                                                +"|'([^']|\\')*'"
	                                                +")\\s*";

	/**
	 * @param encoding the encoding of the input or null if unknown
	 * @throws IOException if something goes wrong reading the input
	 * @throws RuntimeException if the compilation fails.
	 */
	public InputStream compile(Source sass, Charset encoding) throws IOException {
		String base = sass.getSystemId();
		if (sass instanceof StreamSource)
			return compile(((StreamSource)sass).getInputStream(), URLs.asURL(base), encoding);
		else
			try {
				return compile(resolver.resolve(base, base), encoding);
			} catch (TransformerException e) {
				throw new IOException(e);
			}
	}

	/**
	 * @param encoding the encoding of the input or null if unknown
	 * @throws IOException if something goes wrong reading the input
	 * @throws RuntimeException if the compilation fails.
	 */
	public InputStream compile(InputStream sass, URL base, Charset encoding) throws IOException {
		Compiler sassCompiler = new Compiler();
		Options options = new Options();
		options.setIsIndentedSyntaxSrc(false);
		options.setOutputStyle(OutputStyle.EXPANDED);
		options.setSourceMapContents(false);
		options.setSourceMapEmbed(false);
		options.setSourceComments(false);
		options.setPrecision(5);
		options.setOmitSourceMapUrl(true);
		options.getImporters().add(importer);
		StringBuilder scss = new StringBuilder();
		if (env != null) {
			for (String var : env.keySet()) {
				String value = env.get(var);
				if (!value.matches(scssNumberColorString)) {
					// if value contains spaces or special characters that can mess up parsing; wrap it in single quotes
					logger.debug("scss variable '"+var+"' contains special characters: "+value);
					value = "'"+value.replaceAll("'", "\\\\'")+"'";
					logger.debug("scss variable '"+var+"' was quoted                 : "+value);
				} else {
					logger.debug("scss variable '"+var+"' contains no special characters: "+value);
				}
				scss.append("$").append(var).append(": ").append(value).append(";\n");
			}
		}
		// FIXME: if stream starts with BOM, encoding should be UTF-8
		BufferedInputStream bufferedStream = new BufferedInputStream(sass);
		bufferedStream.mark(1000);
		BufferedReader r = new BufferedReader(new InputStreamReader(bufferedStream,
		                                                            encoding != null ? encoding : StandardCharsets.UTF_8));
		String firstLine = r.readLine();
		Pattern charsetRule = Pattern.compile("(@charset +\"(.+)\";?).*");
		Matcher m = charsetRule.matcher(firstLine);
		if (m.matches()) {
			String charset = m.group(2);
			firstLine = firstLine.substring(m.group(1).length());
			if (encoding == null)
				try {
					encoding = Charset.forName(charset);
				} catch (UnsupportedCharsetException e) {
					logger.warn("Ignoring @charset \"" + charset + "\";");
				}
			else
				logger.warn("Ignoring @charset \"" + charset + "\";");
		}
		if (encoding == null)
			encoding = StandardCharsets.UTF_8;
		if (encoding != StandardCharsets.UTF_8) {
			if (firstLine.getBytes(StandardCharsets.UTF_8).length < 1000) {
				bufferedStream.reset();
				r = new BufferedReader(new InputStreamReader(bufferedStream, encoding));
				firstLine = r.readLine();
				m = charsetRule.matcher(firstLine);
				if (m.matches()) { // must be true
					firstLine = firstLine.substring(m.group(1).length());
				}
			} else {
				logger.warn("Ignoring @charset \"" + encoding + "\";");
			}
		}
		scss.append(firstLine).append("\n");
		scss.append(CharStreams.toString(r));
		r.close();
		try {
			Output result = sassCompiler.compileString(scss.toString(), StandardCharsets.UTF_8, URIs.asURI(base), null, options);
			if (result.getErrorStatus() != 0)
				throw new RuntimeException("Could not compile SASS style sheet: " + result.getErrorMessage());
			String css = result.getCss();
			logger.debug(base + " compiled to:\n\n" + css);
			return new ByteArrayInputStream(css.getBytes(StandardCharsets.UTF_8)); }
		catch (CompilationException e) {
			throw new RuntimeException("Could not compile SASS style sheet", e); }
	}

	private static ByteSource byteSource(final InputStream is) {
		return new ByteSource() {
			public InputStream openStream() throws IOException {
				return is;
			}
		};
	}

	/**
	 * URIResolver that is guaranteed to return a StreamSource with a systemId.
	 * Falls back to URL.openStream() if the backing resolver can not resolve the URI.
	 * Throws a TransformerException if an error happens in the backing resolver or the resource can not be found.
	 */
	private static class StreamSourceURIResolver implements URIResolver {

		final URIResolver resolver;

		StreamSourceURIResolver(URIResolver resolver) {
			this.resolver = resolver;
		}

		@Override
		public StreamSource resolve(String href, String base) throws TransformerException {
			Source source = resolver.resolve(href, base);
			String systemId = null;
			if (source != null)
				systemId = source.getSystemId();
			if (systemId == null || systemId.equals(""))
				systemId = null;
			if (source != null && source instanceof StreamSource && systemId != null)
				return (StreamSource)source;
			if (systemId == null)
				systemId = URIs.resolve(base, href).toString();
			InputStream stream;
			if (source != null && source instanceof StreamSource)
				stream = ((StreamSource)source).getInputStream();
			else
				try {
					URLConnection conn = URLs.asURL(systemId).openConnection();
					stream = conn.getInputStream();
					if ("gzip".equalsIgnoreCase(conn.getContentEncoding()))
						stream = new GZIPInputStream(stream); }
				catch (IOException e) {
					throw new TransformerException(e); }
			return new StreamSource(stream, systemId);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(SassCompiler.class);

}
