package org.daisy.pipeline.file.saxon.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.SequenceType;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.file.FileUtils;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:file-exists",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class FileExists extends ExtensionFunctionDefinition {

	private static final StructuredQName funcname = new StructuredQName(
		"pf",
		"http://www.daisy.org/ns/pipeline/functions",
		"file-exists");

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
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					String uri = arguments[0].head().getStringValue();
					return new BooleanValue(exists(URLs.asURI(uri)), BuiltInAtomicType.BOOLEAN);
				} catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:file-exists", e);
				}
			}
		};
	}

	/**
	 * Whether the file or ZIP entry denoted by this absolute URI exists.
	 */
	private static boolean exists(URI uri) throws URISyntaxException {
		try {
			return Files.exists(FileUtils.asPath(uri));
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
