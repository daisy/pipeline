package org.daisy.braille.pef;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

public class PEFValidatorTest {

	@Test
	public void testValidationValid() throws IOException {
		URL input = this.getClass().getResource("resource-files/PEFBookTestInput.pef");
		PEFValidator v = new PEFValidator();
		boolean ret = v.validate(input);

		InputStream is = v.getReportStream();
		int r;
		while ((r = is.read()) > -1) {
			System.out.print((char) r);
		}
		assertTrue(ret);
	}

	@Test
	public void testValidationNotValid() throws IOException {
		URL input = this.getClass().getResource("resource-files/PEFBookTestInputNotValid.pef");
		PEFValidator v = new PEFValidator();
		assertTrue(!v.validate(input));

		InputStream is = v.getReportStream();
		int r;
		while ((r = is.read()) > -1) {
			System.out.print((char) r);
		}
	}

	public static void main(String[] args) throws IOException {
		URL input = PEFValidatorTest.class.getResource("resource-files/PEFBookTestInput.pef");
		PEFValidator v = new PEFValidator();
		boolean ret = v.validate(input);

		InputStream is = v.getReportStream();
		int r;
		while ((r = is.read()) > -1) {
			System.out.print((char) r);
		}
		System.out.println(ret);
	}
}
