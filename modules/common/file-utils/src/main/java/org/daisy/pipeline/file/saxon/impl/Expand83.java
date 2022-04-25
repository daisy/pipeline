package org.daisy.pipeline.file.saxon.impl;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import static org.daisy.pipeline.file.FileUtils.expand83;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:file-expand83",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class Expand83 extends ExtensionFunctionDefinition {

	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "file-expand83");

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] { SequenceType.SINGLE_STRING };
	}

	@Override
	public StructuredQName getFunctionQName() {
		return funcname;
	}

	@Override
	public SequenceType getResultType(SequenceType[] arg0) {
		return SequenceType.SINGLE_STRING;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			@Override
			public Sequence call(XPathContext context, Sequence[] arguments)
					throws XPathException {
				String uri = ((AtomicSequence) arguments[0]).getStringValue();
				try {
					uri = expand83(uri);
				} catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:file-expand83("+uri+")", e);
				}
				return new StringValue(uri, BuiltInAtomicType.STRING);
			}
		};
	}
}
