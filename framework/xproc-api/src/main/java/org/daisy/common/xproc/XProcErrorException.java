package org.daisy.common.xproc;

public class XProcErrorException extends Exception {
	
	private final XProcError error;
	
	public XProcErrorException(XProcError error, Throwable cause) {
		super(cause);
		this.error = error;
	}
	
	public XProcError getXProcError() {
		return error;
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
