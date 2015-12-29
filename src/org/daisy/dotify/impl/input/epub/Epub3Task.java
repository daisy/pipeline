package org.daisy.dotify.impl.input.epub;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.common.io.FileIO;

public class Epub3Task extends ReadWriteTask {
	private final Logger logger;
	private final String opfPath;
	
	Epub3Task(String name) {
		this(name, null);
	}

	Epub3Task(String name, String opfPath) {
		super(name);
		this.logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.opfPath = opfPath;
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		try {
			File unpacked = FileIO.createTempDir();
			try {
				logger.info("Unpacking...");
				ContentExtractor.unpack(new FileInputStream(input), unpacked);
				logger.info("Merging content files...");
				ContainerReader container = new ContainerReader(unpacked);
				ContentMerger merger = new ContentMerger(container);
				if (opfPath==null && container.getOPFPaths().size()>1) {
					StringBuilder sb = new StringBuilder("Epub container contains more than one OPF:");
					for (String s : container.getOPFPaths()) {
						sb.append(" ");
						sb.append(s);
					}
					throw new InternalTaskException(sb.toString());
				}
				merger.makeSingleContentDocument(opfPath==null?container.getOPFPaths().get(0):opfPath, output);
			} catch (EPUB3ReaderException e) {
				throw new InternalTaskException(e);
			} finally {
				logger.info("Deleting temp folder: " + unpacked);
				FileIO.deleteRecursive(unpacked);
			}
		} catch (IOException e) {
			throw new InternalTaskException(e);
		}
	}

}
