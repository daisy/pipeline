package org.daisy.dotify.impl.input.xml;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.daisy.braille.pef.XMLFileCompare;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.impl.input.XsltTask;
import org.daisy.dotify.tasks.runner.TaskRunner;

public class TestHelper {

	public static void toObfl(String srcPath, File res, String lang, Map<String, Object> parameters) throws IOException, TaskSystemException, URISyntaxException {
		TaskGroup tg = new XMLInputManagerFactory().newTaskGroup(new TaskGroupSpecification("xml", "obfl", lang));
		TaskRunner tr = new TaskRunner.Builder().build();
		File src = new File(TestHelper.class.getResource(srcPath).toURI());
		tr.runTasks(src, res, tg.compile(parameters));
	}

	public static void runXSLT(String input, String xslt, String expected, Map<String, Object> options, boolean keep) throws IOException, InternalTaskException, URISyntaxException{
		File actual = File.createTempFile(TestHelper.class.getName(), ".tmp");
		try {
			XsltTask task = new XsltTask("XSLT Test", TestHelper.class.getResource(xslt), options);
			File src = new File(TestHelper.class.getResource(input).toURI());
			task.execute(src, actual);
			XMLFileCompare c = new XMLFileCompare(TransformerFactory.newInstance());
			try {
				assertTrue(c.compareXML(new FileInputStream(actual), TestHelper.class.getResourceAsStream(expected)));
			} catch (TransformerException e) {
				e.printStackTrace();
				fail();
			}
		} finally {
			if (!keep) {
				if (!actual.delete()) {
					actual.deleteOnExit();
				}
			} else {
				System.out.println("Result file is: " + actual);
			}
		}
	}

}
