package org.daisy.pipeline.persistence.impl.messaging;

import java.io.Serializable;
/**
 * Required modification of throwable for the persistent errors messages
 * @author nicol
 *
 */
public class SerializedThrowable extends Throwable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1766409694788631750L;

}
