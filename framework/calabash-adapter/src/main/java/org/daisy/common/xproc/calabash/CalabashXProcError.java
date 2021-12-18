package org.daisy.common.xproc.calabash;

import javax.xml.namespace.QName;
import javax.xml.transform.SourceLocator;

import static org.daisy.common.saxon.SaxonHelper.jaxpQName;
import org.daisy.common.xproc.XProcError;

import com.xmlcalabash.core.XProcException;

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
		return e.getMessage();
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
