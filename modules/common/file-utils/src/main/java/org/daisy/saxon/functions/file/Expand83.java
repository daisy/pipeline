package org.daisy.saxon.functions.file;

import java.io.File;
import java.net.URI;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

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
				uri = Expand83.expand83(uri);
				return new StringValue(uri, BuiltInAtomicType.STRING);
			}
		};
	}

	/**
	 * Expands 8.3 encoded path segments.
	 *
	 * For instance `C:\DOCUME~1\file.xml` will become `C:\Documents and
	 * Settings\file.xml`
	 */
	public static String expand83(String uri) throws XPathException {
		if (uri == null || !uri.startsWith("file:/")) {
			return uri;
		}

		try {
			URI u = new URI(uri);
			File file = new File(new URI(u.getScheme(), u.getSchemeSpecificPart(), null));
			if (!file.exists()) {
				return uri;
			}
			URI expandedUri = expand83(file);
			if (expandedUri == null) {
				return uri;
			} else {
				return new URI(expandedUri.getScheme(), expandedUri.getSchemeSpecificPart(), u.getFragment()).toString();
			}

		} catch (Exception e) {
			throw new XPathException("pf:file-expand83("+uri+") failed", e);
		}
	}

	/**
	 * this is extracted out of `expand83(String)` because it can be unit tested
	 * with a custom File implementation.
	 */
	public static URI expand83(File file) throws XPathException {
		try {
			if (file.exists()) {
				return file.getCanonicalFile().toURI();
			} else {
				return file.toURI();
			}
		} catch (Exception e) {
			throw new XPathException("pf:file-expand83("+file+") failed", e);
		}
	}

}
