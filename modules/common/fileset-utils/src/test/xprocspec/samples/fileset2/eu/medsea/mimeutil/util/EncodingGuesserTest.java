/*
 * Copyright 2007-2009 Medsea Business Solutions S.L.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.medsea.mimeutil.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import eu.medsea.util.EncodingGuesser;
import junit.framework.TestCase;

public class EncodingGuesserTest extends TestCase {

	public void setUp() {
		EncodingGuesser.setSupportedEncodings(EncodingGuesser.getCanonicalEncodingNamesSupportedByJVM());
	}

	public void tearDown() {
		EncodingGuesser.setSupportedEncodings(new ArrayList());
	}

	public void test_UTF_8_EncodingWithAndWithoutBOM() {

		byte [] utf8_bom = new byte [] {(byte)0xEF, (byte)0xBB, (byte)0xBF};
		String message = "This is a message which should always be in UTF-8 format because of javas unicode strings.";

		byte [] data = concatenateByteArrays(utf8_bom, message.getBytes());

		Collection encodings = EncodingGuesser.getPossibleEncodings(data);
		assertTrue(encodings.size() == 1);
		assertTrue(encodings.contains("UTF-8"));

		encodings = EncodingGuesser.getPossibleEncodings(message.getBytes());
		assertTrue(encodings.size() != 1);
		assertTrue(encodings.contains("UTF-8"));
	}

	// Utility method to concatenate to byte arrays
	private byte [] concatenateByteArrays(byte [] a, byte [] b) {
		byte [] data = new byte[a.length + b.length];
		for(int i = 0; i < a.length; i++) {
			data[i] = a[i];
		}

		for(int i = 0; i < b.length; i++) {
			data[a.length + i] = b[i];
		}
		return data;
	}

	public void testInitialAndLimitedSetOfSupportedEncodings() {
		Collection currentEncodings = null;

		// Know text files
		String [] fileLocations = new String [] {"src/test/resources/e-svg.img", "src/test/resources/e.svg", "src/test/resources/e.xml",
				"src/test/resources/e[xml]", "src/test/resources/log4j.properties", "src/test/resources/magic.mime","src/test/resources/mime-types.properties",
				"src/test/resources/plaintext", "src/test/resources/plaintext.txt"};


		try {
			// We will time these runs and make sure that the limited list runs faster
			Date now = new Date();

			// Run with all currently supported encodings
			for(int i = 0; i < fileLocations.length; i++) {
				InputStream in = new FileInputStream(fileLocations[i]);
				// Try to read up to 1K of data
				byte [] data = new byte [1024];
				int length = in.read(data);
				in.close();
				if(length < 1024) {
					data = EncodingGuesser.getByteArraySubArray(data, 0, length);
				}
				Collection possibleEncodings = EncodingGuesser.getPossibleEncodings(data);

				// We expect a large number of matches per file one of which should be UTF-8
				assertTrue(possibleEncodings.size() != 1);
				assertTrue(possibleEncodings.contains("UTF-8"));
			}

			long time_1 = (new Date().getTime()) - now.getTime();



			// Now set the supported encodings to this limited list
			Collection limitedEncodingList = new ArrayList();
			limitedEncodingList.add("windows-1251");
			currentEncodings = EncodingGuesser.setSupportedEncodings(limitedEncodingList);

			// We don't want to include the initialisation in the calculation
			now = new Date();

			for(int i = 0; i < fileLocations.length; i++) {
				InputStream in = new FileInputStream(fileLocations[i]);
				// Try to read up to 1K of data
				byte [] data = new byte [1024];
				int length = in.read(data);
				in.close();
				if(length < 1024) {
					data = EncodingGuesser.getByteArraySubArray(data, 0, length);
				}
				Collection possibleEncodings = EncodingGuesser.getPossibleEncodings(data);

				// We expect only one match per file and that should be windows-1251
				assertTrue(possibleEncodings.size() == 1);
				assertTrue(possibleEncodings.contains("windows-1251"));
			}

			long time_2 = (new Date().getTime()) - now.getTime();

			// The time difference should be quite significant.
			// On my machine just for these eight files the times recorder were
			// time_1 was approximately=320 and time_2 was approximately=0
			// These were fairly consistent.

			assertTrue(time_2 < time_1);

		}catch(Exception e) {
			fail("Should not get here.");
		}finally {
		// Reset to the original Collection
			if(currentEncodings != null){
				EncodingGuesser.setSupportedEncodings(currentEncodings);
			}
		}
	}

}
