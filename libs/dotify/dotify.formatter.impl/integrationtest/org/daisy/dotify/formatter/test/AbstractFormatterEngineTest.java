package org.daisy.dotify.formatter.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamSource;

import org.daisy.braille.utils.pef.PEFFileCompare;
import org.daisy.braille.utils.pef.PEFFileCompareException;
import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineMaker;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMaker;

abstract class AbstractFormatterEngineTest {
	void testPEF(String input, String expected, boolean keep) throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF(input, expected, keep?File.createTempFile("TestResult", ".tmp"):null);
	}
	
	void testPEF(String input, String expected, File res) throws LayoutEngineException, IOException, PagedMediaWriterConfigurationException {
		testPEF(FormatterEngineMaker.newInstance().newFormatterEngine("sv-SE",
				BrailleTranslatorFactory.MODE_UNCONTRACTED, 
				PagedMediaWriterFactoryMaker.newInstance().newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE)), input, expected, res);
	}

	void testPEF(FormatterEngine engine, String input, String expected, File res) throws LayoutEngineException, IOException {
		boolean keep = res!=null;
		if (!keep) {
			res = File.createTempFile("TestResult", ".tmp");
			res.deleteOnExit();
		}

		engine.convert(this.getClass().getResourceAsStream(input), new FileOutputStream(res));

		try {
			PEFFileCompare cmp = new PEFFileCompare();
			cmp.compare(new StreamSource(this.getClass().getResourceAsStream(expected)), new StreamSource(new FileInputStream(res)));
			assertEquals("Binary compare is equal", -1, cmp.getPos());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (PEFFileCompareException e) {
			e.printStackTrace();
			fail();
		} finally {
			if (!keep && !res.delete()) {
				System.err.println("Delete failed.");
			}
			if (res.isFile()) {
				System.out.println(res.getAbsolutePath());
			}
		}
	}
}
