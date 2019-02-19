package org.daisy.pipeline.client.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.utils.XML;
import org.junit.Test;
import org.w3c.dom.Document;

public class ProgressTest {
	
	private File resources = new File("src/test/resources/");
	private String loadResource(String href) {
		File scriptXmlFile = new File(resources, href);
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(scriptXmlFile.getPath()));
			return new String(encoded, Charset.defaultCharset());
		} catch (IOException e) {
			assertTrue("Failed to read "+scriptXmlFile.getPath(), false);
			return null;
		}
	}
	private Document loadResourceXml(String href) {
		String xmlString = loadResource(href);
		return XML.getXml(xmlString);
	}
	
	@Test
	public void testBasic() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job6.xml"));
			assertNotNull(job);
			
			assertEquals(28, job.getMessages().size());
			assertEquals("ZedAI file name: \n\n", job.getMessages().get(0).text);
			assertEquals("MODS file name: \n\n", job.getMessages().get(1).text);
			assertEquals("CSS file name: \n\n", job.getMessages().get(2).text);
			assertEquals("Renaming certain elements to span with @role", job.getMessages().get(3).text);
			assertEquals("Renaming code and kbd elements to reflect block or phrase variants.", job.getMessages().get(4).text);
			assertEquals("Renaming annotation elements to identify block or phrase variants.", job.getMessages().get(5).text);
			assertEquals("Convert br to lines", job.getMessages().get(6).text);
			assertEquals("Grouping contents of a definition list into items", job.getMessages().get(7).text);
			assertEquals("Move out inlined image groups", job.getMessages().get(8).text);
			assertEquals("Move out inlined lists", job.getMessages().get(9).text);
			assertEquals("Move out inlined definition lists", job.getMessages().get(10).text);
			assertEquals("Move out inlined prodnotes", job.getMessages().get(11).text);
			assertEquals("Move out inlined divs", job.getMessages().get(12).text);
			assertEquals("Move out inlined poems", job.getMessages().get(13).text);
			assertEquals("Move out inlined linegroups", job.getMessages().get(14).text);
			assertEquals("Move out inlined tables", job.getMessages().get(15).text);
			assertEquals("Move out inlined sidebars", job.getMessages().get(16).text);
			assertEquals("Move out inlined notes", job.getMessages().get(17).text);
			assertEquals("Move out inlined epigraphs", job.getMessages().get(18).text);
			assertEquals("Move out inlined annotation blocks", job.getMessages().get(19).text);
			assertEquals("Move out inlined code blocks", job.getMessages().get(20).text);
			assertEquals("Normalize mixed block/inline content", job.getMessages().get(21).text);
			assertEquals("Normalize mixed section/block content", job.getMessages().get(22).text);
			assertEquals("Normalize mixed section and block content model for level1", job.getMessages().get(23).text);
			assertEquals("Normalize mixed section and block content model for level1", job.getMessages().get(24).text);
			assertEquals("Translate to ZedAI", job.getMessages().get(25).text);
			assertEquals("Generating CSS", job.getMessages().get(26).text);
			assertEquals("baseURI (Please see detailed log for more info.)", job.getMessages().get(27).text);

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testMessagesJoin() {
		try {
			Job job = new Job(XML.getXml(
					"<job xmlns=\"http://www.daisy.org/ns/pipeline/data\">"
					+"<messages progress=\"0.0\">"
					+"<message content=\"ZedAI file name: &#10;&#10;\" level=\"INFO\" sequence=\"70\" timeStamp=\"1550487959130\"/>"
					+"<message content=\"MODS file name: &#10;&#10;\" level=\"INFO\" sequence=\"79\" timeStamp=\"1550487959132\"/>"
					+"<message content=\"CSS file name: &#10;&#10;\" level=\"INFO\" sequence=\"88\" timeStamp=\"1550487959133\"/>"
					+"</messages>"
					+"</job>"
			));
			
			Job jobUpdate = new Job(XML.getXml(
					"<job xmlns=\"http://www.daisy.org/ns/pipeline/data\">"
					+"<messages msgSeq=\"88\" progress=\"1.0\">"
					+"<message content=\"CSS file name: &#10;&#10;\" level=\"INFO\" sequence=\"88\" timeStamp=\"1550487959647\"/>"
					+"<message content=\"Renaming certain elements to span with @role\" level=\"INFO\" sequence=\"253\" timeStamp=\"1550487959650\"/>"
					+"<message content=\"Renaming code and kbd elements to reflect block or phrase variants.\" level=\"INFO\" sequence=\"255\" timeStamp=\"1550487959653\"/>"
					+"<message content=\"Renaming annotation elements to identify block or phrase variants.\" level=\"INFO\" sequence=\"257\" timeStamp=\"1550487959656\"/>"
					+"<message content=\"Convert br to lines\" level=\"INFO\" sequence=\"259\" timeStamp=\"1550487959661\"/>"
					+"<message content=\"Grouping contents of a definition list into items\" level=\"INFO\" sequence=\"261\" timeStamp=\"1550487959664\"/>"
					+"<message content=\"Move out inlined image groups\" level=\"INFO\" sequence=\"263\" timeStamp=\"1550487959667\"/>"
					+"<message content=\"Move out inlined lists\" level=\"INFO\" sequence=\"265\" timeStamp=\"1550487959670\"/>"
					+"<message content=\"Move out inlined definition lists\" level=\"INFO\" sequence=\"267\" timeStamp=\"1550487959674\"/>"
					+"<message content=\"Move out inlined prodnotes\" level=\"INFO\" sequence=\"269\" timeStamp=\"1550487959677\"/>"
					+"<message content=\"Move out inlined divs\" level=\"INFO\" sequence=\"271\" timeStamp=\"1550487959680\"/>"
					+"<message content=\"Move out inlined poems\" level=\"INFO\" sequence=\"273\" timeStamp=\"1550487959683\"/>"
					+"<message content=\"Move out inlined linegroups\" level=\"INFO\" sequence=\"275\" timeStamp=\"1550487959686\"/>"
					+"<message content=\"Move out inlined tables\" level=\"INFO\" sequence=\"277\" timeStamp=\"1550487959689\"/>"
					+"<message content=\"Move out inlined sidebars\" level=\"INFO\" sequence=\"279\" timeStamp=\"1550487959692\"/>"
					+"<message content=\"Move out inlined notes\" level=\"INFO\" sequence=\"281\" timeStamp=\"1550487959696\"/>"
					+"<message content=\"Move out inlined epigraphs\" level=\"INFO\" sequence=\"283\" timeStamp=\"1550487959699\"/>"
					+"<message content=\"Move out inlined annotation blocks\" level=\"INFO\" sequence=\"285\" timeStamp=\"1550487959702\"/>"
					+"<message content=\"Move out inlined code blocks\" level=\"INFO\" sequence=\"287\" timeStamp=\"1550487959705\"/>"
					+"<message content=\"Normalize mixed block/inline content\" level=\"INFO\" sequence=\"289\" timeStamp=\"1550487959708\"/>"
					+"<message content=\"Normalize mixed section/block content\" level=\"INFO\" sequence=\"291\" timeStamp=\"1550487959711\"/>"
					+"<message content=\"Normalize mixed section and block content model for level1\" level=\"INFO\" sequence=\"292\" timeStamp=\"1550487959714\"/>"
					+"<message content=\"Normalize mixed section and block content model for level1\" level=\"INFO\" sequence=\"293\" timeStamp=\"1550487959720\"/>"
					+"<message content=\"Translate to ZedAI\" level=\"INFO\" sequence=\"295\" timeStamp=\"1550487959723\"/>"
					+"<message content=\"Generating CSS\" level=\"INFO\" sequence=\"313\" timeStamp=\"1550487959727\"/>"
					+"<message content=\"baseURI (Please see detailed log for more info.)\" level=\"ERROR\" portion=\"1.0\" progress=\"1.0\" sequence=\"314\" timeStamp=\"1550487959730\"/>"
					+"</messages>"
					+"</job>"
			));
			
			assertEquals(3, job.getMessages().size());
			assertEquals(25, jobUpdate.getMessages().size()); // first message with sequence = msgSeq is not included in list
			
			job.joinMessages(jobUpdate);
			
			assertEquals(28, job.getMessages().size());

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	// Note that the progress is not monotonically increasing, but this is not a bug. It is the
	// progress information in this specific job XML that is unreliable.
	@Test
	public void testProgressEstimate() throws Pipeline2Exception {
		Job job = null;
		long now = new Date().getTime();
		int[] expected = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 8, 8, 8, 14,
		                           17, 19, 20, 21, 22, 22, 23, 23, 24, 24, 24, 26, 28, 28, 29, 28, 29, 28, 29, 28, 29, 28,
		                           29, 28, 29, 35, 36, 37, 38, 39, 40, 41, 41, 42, 43, 44, 44, 45, 45, 46, 46, 47, 47, 48,
		                           48, 49, 49, 50, 50, 50, 51, 51, 51, 52, 52, 52, 53, 53, 53, 53, 54, 54, 54, 54, 55, 55,
		                           55, 55, 55, 56, 56, 36, 36, 37, 37, 38, 38, 39, 39, 39, 40, 36, 37, 39, 40, 41, 42, 42,
		                           43, 43, 44, 45, 45, 46, 46, 46, 47, 47, 48, 48, 48, 49, 49, 50, 50, 50, 51, 51, 51, 52,
		                           52, 53, 53, 53, 54, 54, 54, 55, 55, 55, 55, 90, 90, 91, 91, 100, 100};
		int k = 0;
		for (int i = 1; i <= 78; i++) {
			Job j = new Job(loadResourceXml("responses/jobs/job7/"+i+".xml"));
			j.getMessages(-1, now);
			if (job == null)
				job = j;
			else
				job.joinMessages(j);
			assertEquals(expected[k++], (int)Math.round(job.getProgressEstimate(now)));
			now += 500;
			assertEquals(expected[k++], (int)Math.round(job.getProgressEstimate(now)));
			now += 500;
		}
	}
}
