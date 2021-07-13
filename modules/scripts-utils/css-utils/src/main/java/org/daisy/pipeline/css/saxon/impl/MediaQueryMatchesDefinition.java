package org.daisy.pipeline.css.saxon.impl;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

import org.daisy.pipeline.css.Medium;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:media-query-matches",
	service = { ExtensionFunctionDefinition.class }
)
public class MediaQueryMatchesDefinition extends ExtensionFunctionDefinition {

	private static final StructuredQName funcname = new StructuredQName(
		"pf",
		"http://www.daisy.org/ns/pipeline/functions",
		"media-query-matches");

	public StructuredQName getFunctionQName() {
		return funcname;
	}

	@Override
	public int getMinimumNumberOfArguments() {
		return 2;
	}

	@Override
	public int getMaximumNumberOfArguments() {
		return 2;
	}

	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING,
			SequenceType.SINGLE_STRING
		};
	}

	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_BOOLEAN;
	}

	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					String mediaQuery = arguments[0].head().getStringValue();
					Medium medium = Medium.parse(arguments[1].head().getStringValue());
					return BooleanValue.get(medium.matches(mediaQuery));
				} catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:media-query-matches", e);
				}
			}
		};
	}
}
