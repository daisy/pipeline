package org.daisy.dotify.impl.input.epub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ContainerReader extends AbstractContainerReader {
	private final Document d;

	public ContainerReader(File root) throws EPUB3ReaderException {
		if (!root.isDirectory()) {
			throw new IllegalArgumentException("Not a directory");
		}

		File container = new File(new File(root, "META-INF"), "container.xml");
		this.d = readFromStreamAsXML(container);
	}

	public List<String> getPaths() throws EPUB3ReaderException {
		List<String> paths = new ArrayList<String>();
		NodeList nl = d.getElementsByTagName("rootfile");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			paths.add(n.getAttributes().getNamedItem("full-path").getNodeValue());
		}
		return paths;
	}

}
