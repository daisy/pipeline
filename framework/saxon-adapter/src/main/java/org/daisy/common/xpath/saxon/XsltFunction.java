package org.daisy.common.xpath.saxon;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.Xslt30Transformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import org.xml.sax.InputSource;

public class XsltFunction extends ExtensionFunctionDefinition {

	private final StructuredQName functionName;
	private final URL xslt;

	public XsltFunction(URL xslt, StructuredQName functionName) {
		this.functionName = functionName;
		this.xslt = xslt;
	}

	@Override
	public StructuredQName getFunctionQName() {
		return functionName;
	}

	// allow any number of argument and of any type
	@Override
	public int getMinimumNumberOfArguments() {
		return 0;
	}

	@Override
	public int getMaximumNumberOfArguments() {
		return 20;
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] { SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE,
		                            SequenceType.ANY_SEQUENCE };
	}

	// allow any type of result
	@Override
	public SequenceType getResultType(SequenceType[] arg) {
		return SequenceType.ANY_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				Xslt30Transformer xslt; {
					try {
						xslt = compileXslt(XsltFunction.this.xslt, context.getConfiguration());
					} catch (SaxonApiException|IOException|URISyntaxException e) {
						throw new XPathException("Failed to compile XSLT: " + XsltFunction.this.xslt, e);
					}
				}
				XdmValue[] xdmArguments = new XdmValue[arguments.length];
				for (int i = 0; i < arguments.length; i++) {
					xdmArguments[i] = XdmValue.wrap(arguments[i]);
				}
				try {
					return xslt.callFunction(new QName(functionName), xdmArguments).getUnderlyingValue();
				} catch (SaxonApiException e) {
					throw new XPathException("Failed to call XSLT function: " + functionName, e);
				}
			}
		};
	}

	private static Xslt30Transformer compileXslt(URL xslt, Configuration config) throws SaxonApiException, IOException, URISyntaxException {
		Processor processor = new Processor(config);
		XsltCompiler compiler = processor.newXsltCompiler();
		compiler.setSchemaAware(processor.isSchemaAware());
		DocumentBuilder builder = processor.newDocumentBuilder();
		builder.setBaseURI(xslt.toURI());
		XsltExecutable executable = compiler.compile(
			builder.build(new SAXSource(new InputSource(xslt.openStream()))).asSource());
		return executable.load30();
	}
}
