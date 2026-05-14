package eu.medsea.mimeutil.detector;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
// import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.MimeUtil2;
import eu.medsea.mimeutil.TextMimeDetector;
import eu.medsea.mimeutil.TextMimeType;
import eu.medsea.mimeutil.handler.TextMimeHandler;
import eu.medsea.util.EncodingGuesser;
import eu.medsea.util.StringUtil;

import junit.framework.TestCase;

public class TextMimeDetectorTest extends TestCase {

	static {
		// We can never register or unregister the TextMimeDetector. This is coded in and will
		// be used only when no mime type has been returned from any other registered MimeDetector.

		// In this case there are NO other registered MimeDetectors so it acts as a default.
		// The following would result in an Exception being thrown
		// MimeUtil.registerMimeDetector("eu.medsea.mimeutil.TextMimeDetector");


	}

	MimeUtil2 mimeUtil = new MimeUtil2();

	public void setUp() {
		// We will initialise the encodings with all those supported by the JVM
		EncodingGuesser.setSupportedEncodings(EncodingGuesser.getCanonicalEncodingNamesSupportedByJVM());
	}

	public void tearDown() {
		EncodingGuesser.setSupportedEncodings(new ArrayList());
	}

	public void testBoundaryCases( ){
		assertEquals(mimeUtil.getMimeTypes(new File("src/test/resources/porrasturvat-1.0.3.tar.gz")), "application/octet-stream");
	}

	// We don't register any MimeDetector(s) so the default TextMimeDetector will be used
	public void testGetMimeTypesFile() {

		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/a.html")).contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/b-jpg.img")).contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/b.jpg")).contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/c-gif.img")).contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/c.gif")).contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/d-png.img")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e-svg.img")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e.svg")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e.xml")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e[xml]")).contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/f.tar.gz")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/log4j.properties")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/magic.mime")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/mime-types.properties")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/plaintext")).contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/plaintext.txt")).contains("text/plain"));

		// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
		// and is therefore considered to be a text file. This is a small risk with small binary files.
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/test.bin")).contains("text/plain"));
	}

	public void testGetMimeTypesString() {
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/a.html").contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes("src/test/resources/b-jpg.img").contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes("src/test/resources/b.jpg").contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes("src/test/resources/c-gif.img").contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes("src/test/resources/c.gif").contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes("src/test/resources/d-png.img").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/e-svg.img").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/e.svg").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/e.xml").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/e[xml]").contains("text/plain"));
		assertFalse(mimeUtil.getMimeTypes("src/test/resources/f.tar.gz").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/log4j.properties").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/magic.mime").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/mime-types.properties").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/plaintext").contains("text/plain"));
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/plaintext.txt").contains("text/plain"));

		// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
		// and is therefore considered to be a text file. This is a small risk with small binary files.
		assertTrue(mimeUtil.getMimeTypes("src/test/resources/test.bin").contains("text/plain"));

	}

	public void testGetMimeTypesStringWithExtensionMimeDetector() {
		try {
			mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");

			assertTrue(mimeUtil.getMimeTypes("src/test/resources/a.html").contains("text/plain"));

			assertFalse(mimeUtil.getMimeTypes("src/test/resources/b-jpg.img").contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes("src/test/resources/b.jpg").contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes("src/test/resources/c-gif.img").contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes("src/test/resources/c.gif").contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes("src/test/resources/d-png.img").contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes("src/test/resources/d.png").contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes("src/test/resources/f.tar.gz").contains("text/plain"));

			assertTrue(mimeUtil.getMimeTypes("src/test/resources/e-svg.img").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/e.svg").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/e.xml").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/e[xml]").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/log4j.properties").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/magic.mime").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/mime-types.properties").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/plaintext").contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/plaintext.txt").contains("text/plain"));

			// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
			// and is therefore considered to be a text file. This is a small risk with small binary files.
			assertTrue(mimeUtil.getMimeTypes("src/test/resources/test.bin").contains("text/plain"));


			// As the ExtensionMimeDetector should also returned a text/plain MimeType for an extension of .txt
			// lets make sure the specificity has been updated and its still a TextMimeType
			Collection mimeTypes = mimeUtil.getMimeTypes("src/test/resources/plaintext.txt");
			assertTrue(mimeTypes.contains("text/plain"));
			Collection retain = new HashSet();
			retain.add("text/plain");
			mimeTypes.retainAll(retain);
			MimeType mimeType = (MimeType)mimeTypes.iterator().next();
			assertTrue(mimeType instanceof TextMimeType);
			assertTrue(((TextMimeType)mimeType).getSpecificity() == 2);

		}finally{
			// We want this to unregister no matter what
			mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		}
	}

	public void testGetMimeTypesInputStream() {
		try {

			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/a.html").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/b-jpg.img").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/b.jpg").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/c-gif.img").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/c.gif").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/d-png.img").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e-svg.img").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e.svg").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e.xml").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e[xml]").toURI().toURL().openStream()).contains("text/plain"));
			assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/f.tar.gz").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/log4j.properties").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/magic.mime").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/mime-types.properties").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/plaintext").toURI().toURL().openStream()).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/plaintext.txt").toURI().toURL().openStream()).contains("text/plain"));

			// Even though this is a binary file, due to it's small size (5 bytes) it has matched with a small number of encodings
			// and is therefore considered to be a text file. This is a small risk with small binary files.
			assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/test.bin").toURI().toURL().openStream()).contains("text/plain"));
		}catch(Exception e) {
			fail("Should never get here");
		}
	}

	public void testGetMimeTypesInputStreamAndEnsureStreamIsReset() {
		try {
			InputStream in = (new File("src/test/resources/a.html").toURI().toURL()).openStream();
			assertTrue(mimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(in).contains("text/plain"));
			assertTrue(mimeUtil.getMimeTypes(in).contains("text/plain"));

			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			// Read some text from the stream so we can check that it's definitely been reset in the getMimeTypes() method
			assertEquals("<html>", br.readLine()); // This is only contained in the first line of the file so reset must have happened correctly
		}catch(Exception e) {
			fail("Should never get here");
		}
	}

	public void testAddMimeHandler() {
		TextMimeDetector.registerTextMimeHandler(new XMLHandler());
		TextMimeDetector.registerTextMimeHandler(new SVGHandler());

		// Even though the next handler would match and change the mime subType it
		// will never fire as the SVGHandler returns true from it's handle(...)
		// method so no further Handler(s) will fire
		TextMimeDetector.registerTextMimeHandler(new NeverFireHandler());

		Collection c = mimeUtil.getMimeTypes("src/test/resources/e.xml");
		assertTrue(c.size() == 1);
		assertTrue(c.contains("text/xml"));

		c = mimeUtil.getMimeTypes("src/test/resources/e.svg");
		assertTrue(c.size() == 1);
		assertTrue(c.contains("image/svg+xml"));
	}

	/* We will add here new test for URL's that do not require an Internet connection
	 * so that the build will always work.
	public void testGetMimeTypesURL() {
		try {
			URL url = new URL("http://www.google.com/index.html");
			Collection mimeTypes = MimeUtil.getMimeTypes(url);
			assertTrue(mimeTypes.contains("text/plain"));

		}catch(Exception e) {
			fail("Should never get here");
		}
	}
	*/


	class XMLHandler implements TextMimeHandler {

		public boolean handle(TextMimeType mimeType, String content) {
			if(content.startsWith("<?xml")) {
				mimeType.setMimeType(new MimeType("text/xml"));

				// Now lets find the encoding if possible
				int index = content.indexOf("encoding=\"");
				if(index != -1) {
					int endindex = content.indexOf("\"", index+10);
					mimeType.setEncoding(content.substring(index+10, endindex));
					// return true; we don't want to say we have handled this so other handlers can better determine the actual type of XML
				}
			}
			return false;
		}
	}

	class SVGHandler implements TextMimeHandler {
		public boolean handle(TextMimeType mimeType, String content) {
			if(mimeType.equals(new MimeType("text/xml"))) {
				if(StringUtil.contains(content, "<svg  ")) {
					mimeType.setMimeType(new MimeType("image/svg+xml"));
					return true;
				}
			}
			return false;
		}
	}

	class NeverFireHandler implements TextMimeHandler {
		public boolean handle(TextMimeType mimeType, String content) {
			if("svg+xml".equals(mimeType.getSubType())) {
				mimeType.setMediaType("very-funny");
			}
			return false;
		}
	}

	public void testUnicodeAndWestern() {
		String[] encodings = {"UTF-8", "ISO-8859-1", "ISO-8859-15", "ASCII"};

		Collection c_encodings = new ArrayList();
		c_encodings.addAll(Arrays.asList(encodings));
		EncodingGuesser.setSupportedEncodings(c_encodings);
		TextMimeDetector.setPreferredEncodings(encodings);

		assertEquals(MimeUtil.getMimeTypes(new File("src/test/resources/textfiles/western")), "text/plain;charset=ISO-8859-1");
		assertEquals(MimeUtil.getMimeTypes(new File("src/test/resources/textfiles/unicode")), "text/plain;charset=UTF-8");
	}

}