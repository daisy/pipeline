package org.daisy.dotify.impl.input.epub;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ContainerReader extends AbstractContainerReader {
	private final Logger logger;
	private final File folder;
	private final List<String> paths;

	public ContainerReader(File root) throws EPUB3ReaderException {
		if (!root.isDirectory()) {
			throw new IllegalArgumentException("Not a directory");
		}
		this.folder = root;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		logger.info("Reading container.xml");
		File container = new File(new File(root, "META-INF"), "container.xml");
		Document d = readFromStreamAsXML(container);
		List<String> tmp = new ArrayList<String>();
		NodeList nl = d.getElementsByTagName("rootfile");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			tmp.add(n.getAttributes().getNamedItem("full-path").getNodeValue());
		}
		this.paths = Collections.unmodifiableList(tmp);
	}

	public List<String> getOPFPaths() throws EPUB3ReaderException {
		return paths;
	}

	public File getFolder() {
		return folder;
	}
	
	public OPF readOPF(String path) throws EPUB3ReaderException {
		logger.fine("Reading " + path);
		OPFReader opfReader = new OPFReader();
		return opfReader.parse(folder, path);
	}

}
