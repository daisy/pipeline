package org.daisy.pipeline.tts.cereproc.impl;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.tts.Voice;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.util.*;


public class CereProcEngineTest extends AbstractTest {


	private static final Map<String,String> params = new HashMap<>();

	static {
		params.put("org.daisy.pipeline.tts.cereproc.server", System.getProperty("org.daisy.pipeline.tts.cereproc.server"));
		params.put("org.daisy.pipeline.tts.cereproc.port", System.getProperty("org.daisy.pipeline.tts.cereproc.port"));
		params.put("org.daisy.pipeline.tts.cereproc.dnn.port", System.getProperty("org.daisy.pipeline.tts.cereproc.dnn.port"));
	}


	@Test
	public void TestSSMLFormatter() throws Throwable {
		CereProcService service = new CereProcService() {
			@Override
			protected CereProcEngine newEngine(String server, File client, int priority, Map<String, String> params) throws Throwable {
				return null;
			}
		};

		File client = new File(CereProcEngineTest.class.getResource("/ClientMock").toURI());
		CereProcEngine engine = new CereProcEngine(CereProcEngine.Variant.STANDARD,
				service,
				"Server",
				9999,
				client,
				1,
				CereProcEngine.class.getResource("/transform-ssml.xsl")
		);
		Configuration conf = new Configuration();
		List<XdmItem> ssmlProcessed = new ArrayList<>();
		XMLStreamWriter writer = new SaxonOutputValue(
				item -> {
					if (item instanceof XdmNode) {
						ssmlProcessed.add(item);
					} else {
						throw new RuntimeException(); // should not happen
					}
				}, conf).asXMLStreamWriter();

		writer.writeStartDocument("1.0");
		writer.writeStartElement("ssml", "speak", "x");
		writer.writeAttribute("xml:lang", "sv");
		writer.writeCharacters("This is a Γ. test roman letter III, lorem ipsum 27 kap.");
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
		writer.close();

		XdmNode node = (XdmNode) ssmlProcessed.get(0);
		Voice  v = new Voice("cereproc", "Ylva", new Locale("sv"), null, null);
		Assert.assertEquals("<voice name=\"Ylva\">This is a  gamma . test roman letter  tre, lorem ipsum Tjugosjunde kapitlet.</voice><break time=\"250ms\"></break>",
				engine.transformSSML(node, v)
		);
		engine.transformSSML(node, v);
		engine.transformSSML(node, v);
		engine.transformSSML(node, v);
		engine.transformSSML(node, v);
	}
}
