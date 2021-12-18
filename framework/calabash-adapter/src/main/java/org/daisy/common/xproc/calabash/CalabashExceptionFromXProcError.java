package org.daisy.common.xproc.calabash;

import com.xmlcalabash.core.XProcException;

import net.sf.saxon.s9api.QName;

import org.daisy.common.xproc.XProcError;

/**
 * Wraps a {@link XProcError} in a {@link XProcException}.
 *
 * Note that this class does not extend {@link org.daisy.common.xproc.XProcErrorException}.
 */
public class CalabashExceptionFromXProcError extends XProcException {

	private final XProcError error;

	private CalabashExceptionFromXProcError(XProcError error) {
		super(error.getCode() == null ? null : new QName(error.getCode()),
		      error.getLocation(),
		      error.getMessage(),
		      CalabashExceptionFromXProcError.from(error.getCause()));
		this.error = error;
	}

	public static XProcException from(XProcError error) {
		if (error == null)
			return null;
		else if (error instanceof CalabashXProcError)
			return ((CalabashXProcError)error).getCalabashException();
		else
			return new CalabashExceptionFromXProcError(error);
	}

	XProcError getXProcError() {
		return error;
	}
}
