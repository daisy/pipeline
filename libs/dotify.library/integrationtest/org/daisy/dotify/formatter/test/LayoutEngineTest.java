package org.daisy.dotify.formatter.test;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineMaker;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMaker;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class LayoutEngineTest extends AbstractFormatterEngineTest {
    private static final Logger logger = Logger.getLogger(LayoutEngineTest.class.getCanonicalName());

    @Test
    public void testLayoutEngine() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {

        PagedMediaWriterFactory f = PagedMediaWriterFactoryMaker.newInstance().getFactory(MediaTypes.TEXT_MEDIA_TYPE);
        FormatterEngine engine = FormatterEngineMaker.newInstance().newFormatterEngine("en",
                TranslatorType.BYPASS.toString(),
                f.newPagedMediaWriter());
        File res = File.createTempFile("TestResult", ".tmp");
        res.deleteOnExit();

        engine.convert(
            this.getClass().getResourceAsStream("resource-files/obfl-input.obfl"), new FileOutputStream(res)
        );

        try {
            int ret = compareText(
                this.getClass().getResourceAsStream("resource-files/obfl-expected.txt"), new FileInputStream(res)
            );
            assertEquals("Binary compare is equal", -1, ret);
        } catch (IOException e) {
            logger.throwing(AbstractFormatterEngineTest.class.getName(), "testLayoutEngine", e);
            fail();
        } finally {
            if (!res.delete()) {
                logger.severe("Delete failed.");
            }
        }
    }

    @Test
    public void testLayoutEngingeDLS() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/obfl-input-dls.obfl",
            "resource-files/obfl-dls-expected.pef",
            false
        );
    }

    @Test
    public void testLayoutEngingeBorder() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/obfl-input-border.obfl",
            "resource-files/obfl-border-expected.pef",
            false
        );
    }

    @Test
    public void testLayoutEngingeToc() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/obfl-input-toc.obfl",
            "resource-files/obfl-toc-expected.pef",
            false
        );
    }

    @Test
    public void testLayoutEngingeContentItems() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            FormatterEngineMaker.newInstance().getFactory().newFormatterEngine(
                new FormatterConfiguration.Builder("sv-SE", TranslatorType.UNCONTRACTED.toString())
                    .allowsEndingPageOnHyphen(true)
                    .build(),
                PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE)
            ),
            "resource-files/obfl-input-content-items.obfl",
            "resource-files/obfl-content-items-expected.pef",
            null
        );
    }

    @Test
    public void testLayoutEngingeContentItemsFallback() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            FormatterEngineMaker.newInstance().getFactory().newFormatterEngine(
                new FormatterConfiguration.Builder("sv-SE", TranslatorType.UNCONTRACTED.toString())
                    .allowsEndingPageOnHyphen(true)
                    .build(),
                PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE)
            ),
            "resource-files/obfl-input-content-items-fallback.obfl",
            "resource-files/obfl-content-items-fallback-expected.pef",
            null
        );
    }

    @Test
    public void testLayoutEngingeContentItemsFallback2() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            FormatterEngineMaker.newInstance().getFactory().newFormatterEngine(
                new FormatterConfiguration.Builder("sv-SE", TranslatorType.UNCONTRACTED.toString())
                    .allowsEndingPageOnHyphen(true)
                    .build(),
                PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE)
            ),
            "resource-files/obfl-input-content-items-fallback2.obfl",
            "resource-files/obfl-content-items-fallback2-expected.pef",
            null
        );
    }

    @Test
    public void testLayoutEngingeContentItemsFallback3() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            FormatterEngineMaker.newInstance().getFactory().newFormatterEngine(
                new FormatterConfiguration.Builder("sv-SE", TranslatorType.UNCONTRACTED.toString())
                    .allowsEndingPageOnHyphen(true)
                    .build(),
                PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE)
            ),
            "resource-files/obfl-input-content-items-fallback3.obfl",
            "resource-files/obfl-content-items-fallback3-expected.pef",
            null
        );
    }

    @Test (expected = RuntimeException.class)
    public void testLayoutEngineContentItemsNoFallback() throws
            LayoutEngineException,
            IOException,
            PagedMediaWriterConfigurationException {
        testPEF(
            "resource-files/obfl-input-content-items-no-fallback.obfl",
            null,
            false
        );
    }

    public int compareText(InputStream f1, InputStream f2) throws IOException {
        BufferedReader br1 = new BufferedReader(new InputStreamReader(f1));
        BufferedReader br2 = new BufferedReader(new InputStreamReader(f2));

        try {
            int linePos = 1;
            String line;
            while ((line = br1.readLine()) != null) {
                if (!line.equals(br2.readLine())) {
                    return linePos;
                }
                linePos++;
            }
            return -1;
        } finally {
            br1.close();
            br2.close();
        }
    }
}
