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
 * Provides an unsupported paper exception 
 * @author Joel Håkansson
 */
public class UnsupportedPaperException extends EmbosserFactoryException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4972964705499633760L;

	public UnsupportedPaperException() { }

	public UnsupportedPaperException(String message) {
		super(message);
	}

	public UnsupportedPaperException(Throwable cause) {
		super(cause);
	}

	public UnsupportedPaperException(String message, Throwable cause) {
		super(message, cause);
	}
}
