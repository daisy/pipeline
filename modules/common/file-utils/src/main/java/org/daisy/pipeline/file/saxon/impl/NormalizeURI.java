package org.daisy.pipeline.file.saxon.impl;

import java.net.URI;
import java.net.URISyntaxException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import static org.daisy.pipeline.file.FileUtils.normalizeURI;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:normalize-uri",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class NormalizeURI extends ExtensionFunctionDefinition {

	private static final StructuredQName funcname = new StructuredQName(
		"pf",
		"http://www.daisy.org/ns/pipeline/functions",
		"normalize-uri");

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.OPTIONAL_STRING,
			SequenceType.SINGLE_BOOLEAN
		};
	}

	@Override
	public int getMinimumNumberOfArguments() {
		return 1;
	}

	@Override
	public int getMaximumNumberOfArguments() {
		return 2;
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

			@SuppressWarnings({ "unchecked", "rawtypes" })
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					URI uri; {
						Item i = arguments[0].head();
						try {
							uri = new URI(i != null ? i.getStringValue().trim() : "");
						} catch (URISyntaxException e) {
							throw new XPathException("Not a valid URI: " + i, e);
						}
					}
					boolean fragment = arguments.length > 1 ? ((BooleanValue)arguments[1]).getBooleanValue() : true;
					return new StringValue(normalizeURI(uri, !fragment).toASCIIString(), BuiltInAtomicType.STRING);
				} catch (XPathException e) {
					throw e;
				} catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:normalize-uri", e);
				}
			}
		};
	}
}
