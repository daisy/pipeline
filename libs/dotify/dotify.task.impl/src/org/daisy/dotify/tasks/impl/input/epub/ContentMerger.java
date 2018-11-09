package org.daisy.dotify.tasks.impl.input.epub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.common.io.FileIO;
import org.daisy.dotify.common.io.ResourceLocator;
import org.daisy.dotify.common.io.ResourceLocatorException;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;

/**
 * Provides an merger for epub 3 html files.
 * @author Joel Håkansson
 */
public class ContentMerger {
	private static final String CONTENT_NAME = "package.opf.html";
	private final ContainerReader container;
	private final Logger logger;
	private final ResourceLocator locator;

	/**
	 * Creates a new content merger with the specified reader.
	 * @param container the container reader
	 */
	public ContentMerger(ContainerReader container) {
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.container = container;
		this.locator = new EpubResourceLocator("resource-files");
	}

	/**
	 * Creates a new content merger with the specified epub root folder.
	 * @param epub the epub root folder.
	 * @throws EPUB3ReaderException if the container file could not be read 
	 */
	public ContentMerger(File epub) throws EPUB3ReaderException {
		this(new ContainerReader(epub));
	}
	
	/**
	 * Reads an unzipped epub folder and creates a copy of it but with the content files merged.
	 * @param epub the epub root folder
	 * @param output the output folder
	 * @throws EPUB3ReaderException if there is a problem reading the epub
	 */
	public static void copyMerged(File epub, File output) throws EPUB3ReaderException {
		new ContentMerger(epub).copyMerged(output);
	}

	/**
	 * Creates a copy of the epub folder with the content files merged.
	 * @param output the output folder
	 * @throws EPUB3ReaderException if there is a problem reading the epub
	 */
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
		Map<String, String> manifest = new HashMap<>(opf.getManifest());
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
		Map<String, Object> params = new HashMap<>();
		params.put("content", contentName);
		try {
			XMLTools.transform(sourceOpf, targetOpf, locator.getResource("opf-merge-spine.xslt"), params);
		} catch (XMLToolsException | ResourceLocatorException e) {
			throw new EPUB3ReaderException(e);
		}
	}
	
	/**
	 * Makes a single content document based on the original spine items.
	 * @param opfPath the OPF path relative to the epub root
	 * @param resultFile the resulting document
	 * @throws EPUB3ReaderException if the content could not be merged
	 */
	public void makeSingleContentDocument(String opfPath, File resultFile) throws EPUB3ReaderException {
		makeSingleContentDocument(new File(container.getFolder(), opfPath), resultFile);
	}
	
	/**
	 * Makes a single content document based on the original spine items.
	 * @param opfFile the original opf
	 * @param resultFile the resulting document
	 * @throws EPUB3ReaderException if content could not be merged
	 */
	public void makeSingleContentDocument(File opfFile, File resultFile) throws EPUB3ReaderException {
		if (!opfFile.exists()) {
			throw new EPUB3ReaderException("File not found: " + opfFile);
		}
		Map<String, Object> params = new HashMap<>();
		try {
			XMLTools.transform(opfFile, resultFile, locator.getResource("opf-merge-content-docs.xslt"), params,  new net.sf.saxon.TransformerFactoryImpl());
		} catch (XMLToolsException | ResourceLocatorException e) {
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
					Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} else {
					logger.info("Referenced file cannot be found: " + path);
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Failed to resolve path reference: " + id, e);
			}
		}
	}

}
