package org.daisy.pipeline.tts.css.calabash.impl;

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.tree.util.ProcInstParser;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.pipeline.tts.config.ConfigReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

import cz.vutbr.web.css.NetworkProcessor;
import cz.vutbr.web.csskit.DefaultNetworkProcessor;

public class InlineCSSStep extends DefaultStep implements TreeWriterFactory, XProcStep {

	private Logger Logger = LoggerFactory.getLogger(InlineCSSStep.class);

	private String mStyleNsOption;
	private ReadablePipe mConfig;
	private ReadablePipe mSource = null;
	private WritablePipe mResult = null;
	private XProcRuntime mRuntime;
	private NetworkProcessor mNetwork;
	
	public InlineCSSStep(XProcRuntime runtime, XAtomicStep step, final URIResolver resolver) {
		super(runtime, step);
		mRuntime = runtime;
		mNetwork = new DefaultNetworkProcessor() {
			@Override
			public InputStream fetch(URL url) throws IOException {
				try {
					if (url != null) {
						Source resolved = resolver.resolve(url.toString(), "");
						if (resolved != null) {
							if (resolved instanceof StreamSource)
								return ((StreamSource)resolved).getInputStream();
							else
								url = new URL(resolved.getSystemId());
						}
					}
				} catch (TransformerException e) {
				} catch (MalformedURLException e) {
				}
				return super.fetch(url);
			}
		};
	}

	public void setInput(String port, ReadablePipe pipe) {
		if ("config".equalsIgnoreCase(port))
			mConfig = pipe;
		else
			mSource = pipe;
	}

	@Override
	public void setOption(QName name, RuntimeValue value) {
		super.setOption(name, value);
		String optName = name.getLocalName();
		if ("style-ns".equalsIgnoreCase(optName)) {
			mStyleNsOption = value.getString();
		} else {
			mRuntime.error(new Throwable("unknown option " + optName));
			return;
		}
	}

	public void setOutput(String port, WritablePipe pipe) {
		mResult = pipe;
	}

	public void reset() {
		mSource.resetReader();
		mResult.resetWriter();
	}

	static URI buildAbsoluteURI(String path, XdmNode doc) {
		URI u;
		try {
			u = new URI(path);
		} catch (URISyntaxException e) {
			try {
				u = new URI("file://" + path);
			} catch (URISyntaxException e1) {
				return null;
			}
		}
		if (!u.isAbsolute()) {
			u = doc.getBaseURI().resolve(u);
		}

		return u;
	}

	static Collection<URI> getCSSurisInContent(XdmNode doc) {
		Collection<URI> result = new ArrayList<URI>();

		//add the CSS stylesheet URI from the processing-instructions
		XdmSequenceIterator it = doc.axisIterator(Axis.CHILD);
		while (it.hasNext()) {
			XdmNode next = (XdmNode) it.next();
			if (next.getNodeKind() == XdmNodeKind.PROCESSING_INSTRUCTION) {
				String content = next.getStringValue();
				String href = ProcInstParser.getPseudoAttribute(content, "href");
				if ("xml-stylesheet".equals(next.getNodeName().getLocalName())
				        && href != null
				        && !href.isEmpty()
				        && "text/css".equals(ProcInstParser
				                .getPseudoAttribute(content, "type"))) {

					URI uri = buildAbsoluteURI(href, doc);
					if (uri != null)
						result.add(uri);
				}
			}
		}

		//add the CSS stylesheet URIs from the headers
		it = doc.axisIterator(Axis.DESCENDANT);
		while (it.hasNext()) {
			XdmNode next = (XdmNode) it.next();
			if (next.getNodeKind() == XdmNodeKind.ELEMENT
			        && "link".equals(next.getNodeName().getLocalName())
			        && "stylesheet".equals(next.getAttributeValue(new QName(null, "rel")))) {
				String href = next.getAttributeValue(new QName(null, "href"));
				URI uri = buildAbsoluteURI(href, doc);
				if (uri != null)
					result.add(uri);
			}
		}

		return result;
	}

	public void run() throws SaxonApiException {
		super.run();

		//read config
		CSSConfigExtension cssExt = new CSSConfigExtension();
		XdmNode config = null; //it is allowed to not provide any config file
		if (mConfig.moreDocuments()) {
			config = mConfig.read();
		}
		new ConfigReader(mRuntime.getProcessor(), config, cssExt);

		//read first document
		XdmNode doc = null;
		if (mSource.moreDocuments()) {
			doc = mSource.read();
		}

		Collection<URI> alluris = new ArrayList<URI>();
		alluris.addAll(getCSSurisInContent(doc));
		alluris.addAll(cssExt.getCSSstylesheetURIs());

		CSSInliner inliner = new CSSInliner();
		SpeechSheetAnalyser analyzer = new SpeechSheetAnalyser();
		try {
			analyzer.analyse(alluris, cssExt.getEmbeddedCSS(), config.getBaseURI(), mNetwork);
		} catch (Throwable t) {
			Logger.debug("error while analyzing CSS speech: " + t.getMessage());
			mResult.write(doc);
			return;
		}

		// rebuild the document with the additional style info
		XdmNode rebuilt = inliner.inline(this, doc.getBaseURI(), doc, analyzer,
		        mStyleNsOption);

		for (URI uri : alluris) {
			mRuntime.info(null, null, uri.toString() + " inlined");
		}

		mResult.write(rebuilt);
	}

	@Override
	public TreeWriter newInstance() {
		return new TreeWriter(mRuntime);
	}
}
