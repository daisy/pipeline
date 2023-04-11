package org.daisy.common.transform;

import javax.xml.namespace.QName;

/**
 * Unchecked version of {@link javax.xml.transform.TransformerException} with an extra "code"
 * argument.
 */
@SuppressWarnings("serial")
public class TransformerException extends RuntimeException {
	
	private final QName code;
	
	public TransformerException(Throwable cause) {
		this(null, cause);
	}
	
	public TransformerException(QName code, Throwable cause) {
		super(cause.getMessage(), cause);
		this.code = code;
	}
	
	public QName getCode() {
		return code;
	}
	
	public static TransformerException wrap(Throwable e) {
		if (e instanceof TransformerException)
			return (TransformerException)e;
		else
			return new TransformerException(e);
	}
}
