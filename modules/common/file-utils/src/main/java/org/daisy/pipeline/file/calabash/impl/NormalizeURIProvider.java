package org.daisy.pipeline.file.calabash.impl;

import java.net.URI;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import static org.daisy.pipeline.file.FileUtils.normalizeURI;
import static org.daisy.pipeline.file.FileUtils.cResultDocument;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pxi:normalize-uri",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}normalize-uri" }
)
public class NormalizeURIProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		return new NormalizeURI(runtime, step);
	}

	public static class NormalizeURI extends DefaultStep implements XProcStep {

		private static final QName _URI = new QName("uri");

		private WritablePipe result = null;

		public NormalizeURI(XProcRuntime runtime, XAtomicStep step) {
			super(runtime,step);
		}

		public void setOutput(String port, WritablePipe pipe) {
			result = pipe;
		}

		public void reset() {
			result.resetWriter();
		}

		public void run() throws SaxonApiException {
			super.run();
			URI uri = URI.create(getOption(_URI, ""));
			result.write(
				runtime.getProcessor().newDocumentBuilder().build(
					new StreamSource(cResultDocument(normalizeURI(uri).toASCIIString()))));
		}
	}
}
