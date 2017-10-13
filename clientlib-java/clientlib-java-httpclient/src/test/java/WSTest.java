import java.io.File;
import java.util.List;

import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.http.WS;
import org.daisy.pipeline.client.http.WSInterface;
import org.daisy.pipeline.client.models.Alive;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.datatypes.EnumType;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.utils.XML;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WSTest extends PaxExamConfig {

	public WSTest() {
		super(true);
	}
	
	// use this to test the webservice manually in the browser (http://localhost:8181/ws/...)
	//@Test
	public void keepWsAlive() throws InterruptedException {
		Thread.sleep(60000);
	}
	
	@Test
	public void testAlive() {
		WSInterface ws = new WS();
		ws.setEndpoint(getEndpoint());
		Alive alive = ws.alive();
		assertFalse(alive.error);
		assertFalse(alive.authentication);
		assertTrue(alive.localfs);
		assertEquals("1.10", alive.version);
	}
	
	@Test
	public void testScripts() {
		WSInterface ws = new WS();
		ws.setEndpoint(getEndpoint());
		List<Script> scripts = ws.getScripts();
		assertEquals(1, scripts.size());
		assertEquals("foo:script", scripts.get(0).getId());
		Script script = ws.getScript("foo:script");
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		             "<script xmlns=\"http://www.daisy.org/ns/pipeline/data\"\n" +
		             "        href=\"" + getEndpoint() + "/scripts/foo:script\"\n" +
		             "        id=\"foo:script\"\n" +
		             "        input-filesets=\"daisy202 daisy3\"\n" +
		             "        output-filesets=\"epub2 epub3\">\n" +
		             "   <nicename>Example script</nicename>\n" +
		             "   <description>Transforms a Something into a Something.</description>\n" +
		             "   <version>0.0.0-SNAPSHOT</version>\n" +
		             "   <homepage>http://github.com/daisy</homepage>\n" +
		             "   <input desc=\"Input port description.\"\n" +
		             "          mediaType=\"application/x-dtbook+xml\"\n" +
		             "          name=\"source\"\n" +
		             "          nicename=\"Input port\"\n" +
		             "          ordered=\"true\"\n" +
		             "          required=\"true\"\n" +
		             "          sequence=\"false\"\n" +
		             "          type=\"anyFileURI\"/>\n" +
		             "   <option data-type=\"foo:choice\"\n" +
		             "           desc=\"Enum description.\"\n" +
		             "           mediaType=\"\"\n" +
		             "           name=\"option-1\"\n" +
		             "           nicename=\"Enum\"\n" +
		             "           ordered=\"true\"\n" +
		             "           required=\"true\"\n" +
		             "           sequence=\"false\"\n" +
		             "           type=\"string\"/>\n" +
		             "   <option data-type=\"foo:regex\"\n" +
		             "           desc=\"Regex description.\"\n" +
		             "           mediaType=\"\"\n" +
		             "           name=\"option-2\"\n" +
		             "           nicename=\"Regex\"\n" +
		             "           ordered=\"true\"\n" +
		             "           required=\"false\"\n" +
		             "           sequence=\"false\"\n" +
		             "           type=\"string\"/>\n" +
		             "   <option desc=\"Input HTML.\"\n" +
		             "           mediaType=\"application/xhtml+xml text/html\"\n" +
		             "           name=\"href\"\n" +
		             "           nicename=\"HTML\"\n" +
		             "           ordered=\"true\"\n" +
		             "           required=\"true\"\n" +
		             "           sequence=\"false\"\n" +
		             "           type=\"anyFileURI\"/>\n" +
		             "   <option desc=\"Whether or not to include or not include something that you may (or may not) want to include.\"\n" +
		             "           mediaType=\"\"\n" +
		             "           name=\"yes-or-no\"\n" +
		             "           nicename=\"Yes? No?\"\n" +
		             "           ordered=\"true\"\n" +
		             "           required=\"false\"\n" +
		             "           sequence=\"false\"\n" +
		             "           type=\"boolean\"/>\n" +
		             "</script>\n",
		             XML.toString(script.toXml()));
		assertEquals("Example script", script.getNicename());
		assertEquals(5, script.getInputs().size());
		Argument option1 = script.getArgument("option-1");
		assertTrue(option1.getRequired());
		assertEquals("foo:choice", option1.getDataType());
		EnumType choice = (EnumType)ws.getDataType("foo:choice");
		assertEquals(3, choice.values.size());
		assertEquals("one", choice.values.get(0).name);
		assertEquals("two", choice.values.get(1).name);
		assertEquals("three", choice.values.get(2).name);
		Argument option2 = script.getArgument("option-2");
		assertFalse(option2.getRequired());
		assertEquals("one", option2.getDefaultValue());
		assertEquals("foo:regex", option2.getDataType().toString());
	}
	
	@Test
	public void testJobLocal() throws InterruptedException {
		WSInterface ws = new WS();
		ws.setEndpoint(getEndpoint());
		Job job; {
			job = new Job();
			job.setId("1");
			Script script = ws.getScript("foo:script");
			job.setScript(script);
			File jobStorageDir = new File(BASEDIR, "target/tmp/client/jobs");
			JobStorage context = new JobStorage(job, jobStorageDir, null);
			Argument source = script.getArgument("source");
			source.set(new File(BASEDIR, "src/test/resources/input1.xml"), context);
			Argument option1 = script.getArgument("option-1");
			option1.set("three");
			Argument href = script.getArgument("href");
			href.set(new File(BASEDIR, "src/test/resources/input2.html"), context);
		}
		assertNull(job.validate());
		job = ws.postJob(job);
		assertNotNull(job);
		Thread.sleep(2000);
		job = ws.getJob(job.getId(), 0);
		assertEquals("The job is finished", Job.Status.DONE, job.getStatus());
		assertEquals("<result>" +
		             "<source><hello object=\"world\"/></source>" +
		             "<option name=\"href\" value=\""
		                 + new File(BASEDIR, "src/test/resources/input2.html").toURI() + "\"/>" +
		             "</result>\n",
		             job.getResults().get(job.getResult("output-dir")).get(0).asText());
	}
}
