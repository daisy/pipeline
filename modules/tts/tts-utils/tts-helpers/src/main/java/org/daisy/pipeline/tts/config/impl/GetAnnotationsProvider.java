package org.daisy.pipeline.tts.config.impl;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.tts.config.AnnotationsConfigExtension;
import org.daisy.pipeline.tts.config.ConfigReader;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

public class GetAnnotationsProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(final XProcRuntime runtime, XAtomicStep step) {
		return new XProcStep() {

			private String mContentType;
			private ReadablePipe mConfig;
			private WritablePipe mResult;

			public void setInput(String port, ReadablePipe pipe) {
				if ("config".equalsIgnoreCase(port))
					mConfig = pipe;
				else {
					runtime.error(new Throwable("unknown port " + port));
				}
			}

			@Override
			public void setOption(QName name, RuntimeValue value) {
				String optName = name.getLocalName();
				if ("content-type".equalsIgnoreCase(optName)) {
					mContentType = value.getString();
				} else {
					runtime.error(new Throwable("unknown option " + optName));
					return;
				}
			}

			public void setOutput(String port, WritablePipe pipe) {
				mResult = pipe;
			}

			public void reset() {
				mConfig.resetReader();
				mResult.resetWriter();
			}

			public void run() throws SaxonApiException {

				Processor proc = runtime.getProcessor();
				AnnotationsConfigExtension annoExt = new AnnotationsConfigExtension(proc);
				new ConfigReader(proc, mConfig.read(), annoExt);

				for (XdmNode annotations : annoExt.getAnnotations(mContentType)) {
					mResult.write(annotations);
				}
			}

			@Override
			public void setParameter(QName arg0, RuntimeValue arg1) {
			}

			@Override
			public void setParameter(String arg0, QName arg1, RuntimeValue arg2) {
			}
		};
	}
}
