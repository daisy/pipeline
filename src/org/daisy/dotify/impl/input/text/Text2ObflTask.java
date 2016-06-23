package org.daisy.dotify.impl.input.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.api.tasks.TaskOption;

class Text2ObflTask extends ReadWriteTask {
	private final String encoding;
	private final String rootLang;
	private final Map<String, Object> params;
	private static List<TaskOption> options = null;
	
	Text2ObflTask(String name, String rootLang, Map<String, Object> params) {
		this(name, rootLang, "utf-8", params);
	}

	Text2ObflTask(String name, String rootLang, String encoding, Map<String, Object> params) {
		super(name);
		this.rootLang = rootLang;
		this.encoding = encoding;
		this.params = params;
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {
			Text2ObflWriter fw = new Text2ObflWriter(input.getFile(), output, encoding);
			fw.setRootLang(rootLang);
		
			{
				Object v = params.get("page-width");
				if (v!=null) {
					try {
						fw.setWidth(Integer.parseInt(v.toString()));
					} catch (Exception e) {
						Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Failed to set page width.", e);
					}
				}
			}
			{
				Object v = params.get("page-height");
				if (v!=null) {
					try {
						fw.setHeight(Integer.parseInt(v.toString()));
					} catch (Exception e) {
						Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Failed to set page height.", e);
					}
				}
				fw.parse();
			}
		} catch (FileNotFoundException e) {
			throw new InternalTaskException("FileNotFoundException", e);
		} catch (IOException e) {
			throw new InternalTaskException("IOException", e);
		}
		return new DefaultAnnotatedFile.Builder(output).extension("obfl").mediaType("application/x-obfl+xml").build();
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public List<TaskOption> getOptions() {
		if (options==null) {
			options = new ArrayList<>();
			options.add(new TaskOption.Builder("page-width").description("The width of the page").defaultValue("32").build());
			options.add(new TaskOption.Builder("page-height").description("The height of the page").defaultValue("29").build());
		}
		return options;
	}

}
