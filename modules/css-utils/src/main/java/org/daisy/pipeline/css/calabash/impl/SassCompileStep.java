package org.daisy.pipeline.css.calabash.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import static com.google.common.collect.Iterators.forArray;
import com.google.common.io.CharStreams;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.Base64;
import com.xmlcalabash.util.S9apiUtils;

import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import static org.daisy.common.saxon.SaxonHelper.jaxpQName;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.css.CssPreProcessor.PreProcessingResult;
import org.daisy.pipeline.css.CssPreProcessor.PreProcessingSource;
import org.daisy.pipeline.css.sass.SassCompiler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;

public class SassCompileStep extends DefaultStep implements XProcStep {

	private ReadablePipe sourcePipe = null;
	private ReadablePipe contextPipe = null;
	private WritablePipe resultPipe = null;

	private final InMemoryURIResolver inMemoryResolver;
	private final URIResolver cssURIResolver;

	private static final net.sf.saxon.s9api.QName _parameters
		= new net.sf.saxon.s9api.QName("parameters");
	private static final net.sf.saxon.s9api.QName _encoding
		= new net.sf.saxon.s9api.QName("encoding");
	private static final QName _content_type = new QName("content-type");

	private SassCompileStep(XProcRuntime runtime, XAtomicStep step, URIResolver resolver) {
		super(runtime, step);
		inMemoryResolver = new InMemoryURIResolver();
		cssURIResolver = fallback(inMemoryResolver, resolver, simpleURIResolver);
	}

	@Override
	public void setInput(String port, ReadablePipe pipe) {
		if ("source".equals(port))
			sourcePipe = pipe;
		else
			contextPipe = pipe;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		resultPipe = pipe;
	}

	@Override
	public void reset() {
		sourcePipe.resetReader();
		contextPipe.resetReader();
		resultPipe.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			Map<String,Object> sassVariables = new HashMap<>();
			RuntimeValue paramOption = getOption(_parameters);
			if (paramOption != null)
				sassVariables = SaxonHelper.mapFromMapItem(
					(MapItem)SaxonHelper.getSingleItem(paramOption.getValue().getUnderlyingValue()),
					Object.class
				);
			inMemoryResolver.setContext(contextPipe);
			SassCompiler compiler = new SassCompiler(cssURIResolver,
			                                         Collections.unmodifiableMap(sassVariables));
			while (sourcePipe.moreDocuments()) {
				XdmNode doc = sourcePipe.read();
				URI baseURI = doc.getBaseURI();
				XdmNode root = S9apiUtils.getDocumentElement(doc);
				Reader stream; {
					if (!XProcConstants.c_data.equals(root.getNodeName()))
						throw new IllegalArgumentException("Expected c:data document");
					if ("base64".equals(root.getAttributeValue(_encoding)))
						stream = new InputStreamReader(
							new ByteArrayInputStream(Base64.decode(doc.getStringValue())),
							UTF_8);
					else
						stream = new StringReader(doc.getStringValue());
				}
				PreProcessingSource source = new PreProcessingSource(stream, baseURI) {
						@Override
						public Reader reread(Charset encoding) throws IOException {
							Reader newStream;
							if ("base64".equals(root.getAttributeValue(_encoding)))
								newStream = new InputStreamReader(
									new ByteArrayInputStream(Base64.decode(doc.getStringValue())),
									encoding);
							else
								// can not change encoding if file was loaded in memory as plain
								// text (decoding already performed)
								newStream = new StringReader(doc.getStringValue());
							stream.close();
							return newStream; }};
				PreProcessingResult result = compiler.compile(source);
				BaseURIAwareXMLStreamWriter w = XMLCalabashOutputValue.of(resultPipe, runtime)
				                                                      .asXMLStreamWriter();
				w.setBaseURI(URI.create(baseURI.toASCIIString().replaceAll("\\.scss$", ".css")));
				w.writeStartDocument();
				writeStartElement(w, jaxpQName(XProcConstants.c_data));
				writeAttribute(w, _content_type, "text/css");
				w.writeCharacters(CharStreams.toString(result.stream));
				w.writeEndElement();
				w.writeEndDocument();
				w.close();
			}
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	@Component(
		name = "pxi:sass-compile",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}sass-compile" }
	)
	public static class Provider implements XProcStepProvider {

		private URIResolver resolver;

		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step,
		                         XProcMonitor monitor, Map<String,String> properties) {
			return new SassCompileStep(runtime, step, resolver);
		}

		@Reference(
			name = "URIResolver",
			unbind = "-",
			service = URIResolver.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		public void setUriResolver(URIResolver resolver) {
			this.resolver = resolver;
		}
	}

	private static URIResolver simpleURIResolver = new URIResolver() {
			@Override
			public Source resolve(String href, String base) throws TransformerException {
				try {
					URI uri; {
						if (base != null)
							uri = new URI(base).resolve(new URI(href));
						else
							uri = new URI(href);
					}
					return new SAXSource(new InputSource(uri.toASCIIString()));
				} catch (URISyntaxException e) {
					throw new TransformerException(e);
				}
			}
		};

	private static URIResolver fallback(URIResolver... resolvers) {
		return new URIResolver() {
			public Source resolve(String href, String base) throws TransformerException {
				Iterator<URIResolver> iterator = forArray(resolvers);
				while (iterator.hasNext()) {
					Source source = iterator.next().resolve(href, base);
					if (source != null)
						return source; }
				return null;
			}
		};
	}

	private static final Logger logger = LoggerFactory.getLogger(CssAnalyzeStep.class);
}
