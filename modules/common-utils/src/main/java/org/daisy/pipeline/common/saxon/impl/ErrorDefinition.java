package org.daisy.pipeline.common.saxon.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CharMatcher;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;

import org.osgi.service.component.annotations.Component;

import org.slf4j.helpers.MessageFormatter;

@Component(
	name = "pf:error",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class ErrorDefinition extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "error");
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 2;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 3;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING,
			SequenceType.ATOMIC_SEQUENCE,
			SequenceType.OPTIONAL_QNAME
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.EMPTY_SEQUENCE;
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					String msg = CharMatcher.WHITESPACE.trimAndCollapseFrom(
						arguments[0].head().getStringValue(), ' ');
					if (arguments.length > 1) {
						String[] args = MessageDefinition.sequenceToArray(arguments[1]);
						msg = MessageFormatter.format(msg, args).getMessage();
					}
					XPathException xe = new XPathException(msg);
					if (arguments.length > 2) {
						Item i = arguments[2].head();
						if (i != null) {
							StructuredQName code = ((QNameValue)i).getStructuredQName();
							xe.setErrorCodeQName(code);
						}
					}
					throw xe; }
				catch (XPathException e) {
					throw e; }
				catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:error", e); }
			}
		};
	}
}
