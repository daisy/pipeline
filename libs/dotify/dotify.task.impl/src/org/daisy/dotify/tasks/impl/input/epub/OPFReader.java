package org.daisy.dotify.tasks.impl.input.epub;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides a reader of OPF-files.
 * @author Joel Håkansson
 */
public class OPFReader extends AbstractContainerReader {

	/**
	 * Parses a file and returns an OPF.
	 * @param root the root folder of the epub publication
	 * @param pathToOpf the path to the opf, relative to the root
	 * @return returns a parsed OPF.
	 * @throws EPUB3ReaderException if the opf could not be parsed
	 */
	public OPF parse(File root, String pathToOpf) throws EPUB3ReaderException {
		File opf = new File(root, pathToOpf);
		if (!opf.isFile()) {
			throw new IllegalArgumentException("Not a file");
		}

		Document d = readFromStreamAsXML(opf);
		Map<String, String> manifest = new HashMap<>();
		{
			NodeList nl = d.getElementsByTagName("item");
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				manifest.put(n.getAttributes().getNamedItem("id").getNodeValue(), n.getAttributes().getNamedItem("href").getNodeValue());
			}
		}
		List<String> spine = new ArrayList<>();
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
