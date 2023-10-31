package org.daisy.pipeline.braille.common.calabash.impl;

import java.util.Map;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.calabash.XMLCalabashOptionValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.XProcMonitor;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.Query.util.marshallQuery;

import org.osgi.service.component.annotations.Component;

public class PxParseQueryStep extends DefaultStep implements XProcStep {

	@Component(
		name = "px:parse-query",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}parse-query" }
	)
	public static class StepProvider implements XProcStepProvider {
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new PxParseQueryStep(runtime, step);
		}
	}

	private WritablePipe result = null;
	private static final QName _QUERY = new QName("query");

	private PxParseQueryStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
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
		try {
			marshallQuery(query(new XMLCalabashOptionValue(getOption(_QUERY)).toString()),
			              new XMLCalabashOutputValue(result, runtime).asXMLStreamWriter());
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
		super.run();
	}
}
