package eu.medsea.mimeutil.detector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import eu.medsea.mimeutil.MimeUtil2;
import junit.framework.TestCase;

public class MagicMimeMimeDetectorTest extends TestCase {

	private MimeUtil2 mimeUtil;

	protected void setUp() throws Exception {
		super.setUp();
		mimeUtil = new MimeUtil2();
		mimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		mimeUtil = null;
	}

	public void testGetMimeTypesFile() {
		assertEquals(mimeUtil.getMimeTypes(new File("src/test/resources/afpfile.afp")), "application/vnd.ibm.modcap");
	}

	public void testGetMimeTypesURL() {
		try {
			assertEquals(mimeUtil.getMimeTypes(new URL("file:src/test/resources/afpfile.afp")), "application/vnd.ibm.modcap");
		}catch(MalformedURLException e) {
			fail("Should not get here.");
		}
	}

	public void testGetMimeTypesInputStream() {
		try {
			assertEquals(mimeUtil.getMimeTypes(new BufferedInputStream(new FileInputStream("src/test/resources/afpfile.afp"))), "application/vnd.ibm.modcap");
		}catch(FileNotFoundException e) {
			fail("Should not get here.");
		}
	}

	public void testGetMimeTypesByteArray() {
		try {
			InputStream is = new FileInputStream("src/test/resources/afpfile.afp");
			byte [] data = new byte [10];
			is.read(data);
			assertEquals(mimeUtil.getMimeTypes(data), "application/vnd.ibm.modcap");
		}catch(Exception e) {
			fail("Should not get here.");
		}
	}
}
