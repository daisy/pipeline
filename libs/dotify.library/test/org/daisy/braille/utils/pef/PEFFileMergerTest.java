package org.daisy.braille.utils.pef;

import org.daisy.braille.utils.pef.PEFFileMerger.SortType;
import org.daisy.dotify.common.io.FileIO;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class PEFFileMergerTest {

    @Test
    public void testMerger() throws IOException, TransformerException {
        File f = File.createTempFile("MergerTest", "");
        assertTrue("Verify that test is correctly set up", f.delete());
        File dir = new File(f.getParentFile(), f.getName());
        assertTrue("Verify that test is correctly set up", dir.mkdir());

        //copy resources to folder
        File f1 = new File(dir, "volume-1");
        File f2 = new File(dir, "volume-2");
        File f3 = new File(dir, "volume-3");
        File output = File.createTempFile("MergerTest", ".tmp");
        try {
            FileIO.copy(
                this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestInput-1.pef"),
                new FileOutputStream(f1)
            );
            FileIO.copy(
                this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestInput-2.pef"),
                new FileOutputStream(f2)
            );
            FileIO.copy(
                this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestInput-3.pef"),
                new FileOutputStream(f3)
            );

            PEFFileMerger merger = new PEFFileMerger(t -> true);
            merger.merge(dir, new FileOutputStream(output), "Merged file", SortType.STANDARD);
            XMLFileCompare fc = new XMLFileCompare(TransformerFactory.newInstance());
            try (InputStream resultStream = new FileInputStream(output)) {
                assertTrue("Assert that the contents of the file is as expected.", fc.compareXML(
                        this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestExpected.pef"),
                        resultStream)
                );
            }
        } finally {
            if (!f1.delete()) {
                f1.deleteOnExit();
            }
            if (!f2.delete()) {
                f2.deleteOnExit();
            }
            if (!f3.delete()) {
                f3.deleteOnExit();
            }
            if (!dir.delete()) {
                dir.deleteOnExit();
            }
            if (!output.delete()) {
                output.deleteOnExit();
            }
        }

    }

}
