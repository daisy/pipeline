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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.daisy.braille.api.validator.ValidatorFactoryService;

/**
 * Provides a facade for PEFValidator
 * @author Joel Håkansson
 */
public class PEFValidatorFacade {
	private final ValidatorFactoryService factory;
	
	public PEFValidatorFacade(ValidatorFactoryService factory) {
		this.factory = factory;
	}
	
	/**
	 * Validates the supplied PEF-file
	 * @param in the PEF-file to validate
	 * @return returns true if PEF-file is valid and validation was successful, false otherwise 
	 * @throws IOException throws IOException if an error occurred
	 */
	public boolean validate(File in) throws IOException {
		return validate(in, null);
	}
	
	/**
	 * Validates the supplied PEF-file and sends the validator messages to the supplied PrintStream
	 * @param in the PEF-file to validate
	 * @param msg the PrintStream to send validator messages to
	 * @return returns true if PEF-file is valid and validation was successful, false otherwise 
	 * @throws IOException throws IOException if an error occurred
	 */
	public boolean validate(File in, PrintStream msg) throws IOException {
		if (!in.exists()) {
			throw new FileNotFoundException("File does not exist: " + in);
		}
		org.daisy.braille.api.validator.Validator pv = factory.newValidator(PEFValidator.class.getCanonicalName());
		if (pv == null) {
			throw new IOException("Could not find validator.");
		}
		if (msg!=null) {
			msg.println("Validating " + in + " using \"" + pv.getDisplayName() + "\": " + pv.getDescription());
		}
		boolean ok = pv.validate(in.toURI().toURL());
		if (msg!=null) {
			msg.println("Validation was " + (ok ? "succcessful" : "unsuccessful"));
		}
		if (!ok && msg!=null) {
			msg.println("Messages returned by the validator:");
			InputStreamReader report = new InputStreamReader(pv.getReportStream());
			int c;
			while ((c = report.read()) != -1) {
				msg.print((char)c);
			}
			report.close();
			return ok;
		}
		return ok;
	}
}
