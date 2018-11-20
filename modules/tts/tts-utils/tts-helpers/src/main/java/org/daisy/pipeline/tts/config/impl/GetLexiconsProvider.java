package org.daisy.pipeline.tts.config.impl;

import java.net.URI;
import java.net.URISyntaxException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.config.LexiconsConfigExtension;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

public class GetLexiconsProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(final XProcRuntime runtime, XAtomicStep step) {
		return new XProcStep() {

			private ReadablePipe mConfig;
			private WritablePipe mResult;

			public void setInput(String port, ReadablePipe pipe) {
				if ("config".equalsIgnoreCase(port))
					mConfig = pipe;
				else {
					runtime.error(new Throwable("unknown port " + port));
				}
			}

			public void setOutput(String port, WritablePipe pipe) {
				mResult = pipe;
			}

			@Override
			public void setOption(QName arg0, RuntimeValue arg1) {
			}

			public void reset() {
				mConfig.resetReader();
				mResult.resetWriter();
			}

			public void run() throws SaxonApiException {

				Processor proc = runtime.getProcessor();
				
				LexiconsConfigExtension lexiconExt = new LexiconsConfigExtension(proc);
				new ConfigReader(proc, mConfig.read(), lexiconExt);
				
				int i = 0;
				for (XdmNode lexicon : lexiconExt.getLexicons()) {
					TreeWriter tw = new TreeWriter(proc);
					try {
						tw.startDocument(new URI(lexicon.getDocumentURI().toString().replace(".xml", ""+i++ +".xml")));
						tw.addSubtree(lexicon);
						tw.endDocument();
						mResult.write(tw.getResult());
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
