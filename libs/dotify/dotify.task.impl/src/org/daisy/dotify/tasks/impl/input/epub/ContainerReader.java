package org.daisy.dotify.tasks.impl.input.epub;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides a parser for epub 3 container files (<code>META-INF/container.xml</code>).
 * 
 * @author Joel Håkansson
 */
public class ContainerReader extends AbstractContainerReader {
	private final Logger logger;
	private final File folder;
	private final List<String> paths;

	/**
	 * Creates a new container reader with the specified root.
	 * @param root the epub root folder.
	 * @throws EPUB3ReaderException if the container could not be read
	 * @throws IllegalArgumentException if root is not a directory
	 */
	public ContainerReader(File root) throws EPUB3ReaderException {
		if (!root.isDirectory()) {
			throw new IllegalArgumentException("Not a directory");
		}
		this.folder = root;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		logger.info("Reading container.xml");
		File container = new File(new File(root, "META-INF"), "container.xml");
		Document d = readFromStreamAsXML(container);
		List<String> tmp = new ArrayList<>();
		NodeList nl = d.getElementsByTagName("rootfile");
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			tmp.add(n.getAttributes().getNamedItem("full-path").getNodeValue());
		}
		this.paths = Collections.unmodifiableList(tmp);
	}

	/**
	 * Gets a list of opf paths within the root folder.
	 * @return returns a list of paths
	 */
	public List<String> getOPFPaths() {
		return paths;
	}

	/**
	 * Gets the root folder of the container.
	 * @return returns the root folder
	 */
	public File getFolder() {
		return folder;
	}
	
	/**
	 * Parses an opf file.
	 * @param path the relative path to parse
	 * @return returns the OPF
	 * @throws EPUB3ReaderException if the opf could not be parsed
	 */
	public OPF readOPF(String path) throws EPUB3ReaderException {
		logger.fine("Reading " + path);
		OPFReader opfReader = new OPFReader();
		return opfReader.parse(folder, path);
	}

}
