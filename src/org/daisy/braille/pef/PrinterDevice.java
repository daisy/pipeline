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
package org.daisy.braille.pef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.DocAttributeSet;

import org.daisy.braille.api.embosser.Device;

/**
 * Printer device class of type DocFlavor.INPUT_STREAM.AUTOSENSE
 * 
 * This class can be used when sending a file to a printer.
 * 
 * @author  Joel Håkansson
 * @version 3 jul 2008
 */
public class PrinterDevice implements Device {
	//private final static DocFlavor FLAVOR = DocFlavor.BYTE_ARRAY.AUTOSENSE;
	private final static DocFlavor FLAVOR = DocFlavor.INPUT_STREAM.AUTOSENSE;
	private PrintService service;
	
	/**
	 * Create a device with the provided name.
	 * @param deviceName the name of the device
	 * @param fuzzyLookup If true, the returned device is any device whose name contains the 
	 * supplied deviceName. If false, the returned device name equals the supplied deviceName. 
	 * @throws IllegalArgumentException if no device is found.
	 */
	public PrinterDevice(String deviceName, boolean fuzzyLookup) {
		PrintService[] printers = PrintServiceLookup.lookupPrintServices(FLAVOR, null);
		for (PrintService p : printers) {
			if (p.getName().equals(deviceName)) {
				service = p;
				return;
			}
		}
		if (fuzzyLookup) {
			PrintService match = null;
			double currentMatch = 0;
			for (PrintService p : printers) {
				if (p.getName().contains(deviceName)) {
					double thisMatch = deviceName.length() / (double)p.getName().length();
					if (thisMatch > currentMatch) {
						currentMatch = thisMatch;
						match = p;
					}
				}
			}
			if (match != null) {
				service = match;
				return;
			}			
		}
		throw new IllegalArgumentException("Could not find embosser.");
	}
	
	/**
	 * List available devices
	 * @return returns a list of available devices that accepts DocFlavor.INPUT_STREAM.AUTOSENSE 
	 */
	public static PrintService[] getDevices() {
		PrintService[] printers = PrintServiceLookup.lookupPrintServices(FLAVOR, null);
		return printers;
	}

	/**
	 * Transmit a file to the device
	 * @param file the file to transmit
	 * @throws PrintException
	 */
	public void transmit(File file) throws PrintException {
		try {
			transmit(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new PrintException(e);
		}
	}
	
	private void transmit(InputStream is) throws PrintException {
		InputStreamDoc doc = new InputStreamDoc(is);
		DocPrintJob dpj = service.createPrintJob();
		dpj.print(doc, null);
	}

	private class InputStreamDoc implements Doc {
		private InputStream stream;
		
		/**
		 * Default constructor
		 * @param file
		 * @throws FileNotFoundException
		 */
		public InputStreamDoc(InputStream stream) {
			this.stream = stream;
		}

		public DocAttributeSet getAttributes() {
			return null;
		}

		public DocFlavor getDocFlavor() {
			return FLAVOR;
		}

		public Object getPrintData() throws IOException {
			return getStreamForBytes();
		}

		public Reader getReaderForText() throws IOException {
			return null;
		}

		public InputStream getStreamForBytes() throws IOException {
			return stream;
		}
	}
}
