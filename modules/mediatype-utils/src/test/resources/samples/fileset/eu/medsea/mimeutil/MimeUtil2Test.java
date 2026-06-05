package eu.medsea.mimeutil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeUtil2;

import junit.framework.TestCase;

public class MimeUtil2Test extends TestCase {

	private static Collection UNKNOWN_MIME_TYPE_COLLECTION = new MimeTypeHashSet();

	private static MimeType UNKNOWN_MIME_TYPE = new MimeType("application/octet-stream");

	MimeUtil2 mimeUtil = new MimeUtil2();

	static {
		UNKNOWN_MIME_TYPE_COLLECTION.add(UNKNOWN_MIME_TYPE);
	}

	public void setUp() {
		mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
	}

	public void tearDown() {
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
	}


	/*
	 * Will only work with web access
	 */
	public void testMimeTypesFromWebsite() {
		// Only want to test for the MagicMimeMimeDetector
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");

		try {
			assertTrue(mimeUtil.getMimeTypes(new URL("http://www.gutenberg.org/feeds/catalog.rdf.zip")).contains("application/zip"));
		}catch(Exception e) {
			fail("Should not get here. Possibly no internet connection. " + e.getLocalizedMessage());
		}
	}
	
	public void testGetKnownMimeTypes() {
		assertTrue(MimeUtil2.getKnownMimeTypes().contains("application/octet-stream"));
	}

	public void testMimeTypesEquals() {
		MimeType mt1 = new MimeType("application/xml");
		MimeType mt2 = new MimeType("application/xml");
		MimeType mt3 = new MimeType("text/xml");
		MimeType mt4 = new MimeType("text/plain");

		assertTrue(mt1.equals(mt1));
		assertTrue(mt1.equals(mt2));

		assertTrue(mt1.equals("application/xml"));
		assertTrue(mt2.equals("application/xml"));

		assertFalse(mt3.equals(mt4));
		assertFalse(mt3.equals("text/plain"));
	}

	public void testStreamAndFileGetMimeType() {
		String fileName = "src/test/resources/e.xml";

		try {
			assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/test.bin")).equals(mimeUtil.getMimeTypes(new BufferedInputStream(new FileInputStream("src/test/resources/test.bin")))));
			assertFalse(mimeUtil.getMimeTypes(new File(fileName)).equals(mimeUtil.getMimeTypes(new BufferedInputStream(new FileInputStream(fileName)))));
		}catch(Exception e) {
			fail("Should not get here");
		}
	}

	public void testGetBestMatch() {
		// These tests show how the wild card affects the choice. The first one shows that any application type is preferred
		// and the second shows that any text type is preferred
		assertEquals(MimeUtil2.getPreferedMimeType("application/*;q=0.2,text/xml;q=0.1", "application/xml,text/xml").toString(), "application/xml");
		assertEquals(MimeUtil2.getPreferedMimeType("application/xml;q=0.1,text/*;q=0.2", "application/xml,text/xml").toString(), "text/xml");

		assertTrue(MimeUtil2.getPreferedMimeType("application/*,text/xml;q=0.1", "application/xml,text/xml").equals("text/xml"));
		assertEquals(MimeUtil2.getPreferedMimeType("application/xml;q=0.1,text/*", "application/xml,text/xml").toString(), "application/xml");

		assertEquals(MimeUtil2.getPreferedMimeType("*/*,text/xml;q=0.1", "application/xml,text/xml").toString(), "text/xml");
		assertEquals(MimeUtil2.getPreferedMimeType("application/xml;q=0.1,text/*,*/*", "application/xml,text/xml").toString(), "application/xml");

		// This will return text/html even though the accept string does not contain it
		// because it is the ONLY available option
		assertEquals(MimeUtil2.getPreferedMimeType("application/xml", "text/html").toString(), "text/html");

		// This will return application/xml even though both are acceptable but its the first in the accept list.
		assertEquals(MimeUtil2.getPreferedMimeType("application/xml,text/xml", "text/xml,application/xml").toString(), "application/xml");
		// This will return text/xml as its the first in the accept list
		assertEquals(MimeUtil2.getPreferedMimeType("text/xml,application/xml", "text/xml,application/xml").toString(), "text/xml");
		//
		assertEquals(MimeUtil2.getPreferedMimeType("application/xml;q=0.1,text/xml", "application/xml,text/xml").toString(), "text/xml");

		// The next tests show how the quality factor can affect the result. In these cases one type is preferred over the other
		assertTrue(MimeUtil2.getPreferedMimeType("application/xml,text/xml;q=0.1", "application/xml,text/xml").equals("application/xml"));
		assertEquals(MimeUtil2.getPreferedMimeType("application/xml;q=0.1,text/xml", "application/xml,text/xml").toString(), "text/xml");
	}

	public void testIsMimeTypeKnown() {
		MimeUtil2.addKnownMimeType("application/xml");
		assertTrue(MimeUtil2.isMimeTypeKnown("application/xml"));
		MimeUtil2.addKnownMimeType("text/xml");
		assertTrue(MimeUtil2.isMimeTypeKnown("text/xml"));
		MimeUtil2.addKnownMimeType("text/plain");
		assertTrue(MimeUtil2.isMimeTypeKnown("text/plain"));
		// This will fail as it's unknown
		assertFalse(MimeUtil2.isMimeTypeKnown("xyz/xyz"));
		// Now add it to the known types and try again
		MimeUtil2.addKnownMimeType("xyz/xyz");
		// Now it should be known
		assertTrue(MimeUtil2.isMimeTypeKnown("xyz/xyz"));
	}

	public void testFirstMimeType() {
		assertEquals(MimeUtil2.getFirstMimeType("text/html, application/xml").toString(), "text/html");
		assertEquals(MimeUtil2.getFirstMimeType("text/plain, application/xml").toString(), "text/plain");
	}

	public void testGetMimeTypeAsString() {
		// The default for MimeUtil.getMimeType() is to search by file extension first
		// If the boolean parameter is true it will search by extension first else by sniffing first

		assertTrue(mimeUtil.getMimeTypes("src/test/resources/e.xml").contains("application/xml"));
		assertTrue(mimeUtil.getMimeTypes("a.de").equals(UNKNOWN_MIME_TYPE_COLLECTION));

		// Test for multiple extensions
		assertTrue(mimeUtil.getMimeTypes("e.1.3.jar").contains("application/java-archive"));


		// The following fails to detect using the OpendesktopMimeDetector
		//assertTrue(MimeUtil.getMimeTypes("src/test/resources/d-png.img").contains("image/png"));
	}

	public void testGetMimeTypesAsByteArray() {
		String fileName = "src/test/resources/e-svg.img";

		byte [] data = null;

		try {
			assertTrue(mimeUtil.getMimeTypes(data).equals(UNKNOWN_MIME_TYPE_COLLECTION));
			InputStream in = new FileInputStream(fileName);
			data = new byte [50];
			in.read(data, 0, 50);
			in.close();
			// The amount of data we read is to small to match the image/svg+xml rule
			Collection mimeTypes = mimeUtil.getMimeTypes(data);
			assertFalse(mimeTypes.contains("image/svg+xml"));
			in = new FileInputStream(fileName);
			// This is the minimum amount of data we need to read due to the between rule for the image/svg+xml
			data = new byte [1024];
			in.read(data, 0, 1024);
			in.close();
			assertTrue(mimeUtil.getMimeTypes(data).contains("image/svg+xml"));
		}catch(Exception e) {
			fail("Should not get here");
		}
	}

	public void testGetMimeTypeAsFile() {
		// The default for MimeUtil.getMimeType() is to search by file extension first
		// If the boolean parameter is true it will search by extension first else by sniffing first

		// Find by extension first
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e.xml")).contains("application/xml"));
		assertTrue(mimeUtil.getMimeTypes(new File("a.de")).equals(UNKNOWN_MIME_TYPE_COLLECTION));

		// Test for multiple extensions
		assertTrue(mimeUtil.getMimeTypes(new File("e.1.3.jar")).contains("application/java-archive"));


		// The following test case fails to detect properly with the OpendesktopMimeDetector
		//assertTrue(MimeUtil.getMimeTypes(new File("src/test/resources/d-png.img")).contains("image/png"));
	}

	public void testGetMimeTypesURL() {
		try {
			// Test for directories containing spaces in the name (Only do if running on windows)
			if(System.getProperty("os.name").startsWith("Windows")) {
				assertTrue(mimeUtil.getMimeTypes(new URL("file:///c:/Program Files")).equals(MimeUtil2.DIRECTORY_MIME_TYPE));
			}

			// Test for detection of files in a zip file

			// In the root
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/MimeDetector.class")).contains("application/x-java-class"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/MimeDetector.java")).contains("text/x-java"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/a.html")).contains("text/html"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/c-gif.img")).contains("image/gif"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/e.svg")).contains("image/svg+xml"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/f.tar.gz")).contains("application/x-compressed-tar"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/e[xml]")).contains("application/xml"));

			// In sub-directories
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/resources/eu/medsea/mimeutil/magic.mime")).contains("www/mime"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/resources/eu/medsea/mimeutil/mime-types.properties")).contains("text/plain"));

			// This one will log an exception due to no entry defined
			assertFalse(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/")).contains("application/xml"));
		}catch(Exception e) {
			fail("Should not get here " + e.getLocalizedMessage());
		}
	}


	public void testMimeQuality() {
		assertEquals(MimeUtil2.getMimeQuality("*/*"), 0.01, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("*/*;q=0.4"), 0.4, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("text/*"), 0.02, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("text/* ; q=0.2"), 0.2, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("application/*"), 0.02, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("text/html"), 1.0, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("application/abc"), 1.0, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("application/abc ;q=0.9"), 0.9, 0.0);
		assertEquals(MimeUtil2.getMimeQuality("application/abc;a=ignore-a;q=0.7"), 0.7, 0.0);
		// Quality can't be greater than 1.0
		assertEquals(MimeUtil2.getMimeQuality("application/abc;q=10"), 1.0, 0.0);
		try {
			assertEquals(MimeUtil2.getMimeQuality("application/abc;q=a"), 0.0, 0.0);
			fail("Should not have reached here");
		}catch(MimeException expected) {}
		try {
			assertEquals(MimeUtil2.getMimeQuality("application/abc;q=hello"), 0.0, 0.0);
			fail("Should not have reached here");
		}catch(MimeException expected) {}
	}

	public void testMajorCoponent() {
		assertEquals(MimeUtil2.getMediaType("image/png;q=0.5"), "image");
		assertEquals(MimeUtil2.getMediaType("text/xml"), "text");
		assertEquals(MimeUtil2.getMediaType("application/xml;level=1"), "application");
		assertEquals(MimeUtil2.getMediaType("chemical/x-pdb"), "chemical");
		assertEquals(MimeUtil2.getMediaType("vnd.ms-cab-compressed"), "vnd.ms-cab-compressed");
	}

	public void testMinorCoponent() {
		assertEquals(MimeUtil2.getSubType("image/png"), "png");
		assertEquals(MimeUtil2.getSubType("text/xml"), "xml");
		assertEquals(MimeUtil2.getSubType("application/xml"), "xml");
		assertEquals(MimeUtil2.getSubType("chemical/x-pdb"), "x-pdb");
		assertEquals(MimeUtil2.getSubType("vnd.ms-cab-compressed"), "*");
	}
}
