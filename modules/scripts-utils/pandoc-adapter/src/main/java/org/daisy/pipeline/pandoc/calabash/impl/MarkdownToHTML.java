package org.daisy.pipeline.pandoc.calabash.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xml.DocumentBuilder;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.pandoc.Pandoc;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MarkdownToHTML extends DefaultStep implements XProcStep {

	private static final QName _source = new QName("source");
	private static final QName _detect_image_captions = new QName("detect-image-captions");

	private final Provider provider;

	private WritablePipe result = null;

	private MarkdownToHTML(Provider provider, XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
		this.provider = provider;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}

	@Override
	public void reset() {
		result.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			String markdown = getOption(_source).getString();
			boolean detectImageCaptions = getOption(_detect_image_captions, false);
			AtomicReference<Document> xhtml = new AtomicReference<>();
			Pandoc.CommandBuilder b = provider.pandoc
				.newCommand()
				.withInputFormat(Pandoc.Format.MARKDOWN)
				.withOutputFormat(Pandoc.Format.HTML)
				.withArgument("--standalone");
			if (detectImageCaptions)
				b = b.withFilter(Pandoc.Filter.DETECT_IMAGE_CAPTIONS);
			int rv = b.runner()
				.feedInput(markdown.getBytes(StandardCharsets.UTF_8))
				.consumeOutput(html -> xhtml.set(provider.toXML(html, "text/html")))
				.run();
			if (rv != 0)
				throw new RuntimeException("pandoc failed with exit code " + rv);
			XMLCalabashOutputValue.of(result, runtime).asNodeConsumer().accept(xhtml.get());
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	@Component(
		name = "pxi:markdown-to-html",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}markdown-to-html" }
	)
	public static class Provider implements XProcStepProvider {

		protected Provider() {
		}

		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new MarkdownToHTML(this, runtime, step);
		}

		Document toXML(InputStream input, String contentType) throws IOException, SAXException {
			return DocumentBuilder.parse(new InputSource(input), contentType, parsers);
		}

		Pandoc pandoc = null;

		@Reference(
			name = "pandoc",
			unbind = "-",
			service = Pandoc.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindPandoc(Pandoc pandoc) {
			this.pandoc = pandoc;
		}

		private final List<DocumentBuilder> parsers = new ArrayList<>();

		@Reference(
			name = "input-parser",
			unbind = "-",
			service = DocumentBuilder.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.STATIC
		)
		protected void addInputParser(DocumentBuilder parser) {
			parsers.add(parser);
		}
	}
}
