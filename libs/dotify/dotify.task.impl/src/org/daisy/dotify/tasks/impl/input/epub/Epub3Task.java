package org.daisy.dotify.tasks.impl.input.epub;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.daisy.dotify.common.io.FileIO;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;

/**
 * Provides an epub to html task.
 * @author Joel Håkansson
 */
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
	@Deprecated
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {
			File unpacked = FileIO.createTempDir();
			try {
				logger.info("Unpacking...");
				ContentExtractor.unpack(Files.newInputStream(input.getPath()), unpacked);
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
		return new DefaultAnnotatedFile.Builder(output.toPath()).extension("html").mediaType("application/xhtml+xml").build();
	}
	
	@Override
	public List<UserOption> getOptions() {
		List<UserOption> options = new ArrayList<>();
		options.add(new UserOption.Builder("opf-path").description("Specifies a specific opf, if there are more than one in the file.").build());
		return options;
	}

}
