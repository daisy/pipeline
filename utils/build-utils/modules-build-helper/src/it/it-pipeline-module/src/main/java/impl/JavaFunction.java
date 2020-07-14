package impl;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:java-function",
	service = { ExtensionFunctionDefinition.class }
)
public class JavaFunction extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "java-function");
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.ATOMIC_SEQUENCE;
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				String string = arguments[0].head().getStringValue();
				return new StringValue(string.toUpperCase());
			}
		};
	}
}
