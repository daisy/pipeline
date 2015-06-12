/*
 * Braille Utils (C) 2010-2011 Daisy Consortium 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.api.embosser;

/**
 * Provides an exception to indicate that the Contract is not supported
 * @author Joel Håkansson
 *
 */
public class ContractNotSupportedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4350090838306339507L;

	public ContractNotSupportedException() {
	}

	public ContractNotSupportedException(String message) {
		super(message);
	}

	public ContractNotSupportedException(Throwable cause) {
		super(cause);
	}

	public ContractNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

}
