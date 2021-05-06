package org.daisy.dotify.formatter.test;

import org.daisy.braille.utils.pef.PEFFileCompare;
import org.daisy.braille.utils.pef.PEFFileCompareException;
import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineMaker;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * TODO: Write java doc.
 */
abstract class AbstractFormatterEngineTest {
    private static final Logger logger = Logger.getLogger(AbstractFormatterEngineTest.class.getCanonicalName());

    void testPEF(
        String input,
        String expected,
        boolean keep
    ) throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
        testPEF(input, expected, keep ? File.createTempFile("TestResult", ".tmp") : null);
    }

    void testPEF(
        String input,
        String expected,
        File res
    ) throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
        testPEF(
                FormatterEngineMaker.newInstance().newFormatterEngine(
                    "sv-SE",
                    TranslatorType.UNCONTRACTED.toString(),
                    PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE)
                ),
                input,
                expected,
                res
        );
    }

    void testPEF(
        FormatterEngine engine,
        String input,
        String expected,
        File res
    ) throws LayoutEngineException, IOException {
        boolean keep = res != null;
        if (!keep) {
            res = File.createTempFile("TestResult", ".tmp");
            res.deleteOnExit();
        }

        engine.convert(this.getClass().getResourceAsStream(input), new FileOutputStream(res));

        try {
            PEFFileCompare cmp = new PEFFileCompare();
            cmp.compare(
                new StreamSource(this.getClass().getResourceAsStream(expected)),
                new StreamSource(new FileInputStream(res))
            );
            assertEquals("Binary compare is equal", -1, cmp.getPos());
        } catch (IOException e) {
            logger.throwing(AbstractFormatterEngineTest.class.getName(), "testPEF", e);
            fail();
        } catch (PEFFileCompareException e) {
            logger.throwing(AbstractFormatterEngineTest.class.getName(), "testPEF", e);
            fail();
        } finally {
            if (!keep && !res.delete()) {
                logger.severe("Delete failed.");
            }
            if (res.isFile()) {
                logger.info(res.getAbsolutePath());
            }
        }
    }
}
