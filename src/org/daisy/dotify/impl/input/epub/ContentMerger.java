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
	private final File epub;
	private final Logger logger;

	public ContentMerger(File epub) {
		if (!epub.isDirectory()) {
			throw new IllegalArgumentException("Input must be a directory.");
		}
		this.epub = epub;
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
	}
	
	public static void copyMerged(File epub, File output) throws EPUB3ReaderException {
		new ContentMerger(epub).copyMerged(output);
	}

	public void copyMerged(File output) throws EPUB3ReaderException {
		output.mkdirs();

		logger.info("Reading container.xml");

		ContainerReader containerReader = new ContainerReader(epub);

		List<String> opfPaths = containerReader.getPaths();

		logger.info("Copying metadata");
		FileIO.copyRecursive(new File(epub, "META-INF"), new File(output, "META-INF"));

		logger.fine("Found " + opfPaths.size() + " opf-file" + (opfPaths.size() == 1 ? "." : "s."));
		for (String path : opfPaths) {
			logger.fine("Reading " + path);
			OPFReader opfReader = new OPFReader();
			OPF opf = opfReader.parse(epub, path);
			merge(opf, output);
		}
	}

	private void merge(OPF opf, File output) throws EPUB3ReaderException {
		String contentName = "package.opf.html";
		File opfFile = new File(epub, opf.getPath());
		File baseFolder = opfFile.getParentFile();
		System.out.println(baseFolder);

		File resultFile = new File(output, opf.getPath());
		resultFile.getParentFile().mkdirs();

		File contentFile = new File(resultFile.getParentFile(), contentName);
		Map<String, String> manifest = new HashMap<String, String>(opf.getManifest());
		logger.info("Merging spine to " + resultFile);
		File f;
		String path;

		for (String idref : opf.getSpine()) {
			manifest.remove(idref);
		}

		resultFile.getParentFile().mkdirs();
		{
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("content", contentName);
			try {
				XMLTools.transform(opfFile, resultFile, this.getClass().getResource("resource-files/opf-merge-spine.xslt"), params);
			} catch (XMLToolsException e) {
				throw new EPUB3ReaderException(e);
			}
		}

		{
			Map<String, Object> params = new HashMap<String, Object>();
			try {
				XMLTools.transform(opfFile, contentFile, this.getClass().getResource("resource-files/opf-merge-content-docs.xslt"), params);
			} catch (XMLToolsException e) {
				throw new EPUB3ReaderException(e);
			}
		}

		logger.info("Copying resources...");
		File f2;
		for (String id : manifest.keySet()) {
			try {
				path = manifest.get(id);
				f = new File(baseFolder, path).getCanonicalFile();
				if (f.exists()) {
					f2 = new File(resultFile.getParentFile(), path).getCanonicalFile();
					logger.fine("Moving '" + f + "' --> '" + f2 + "'");
					f2.getParentFile().mkdirs();
					FileIO.copyFile(f, f2);

				} else {
					logger.info("Referenced file cannot be found (if several OPF-files use the same resource, this could be correct).");
				}
			} catch (IOException e) {
				logger.log(Level.WARNING, "Failed to resolve path reference: " + id, e);
			}
		}
	}




}
