package eu.medsea.mimeutil.detector;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import eu.medsea.mimeutil.MimeUtil2;

import eu.medsea.util.EncodingGuesser;

import junit.framework.TestCase;

public class OpendesktopMimeDetectorTest extends TestCase {

	MimeUtil2 mimeUtil = new MimeUtil2();

	public void setUp() {
		mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
	}

	public void tearDown() {
		mimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
	}


	public void testGetDescription() {
		MimeDetector mimeDetector = mimeUtil.getMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
		assertEquals(mimeDetector.getDescription(), "Resolve mime types for files and streams using the Opendesktop shared mime.cache file. Version [1.1].");
	}

	public void testGetMimeTypesFileGlob() {

		assertEquals("text/plain", mimeUtil.getMimeTypes(new File("abc.txt")).toString());
		assertEquals("text/x-makefile", mimeUtil.getMimeTypes(new File("makefile")).toString());
		assertEquals("text/x-makefile", mimeUtil.getMimeTypes(new File("Makefile")).toString());
		assertEquals("image/x-win-bitmap", mimeUtil.getMimeTypes(new File("x.cur")).toString());
		assertEquals("application/vnd.ms-tnef", mimeUtil.getMimeTypes(new File("winmail.dat")).toString());
		assertEquals("text/x-troff-mm", mimeUtil.getMimeTypes(new File("abc.mm")).toString());
		assertEquals("text/x-readme", mimeUtil.getMimeTypes(new File("README")).toString());
		assertEquals("video/x-anim", mimeUtil.getMimeTypes(new File("abc.anim5")).toString());
		assertEquals("video/x-anim", mimeUtil.getMimeTypes(new File("abc.animj")).toString());
		assertEquals("text/x-readme", mimeUtil.getMimeTypes(new File("READMEFILE")).toString());
		assertEquals("text/x-readme", mimeUtil.getMimeTypes(new File("READMEanim3")).toString());
		assertEquals("text/x-log", mimeUtil.getMimeTypes(new File("README.log")).toString());
		assertEquals("text/x-readme", mimeUtil.getMimeTypes(new File("README.file")).toString());
		assertEquals("application/x-compress", mimeUtil.getMimeTypes(new File("README.Z")).toString());
		assertEquals(MimeUtil2.UNKNOWN_MIME_TYPE, mimeUtil.getMimeTypes(new File("READanim3")).toString());

		// Try multi extensions
		assertEquals("application/x-java-archive", mimeUtil.getMimeTypes(new File("e.1.3.jar")).toString());
	}

	public void testGetMimeTypesFile() {
		// Globbing won't work so lets try magic sniffing
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/e[xml]")).contains("application/xml"));

		// This is a text file so the text file detector should be used but first verify it's not matched
		assertFalse(mimeUtil.getMimeTypes(new File("src/test/resources/plaintext")).contains("text/plain"));

		// Now set the supported encodings to all encodings supported by the JVM
		EncodingGuesser.setSupportedEncodings(EncodingGuesser.getCanonicalEncodingNamesSupportedByJVM());
		assertTrue(mimeUtil.getMimeTypes(new File("src/test/resources/plaintext")).contains("text/plain"));
		// Clean out the encodings using an empty collection
		EncodingGuesser.setSupportedEncodings(new ArrayList());


	}

	public void testGetMimeTypesURL() {
		try {
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/MimeDetector.class")).contains("application/x-java"));
			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/MimeDetector.java")).contains("text/x-java"));

			assertEquals(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/a.html")).toString(), "text/html");
			assertEquals(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/c-gif.img")).toString(), "image/gif");
			assertEquals(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/e.svg")).toString(), "image/svg+xml");
			assertEquals(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/f.tar.gz")).toString(), "application/x-compressed-tar");

			assertTrue(mimeUtil.getMimeTypes(new URL("jar:file:src/test/resources/a.zip!/e[xml]")).contains("application/xml"));
		}catch(Exception e) {
			fail("Should not get here " + e.getLocalizedMessage());
		}
	}

}
