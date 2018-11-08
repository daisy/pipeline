package org.daisy.common.transform;

/**
 * Unchecked version of javax.xml.transform.TransformerException
 */
@SuppressWarnings("serial")
public class TransformerException extends RuntimeException {
	
	public TransformerException(Throwable cause) {
		super(cause);
	}
}
