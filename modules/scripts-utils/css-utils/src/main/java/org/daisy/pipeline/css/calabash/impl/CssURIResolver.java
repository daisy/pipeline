package org.daisy.pipeline.css.calabash.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;

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

/**
 * URIResolver for CSS files.
 * Compiles SASS to CSS.
 * First tries the provided resolver and falls back to URL.openStream().
 * Throws a TransformerException if an error happens in the backing resolver, if the resource
 * can not be found, or if an error happens in the SASS compilation.
 */
class CssURIResolver implements URIResolver {

	private final StreamSourceURIResolver resolver;
	private final Map<String,String> sassVariables;
	private SassCompiler sassCompiler = null;

	CssURIResolver(URIResolver resolver, Map<String,String> sassVariables) {
		this.resolver = new StreamSourceURIResolver(resolver);
		this.sassVariables = sassVariables;
	}

	@Override
	public Source resolve(String href, String base) throws TransformerException {
		StreamSource resolved = resolver.resolve(href, base);
		if (href.endsWith(".scss")) {
			if (sassCompiler == null) sassCompiler = new SassCompiler(resolver);
			try {
				resolved = new StreamSource(
					sassCompiler.compile(resolved.getInputStream(),
					                     URLs.asURL(resolved.getSystemId()),
					                     sassVariables),
					resolved.getSystemId()); }
			catch (Exception e) {
				throw new TransformerException(e); }
		}
		return resolved;
	}

	private static class SassCompiler {

		final Importer importer;

		SassCompiler(final StreamSourceURIResolver resolver) {
			importer = new Importer() {
					public Collection<Import> apply(String url, Import previous) {
						URI uri = URIs.asURI(url);
						URI base = previous.getAbsoluteUri();
						logger.debug("Importing SASS style sheet: " + uri + " (base = " + base + ")");
						try {
							try {
								StreamSource resolved = resolver.resolve(uri.toString(), base.toString());
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

		InputStream compile(InputStream sass, URL base, Map<String,String> env) throws IOException {
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
			String scss = "";
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
				scss += ("$" + var + ": " + value + ";\n");
			}
			scss += byteSource(sass).asCharSource(StandardCharsets.UTF_8).read();
			try {
				Output result = sassCompiler.compileString(scss, StandardCharsets.UTF_8, URIs.asURI(base), null, options);
				if (result.getErrorStatus() != 0)
					throw new RuntimeException("Could not compile SASS style sheet: " + result.getErrorMessage());
				String css = result.getCss();
				logger.debug(base + " compiled to:\n\n" + css);
				return new ByteArrayInputStream(css.getBytes(StandardCharsets.UTF_8)); }
			catch (CompilationException e) {
				throw new RuntimeException("Could not compile SASS style sheet", e); }
		}
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

	private static final Logger logger = LoggerFactory.getLogger(CssURIResolver.class);

}
