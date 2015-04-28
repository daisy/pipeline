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
package org.daisy.braille.embosser;

/**
 * Provides an EmbosserFactory exception
 * @author Joel Håkansson
 *
 */
public class EmbosserFactoryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1266357428040633688L;

	public EmbosserFactoryException() { }

	public EmbosserFactoryException(String message) {
		super(message);
	}

	public EmbosserFactoryException(Throwable cause) {
		super(cause);
	}

	public EmbosserFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
