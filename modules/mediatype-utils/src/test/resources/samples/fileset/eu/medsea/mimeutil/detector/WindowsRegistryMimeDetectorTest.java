package eu.medsea.mimeutil.detector;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import eu.medsea.mimeutil.MimeUtil2;

import eu.medsea.util.EncodingGuesser;

import junit.framework.TestCase;

public class WindowsRegistryMimeDetectorTest extends TestCase {

	MimeUtil2 mimeUtil = new MimeUtil2();

	public void setUp() {
		mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
	}

	public void tearDown() {
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
	}


	public void testGetDescription() {
		MimeDetector mimeDetector = mimeUtil.getMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
		assertEquals(mimeDetector.getDescription(), "Get the MIME types of file extensions from the Windows Registry. Will be inafective on non-Windows machines.");
	}

	public void testGetMimeTypesFileName() {

		assertEquals("text/h323", mimeUtil.getMimeTypes(new File("abc.323")).toString());

		// Extension xxx does not exist in the Windows Registry so it should be an UNKNOWN_MIME_TYPE
		assertEquals(MimeUtil2.UNKNOWN_MIME_TYPE, mimeUtil.getMimeTypes(new File("abc.xxx")).toString());

	}

	public void testGetMimeTypesFile() {

		assertTrue(mimeUtil.getMimeTypes(new File("abc.323")).contains("text/h323"));
		assertTrue(mimeUtil.getMimeTypes(new File("abc.xxx")).contains(MimeUtil2.UNKNOWN_MIME_TYPE));

	}

	public void testGetMimeTypesURL() {
		try {
			assertTrue(mimeUtil.getMimeTypes(new URL("file:src/test/resources/a.html")).contains("text/html"));
		}catch(Exception e) {
			fail("Should not get here " + e.getLocalizedMessage());
		}
	}

}
