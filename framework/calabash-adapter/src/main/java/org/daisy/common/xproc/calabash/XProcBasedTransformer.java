package org.daisy.common.xproc.calabash;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.runtime.XPipeline;
import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.Mult;
import org.daisy.common.xproc.XProcMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple XProc (<code>p:pipeline</code>) based XML transformer that can be executed in a XMLCalabash runtime.
 */
public class XProcBasedTransformer implements XProcStepProvider {

	private final URI xproc;
	private final Map<QName,Mult<? extends InputValue<?>>> options;
	private final static Logger logger = LoggerFactory.getLogger(XProcBasedTransformer.class);

	/**
	 * The step is assumed to be declared as a <code>p:pipeline</code>.
	 */
	public XProcBasedTransformer(URI xproc, Map<QName,InputValue<?>> options) {
		this.xproc = xproc;
		if (options != null) {
			this.options = new HashMap<>();
			for (QName option : options.keySet())
				this.options.put(option, options.get(option).mult(-1));
		} else
			this.options = null;
	}

	/**
	 * @param step is optional
	 * @param monitor is ignored
	 * @param properties is ignored
	 */
	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		return new TransformerImpl(runtime, step);
	}

	private class TransformerImpl implements XProcStep {

		private final XdmNode xproc;
		private final XProcRuntime runtime;
		private final XAtomicStep step;

		public TransformerImpl(XProcRuntime runtime, XAtomicStep step) {
			if (runtime == null)
				throw new NullPointerException("runtime may not be null");
			this.runtime = runtime;
			this.step = step;
			xproc = runtime.parse(XProcBasedTransformer.this.xproc.toASCIIString(), "");
		}

		private ReadablePipe source = null;
		private WritablePipe result = null;
		private final Map<net.sf.saxon.s9api.QName,RuntimeValue> params = new HashMap<>();

		@Override
		public void setInput(String port, ReadablePipe pipe) {
			source = pipe;
		}

		@Override
		public void setOutput(String port, WritablePipe pipe) {
			result = pipe;
		}

		@Override
		public void reset() {
			source.resetReader();
			result.resetWriter();
		}

		@Override
		public void setParameter(net.sf.saxon.s9api.QName name, RuntimeValue value) {
			setParameter("parameters", name, value);
		}

		@Override
		public void setParameter(String port, net.sf.saxon.s9api.QName name, RuntimeValue value) {
			if ("parameters".equals(port))
				params.put(name, value);
			else
				throw new XProcException("No parameters allowed on port '" + port + "'");
		}

		@Override
		public void setOption(net.sf.saxon.s9api.QName name, RuntimeValue value) {
			// ignore, should not happen
		}

		@Override
		public void run() throws SaxonApiException {
			XProcRuntime innerRuntime = new XProcRuntime(runtime);
			XPipeline pipeline = null;
			try {
				pipeline = innerRuntime.use(xproc);
				// these checks should ideally be done when TransformerImpl is created
				String inputPort = null; {
					for (String port : pipeline.getInputs())
						if (!pipeline.getInput(port).getParameters())
							if (inputPort != null)
								throw new XProcException(step, "Pipeline must have exactly one input port");
							else
								inputPort = port;
					if (inputPort == null)
						throw new XProcException(step, "Pipeline must have exactly one input port"); }
				if (pipeline.getOutputs().size() != 1)
					throw new XProcException(step, "Pipeline must have exactly one output port");
				pipeline.clearInputs(inputPort);
				if (source != null)
					while (source.moreDocuments())
						pipeline.writeTo(inputPort, source.read());
				if (params != null)
					for (net.sf.saxon.s9api.QName name : params.keySet())
						pipeline.setParameter(name, params.get(name));
				if (options != null)
					for (QName option : options.keySet())
						pipeline.passOption(
							new net.sf.saxon.s9api.QName(option),
							XMLCalabashOptionValue.of(options.get(option).get()).asRuntimeValue());
				pipeline.run();
				for (String port : pipeline.getOutputs()) {
					ReadablePipe pipe = pipeline.readFrom(port);
					pipe.canReadSequence(true);
					while (pipe.moreDocuments()) {
						XdmNode doc = pipe.read();
						TreeWriter tree = new TreeWriter(runtime);
						tree.startDocument(doc.getBaseURI());
						tree.addSubtree(doc);
						tree.endDocument();
						result.write(tree.getResult());
					}
				}
			} catch (XProcException e) {
				if (step != null)
					throw e.rebase(step.getLocation());
				else
					throw e;
			} finally {
				if (pipeline != null) {
					for (XdmNode doc : pipeline.errors())
						if (step != null)
							step.reportError(doc);
						else
							logger.debug(doc.toString());
				}
				innerRuntime.close();
				runtime.resetExtensionFunctions();
			}
		}
	}
}
