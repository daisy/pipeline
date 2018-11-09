package org.daisy.dotify.common.xml;

import java.nio.charset.Charset;

/**
 * Provides information about a preliminary charset detection.
 * @author Joel Håkansson
 */
class PreliminaryCharset {
	private final Charset charset;
	private final boolean bom;
	private final boolean exactMatch;
	
	/**
	 * Provides a builder for preliminary charset information.
	 */
	static class Builder {
		private final Charset charset;
		private boolean bom = false;
		private boolean exactMatch = false;
		Builder(Charset charset) {
			this.charset = charset;
		}
		/**
		 * True if there is a byte order mark, false otherwise.
		 * @param value the value
		 * @return returns this builder
		 */
		Builder bom(boolean value) {
			this.bom = value;
			return this;
		}
		/**
		 * True if the charset given is a definite match, false if it
		 * only can be used to read the xml declaration.
		 * @param value the value
		 * @return returns this builder.
		 */
		Builder exactMatch(boolean value) {
			this.exactMatch = value;
			return this;
		}
		/**
		 * Creates an new {@link #PreliminaryCharset} instance. 
		 * @return returns a new instance
		 */
		PreliminaryCharset build() {
			return new PreliminaryCharset(this);
		}
	}

	private PreliminaryCharset(Builder builder) {
		this.charset = builder.charset;
		this.bom = builder.bom;
		this.exactMatch = builder.exactMatch;
	}

	/**
	 * Gets the charset.
	 * @return returns the charset
	 */
	Charset getCharset() {
		return charset;
	}
	
	/**
	 * Returns true if the encoding has a byte order mark, false otherwise.
	 * @return returns true if the encoding has a byte order mark
	 */
	boolean hasBom() {
		return bom;
	}
	
	/**
	 * Returns true if the detected charset is an exact match, false if
	 * the charset only can be used to read the xml declaration. 
	 * @return returns true if the charset is an exact match, false otherwise
	 */
	boolean isExactMatch() {
		return exactMatch;
	}
}
