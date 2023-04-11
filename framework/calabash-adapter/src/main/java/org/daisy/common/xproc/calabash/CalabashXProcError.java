package org.daisy.common.xproc.calabash;

import javax.xml.namespace.QName;
import javax.xml.transform.SourceLocator;

import static org.daisy.common.saxon.SaxonHelper.jaxpQName;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.xproc.XProcError;

import com.xmlcalabash.core.XProcException;

import net.sf.saxon.trans.XPathException;

/**
 * Wraps a {@link XProcException} in a {@link XProcError}.
 */
public class CalabashXProcError extends XProcError {

	private final XProcException e;

	private CalabashXProcError(XProcException e) {
		this.e = e;
	}

	public QName getCode() {
		if (e.getErrorCode() != null)
			return jaxpQName(e.getErrorCode());
		else
			return null;
	}

	public String getMessage() {
		Throwable e = this.e;
		while (e instanceof XProcException || e instanceof XPathException || e instanceof TransformerException) {
			String message = e.getMessage();
			if (message != null)
				return message;
			e = e.getCause();
		}
		if (e == null)
			return null;
		String message = null;
		while (e != null) {
			String m = e.getClass().getName();
			if (message == null)
				message = m;
			else
				message = message + ": " + m;
			m = e.getMessage();
			if (m != null) {
				message = message + ": " + m;
				break;
			}
			e = e.getCause();
		}
		return message;
	}

	public XProcError getCause() {
		return CalabashXProcError.from(e.getErrorCause());
	}

	public SourceLocator[] getLocation() {
		return e.getLocation();
	}

	public static XProcError from(XProcException e) {
		if (e == null)
			return null;
		else if (e instanceof CalabashExceptionFromXProcError)
			return ((CalabashExceptionFromXProcError)e).getXProcError();
		else
			return new CalabashXProcError(e);
	}

	XProcException getCalabashException() {
		return e;
	}
}
