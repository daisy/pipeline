package org.daisy.pipeline.braille.common.calabash;

import java.net.URI;
import java.util.Map;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.extensions.Eval;
import com.xmlcalabash.io.ReadableDocument;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.s9api.QName;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

/**
 * Simple XProc (<code>p:pipeline</code>) based transformer that can be executed in a XMLCalabash runtime.
 */
public class CxEvalBasedTransformer implements XProcStepProvider {

	private final URI xprocUri;
	private final javax.xml.namespace.QName xprocType;
	private final Map<String,String> xprocOptions;

	private static final QName _name = new QName("name");
	private static final QName _namespace = new QName("namespace");
	private static final QName _value = new QName("value");
	private static final QName _step = new QName("step");

	/**
	 * The step is assumed to be declared as a <code>p:pipeline</code>.
	 */
	public CxEvalBasedTransformer(URI uri, javax.xml.namespace.QName name, Map<String,String> options) {
		this.xprocUri = uri;
		this.xprocType = name;
		this.xprocOptions = options;
	}

	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new PreparedEval(runtime, step);
	}

	private class PreparedEval extends Eval implements XProcStep {
		public PreparedEval(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
			setInput("pipeline", new ReadableDocument(runtime) {
				private boolean readDoc = false;
				@Override
				protected void readDoc() {
					if (readDoc) return;
					readDoc = true;
					documents.add(runtime.parse(xprocUri.toASCIIString(), "")); }});
			if (xprocType != null) {
				setOption(_step, new RuntimeValue() { public QName getQName() { return new QName(xprocType); }});
				throw new RuntimeException("p:library not supported due to a bug in cx:eval"); }
			if (xprocOptions != null)
				setInput("options", new ReadableDocument(runtime) {
					private boolean readDoc = false;
					@Override
					protected void readDoc() {
						if (readDoc) return;
						readDoc = true;
						TreeWriter optionWriter = new TreeWriter(runtime);
						optionWriter.startDocument(step.getNode().getBaseURI());
						optionWriter.addStartElement(cx_options);
						optionWriter.startContent();
						for (String option : xprocOptions.keySet()) {
							optionWriter.addStartElement(cx_option);
							optionWriter.addAttribute(_name, option);
							optionWriter.addAttribute(_namespace, "");
							optionWriter.addAttribute(_value, xprocOptions.get(option));
							optionWriter.startContent();
							optionWriter.addEndElement(); }
						optionWriter.addEndElement();
						optionWriter.endDocument();
						documents.add(optionWriter.getResult()); }});
		}
		public void setParameter(String port, QName name, RuntimeValue value) {
			if ("parameters".equals(port))
				setParameter(name, value);
			else
				super.setParameter(port, name, value);
		}
	}
}
