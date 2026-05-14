package org.daisy.pipeline.image.saxon.impl;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.URLDecoder;

import javax.imageio.ImageIO;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.value.BigIntegerValue;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pf:image-dimensions",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class ImageDimensions extends ExtensionFunctionDefinition {

	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "image-dimensions");

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] { SequenceType.SINGLE_STRING };
	}

	public StructuredQName getFunctionQName() {
		return funcname;
	}

	public SequenceType getResultType(SequenceType[] arg0) {
		return SequenceType.ATOMIC_SEQUENCE; // NUMERIC_SEQUENCE
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments)
					throws XPathException {
				try {
					String path = ((StringValue) arguments[0])
							.getStringValue();
					BufferedImage image = ImageIO.read(new URL(URLDecoder
							.decode(path)));
					return new LazySequence(
							new ArrayIterator (new IntegerValue[] {
									new BigIntegerValue(image.getWidth()),
									new BigIntegerValue(image.getHeight()) }));
				} catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:image-dimensions", e);
				}
			}
		};
	}

	private static final Logger logger = LoggerFactory
			.getLogger(ImageDimensions.class);
}
