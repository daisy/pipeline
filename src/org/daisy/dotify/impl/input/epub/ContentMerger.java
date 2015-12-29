package org.daisy.dotify.impl.input.epub;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.common.io.FileIO;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;

public class ContentMerger {
	private final static String CONTENT_NAME = "package.opf.html";
	private final ContainerReader container;
	private final Logger logger;

	public ContentMerger(ContainerReader container) throws EPUB3ReaderException {
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.container = container;		
	}

	public ContentMerger(File epub) throws EPUB3ReaderException {
		this(new ContainerReader(epub));
	}
	
	public static void copyMerged(File epub, File output) throws EPUB3ReaderException {
		new ContentMerger(epub).copyMerged(output);
	}

	public void copyMerged(File output) throws EPUB3ReaderException {
		output.mkdirs();

		List<String> opfPaths = container.getOPFPaths();

		logger.info("Copying metadata...");
		FileIO.copyRecursive(new File(container.getFolder(), "META-INF"), new File(output, "META-INF"));

		logger.fine("Found " + opfPaths.size() + " opf-file" + (opfPaths.size() == 1 ? "." : "s."));
		for (String path : opfPaths) {
			merge(container.readOPF(path), output);
		}
	}

	private void merge(OPF opf, File output) throws EPUB3ReaderException {
		File opfFile = new File(container.getFolder(), opf.getPath());
		File baseFolder = opfFile.getParentFile();
		File resultFile = new File(output, opf.getPath());
		resultFile.getParentFile().mkdirs();

		File contentFile = new File(resultFile.getParentFile(), CONTENT_NAME);
		Map<String, String> manifest = new HashMap<String, String>(opf.getManifest());
		logger.info("Merging spine to " + resultFile);

		//remove spine items
		for (String idref : opf.getSpine()) {
			manifest.remove(idref);
		}

		resultFile.getParentFile().mkdirs();
		makeMergedSpineOPF(CONTENT_NAME, opfFile, resultFile);
		makeSingleContentDocument(opfFile, contentFile);
		copyResourcesInManifest(manifest, baseFolder, resultFile.getParentFile());
	}

	/**
	 * Creates a new opf with the spine items replaced with a single item, the supplied name.
	 * @param contentName the name of the new spine item
	 * @param sourceOpf the source opf
	 * @param targetOpf the target opf
	 * @throws EPUB3ReaderException
	 */
	private void makeMergedSpineOPF(String contentName, File sourceOpf, File targetOpf) throws EPUB3ReaderException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("content", contentName);
		try {
			XMLTools.transform(sourceOpf, targetOpf, this.getClass().getResource("resource-files/opf-merge-spine.xslt"), params);
		} catch (XMLToolsException e) {
			throw new EPUB3ReaderException(e);
		}
	}
	
	public void makeSingleContentDocument(String opfPath, File resultFile) throws EPUB3ReaderException {
		makeSingleContentDocument(new File(container.getFolder(), opfPath), resultFile);
	}
	
	/**
	 * Makes a single content document based on the original spine items.
	 * @param opfFile the original opf
	 * @param resultFile the resulting document
	 * @throws EPUB3ReaderException
	 */
	public void makeSingleContentDocument(File opfFile, File resultFile) throws EPUB3ReaderException {
		if (!opfFile.exists()) {
			throw new EPUB3ReaderException("File not found: " + opfFile);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			XMLTools.transform(opfFile, resultFile, this.getClass().getResource("resource-files/opf-merge-content-docs.xslt"), params);
		} catch (XMLToolsException e) {
			throw new EPUB3ReaderException(e);
		}
	}

	/**
	 * Copies the resources in the manifest from the source to the target
	 * @param manifest the manifest
	 * @param sourceFolder the source folder
	 * @param targetFolder the target folder
	 */
	private void copyResourcesInManifest(Map<String, String> manifest, File sourceFolder, File targetFolder) {
		logger.info("Copying resources...");
		File source;
		File target;
		String path;
		for (String id : manifest.keySet()) {
			try {
				path = manifest.get(id);
				source = new File(sourceFolder, path).getCanonicalFile();
				if (source.exists()) {
					target = new File(targetFolder, path).getCanonicalFile();
					logger.fine("Copying " + source + " --> " + target);
					target.getParentFile().mkdirs();
					FileIO.copyFile(source, target);
				} else {
					logger.info("Referenced file cannot be found: " + path);
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Failed to resolve path reference: " + id, e);
			}
		}
	}

}
