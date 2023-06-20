package org.daisy.pipeline.css.sass;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
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

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.css.CssPreProcessor;
import org.daisy.pipeline.css.sass.impl.SassPostProcessLexer;
import org.daisy.pipeline.css.sass.impl.SassPostProcessParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CssPreProcessor} that handles media type "text/x-scss".
 */
public class SassCompiler implements CssPreProcessor {

	public boolean supportsMediaType(String mediaType, URL url) {
		if ("text/x-scss".equals(mediaType))
			return true;
		else if (mediaType == null && url != null && url.toString().endsWith(".scss"))
			return true;
		else
			return false;
	}

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
					URI uri = URLs.asURI(url);
					URI base = previous.getAbsoluteUri();
					logger.debug("Importing SASS style sheet: " + uri + " (base = " + base + ")");
					try {
						try {
							StreamSource resolved = SassCompiler.this.resolver.resolve(uri.toString(), base.toString());
							URI abs = URLs.asURI(resolved.getSystemId());
							logger.debug("Resolved to: " + abs);
							try {
								return ImmutableList.of(
									new Import(uri, abs,
									           preProcess(
									               byteSource(resolved.getInputStream())
									               // why are we assuming UTF-8?
									               .asCharSource(StandardCharsets.UTF_8).read()))); }
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

	private static final String scssNumber = "\\d*\\.\\d+";
	private static final String scssColor = "(#[\\da-zA-Z]+|(rgb|hsl)a?\\([^)]*\\))";
	private static final String scssBadStringChars = "!\"#$'()*+,\\.\\/:<=>?@\\[\\\\\\]^`{|}~-";
	private static final String scssNumberColorString = "\\s*("+ scssNumber
	                                                       +"|"+ scssColor
	                                                       +"|[^\\s"+scssBadStringChars+"]+"
	                                                       +"|\"([^\"]|\\\")*\""
	                                                       +"|'([^']|\\')*'"
	                                                       +")\\s*";
	private static final Pattern charsetRule = Pattern.compile("(@charset +\"(.+)\";?).*");
	private static final Pattern sourceMappingComment
		= Pattern.compile("/\\*# sourceMappingURL=data:application/json;base64,(.+)=* \\*/");
	private static final Base64.Decoder base64Decoder = Base64.getDecoder();
	
	/**
	 * @throws IOException if something goes wrong reading the input
	 * @throws RuntimeException if the compilation fails.
	 */
	@Override
	public PreProcessingResult compile(PreProcessingSource source) throws IOException {
		Compiler sassCompiler = new Compiler();
		Options options = new Options();
		options.setIsIndentedSyntaxSrc(false);
		options.setOutputStyle(OutputStyle.EXPANDED);
		options.setSourceMapContents(false);
		options.setSourceMapEmbed(true);
		options.setSourceComments(false);
		options.setPrecision(5);
		options.setOmitSourceMapUrl(false);
		options.getImporters().add(importer);
		// FIXME: Note that the addition of these variables breaks the original line info in
		// sourceMap. Luckily this is not a real problem because only the base info is used.
		StringBuilder scss = new StringBuilder();
		if (env != null) {
			for (String var : env.keySet()) {
				String value = env.get(var);
				if (!value.matches(scssNumberColorString)) {
					// if value contains spaces or special characters that can mess up parsing; wrap it in single quotes
					logger.debug("scss variable '"+var+"' contains special characters: "+value);
					value = "'"+value.replace("\n", "\\A").replace("'","\\27")+"'";
					logger.debug("scss variable '"+var+"' was quoted                 : "+value);
				} else {
					logger.debug("scss variable '"+var+"' contains no special characters: "+value);
				}
				scss.append("$").append(var).append(": ").append(value).append(";\n");
			}
		}
		BufferedReader stream = new BufferedReader(source.stream);
		String firstLine = stream.readLine();
		Matcher m = charsetRule.matcher(firstLine);
		if (m.matches()) {
			String charset = m.group(2);
			firstLine = firstLine.substring(m.group(1).length());
			try {
				stream = new BufferedReader(source.reread(Charset.forName(charset)));
				firstLine = stream.readLine();
				m = charsetRule.matcher(firstLine);
				if (m.matches()) { // must be true
					firstLine = firstLine.substring(m.group(1).length());
				}
			} catch (UnsupportedCharsetException e) {
				logger.warn("Ignoring @charset \"" + charset + "\";");
			} catch (IOException e) {
				logger.warn("Ignoring @charset \"" + charset + "\";");
			}
		}
		scss.append(firstLine).append("\n");
		scss.append(CharStreams.toString(stream));
		stream.close();
		try {
			// FIXME: Note that preProcess() breaks the original column info in sourceMap. Luckily
			// this is not a real problem because only the base info is used.
			Output result = sassCompiler.compileString(preProcess(scss.toString()), source.base, null, options);
			String css = result.getCss();
			String sourceMap = null; {
				int lastNewlineIdx = css.lastIndexOf('\n');
				String lastLine = css.substring(lastNewlineIdx + 1);
				if ((m = sourceMappingComment.matcher(lastLine)).matches()) {
					sourceMap = new String(base64Decoder.decode(m.group(1)));
					css = css.substring(0, lastNewlineIdx);
				}
			}
			// FIXME: Note that postProcess() breaks the column info in sourceMap. Luckily this only
			// happens in selectors, not in url() values.
			css = postProcess(css);
			logger.debug(source.base + " compiled to:\n---\n" + css + "---\n");
			return new PreProcessingResult(
				new StringReader(css),
				sourceMap,
				// in source map files are relative to the current working directory
				sourceMap != null
					? URLs.asURI(new File("").getAbsoluteFile())
					: source.base); }
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
				systemId = URLs.resolve(URLs.asURI(base), URLs.asURI(href)).toASCIIString();
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

	/**
	 * In order to fully support stacked pseudo-elements and pseudo-classes on pseudo-elements (also
	 * in combination with @extend), we need to pre-processed the SASS before it is compiled to
	 * CSS. Pseudo-elements are replaced with a child selector followed by a pseudo-element. This is
	 * reverted in {@link #postProcess(String)}.
	 */
	private static String preProcess(String sass) {
		return sass.replaceAll("::", ">::");
	}

	/**
	 * Replace child selector followed by a pseudo-element with the pseudo-element.
	 */
	private static String postProcess(String css) {
		if (!css.contains("::"))
			return css;
		try {
			ANTLRInputStream input;
			try {
				input = new ANTLRInputStream(
					new ByteArrayInputStream(css.getBytes(StandardCharsets.UTF_8)),
					StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				throw new RuntimeException(e); // should not happen
			}
			SassPostProcessLexer lexer = new SassPostProcessLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			SassPostProcessParser parser = new SassPostProcessParser(tokens);
			return parser.stylesheet();
		} catch (RecognitionException e) {
			throw new RuntimeException("Error happened while parsing the CSS", e);
		}
	}
}
