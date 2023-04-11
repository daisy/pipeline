package org.daisy.common.xproc;

import javax.xml.namespace.QName;

import org.daisy.common.transform.TransformerException;

public class XProcErrorException extends TransformerException {
	
	private final XProcError error;
	
	public XProcErrorException(XProcError error, Throwable cause) {
		super(cause);
		this.error = error;
	}
	
	public XProcError getXProcError() {
		return error;
	}
	
	@Override
	public QName getCode() {
		return error.getCode();
	}
	
	@Override
	public String getMessage() {
		return error.getMessage();
	}
	
	@Override
	public String toString() {
		return error.toString();
	}
}
