package org.daisy.dotify.impl.input.epub;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OPFReader extends AbstractContainerReader {

	public OPF parse(File root, String pathToOpf) throws EPUB3ReaderException {
		File opf = new File(root, pathToOpf);
		if (!opf.isFile()) {
			throw new IllegalArgumentException("Not a file");
		}

		Document d = readFromStreamAsXML(opf);
		Map<String, String> manifest = new HashMap<String, String>();
		{
			NodeList nl = d.getElementsByTagName("item");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				manifest.put(n.getAttributes().getNamedItem("id").getNodeValue(), n.getAttributes().getNamedItem("href").getNodeValue());
			}
		}
		List<String> spine = new ArrayList<String>();
		// spine contains itemrefs
		{
			NodeList nl = d.getElementsByTagName("itemref");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				spine.add(n.getAttributes().getNamedItem("idref").getNodeValue());
			}
		}
		return new OPF(pathToOpf, spine, manifest);
	}

}
