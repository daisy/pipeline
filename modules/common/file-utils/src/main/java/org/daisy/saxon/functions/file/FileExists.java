package org.daisy.saxon.functions.file;

import java.io.File;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:file-exists",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class FileExists extends ExtensionFunctionDefinition {

	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "file-exists");

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
		return SequenceType.SINGLE_BOOLEAN;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Sequence call(XPathContext context, Sequence[] arguments)
					throws XPathException {

				try {
					String path = ((AtomicSequence) arguments[0])
							.getStringValue();
					boolean result = (path.isEmpty()) ? true : new File(path)
							.exists();
					return new BooleanValue(result, BuiltInAtomicType.BOOLEAN);
				} catch (Exception e) {
					throw new XPathException("pf:file-exists failed", e);
				}
			}
		};
	}

}
