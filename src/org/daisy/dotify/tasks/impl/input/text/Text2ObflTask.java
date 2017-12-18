package org.daisy.dotify.tasks.impl.input.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
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
import org.daisy.dotify.common.xml.XMLTools;

class Text2ObflTask extends ReadWriteTask {
	private static final String SOURCE_ENCODING = "source-encoding";
	private static final String SOURCE_LANGUAGE = "source-language";
	private static final String PAGE_WIDTH = "page-width";
	private static final String PAGE_HEIGHT = "page-height";
	private static final String DEFAULT_ENCODING = "utf-8";
	private final String encoding;
	private final String rootLang;
	private final Map<String, Object> params;
	private static List<TaskOption> options = null;
	
	Text2ObflTask(String name, String rootLang, Map<String, Object> params) {
		this(name, rootLang, getEncoding(params), params);
	}

	Text2ObflTask(String name, String rootLang, String encoding, Map<String, Object> params) {
		super(name);
		this.rootLang = rootLang;
		this.encoding = encoding;
		this.params = params;
	}
	
	private static String getEncoding(Map<String, Object> params) {
		Object param = params.get(SOURCE_ENCODING);
		return (param!=null)?""+param:null;
	}
	
	private String getLanguage() {
		Object param = params.get(SOURCE_LANGUAGE);
		return (param!=null)?""+param:rootLang;
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {
			Text2ObflWriter fw = new Text2ObflWriter(input.getFile(), output, encoding!=null?encoding:
				XMLTools.detectBomEncoding(Files.readAllBytes(input.getFile().toPath()))
					.map(v->v.name())
					.orElse(DEFAULT_ENCODING));
			fw.setRootLang(getLanguage());
		
			{
				Object v = params.get(PAGE_WIDTH);
				if (v!=null) {
					try {
						fw.setWidth(Integer.parseInt(v.toString()));
					} catch (Exception e) {
						Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, "Failed to set page width.", e);
					}
				}
			}
			{
				Object v = params.get(PAGE_HEIGHT);
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
			options.add(new TaskOption.Builder(PAGE_WIDTH).description("The width of the page").defaultValue("32").build());
			options.add(new TaskOption.Builder(PAGE_HEIGHT).description("The height of the page").defaultValue("29").build());
			options.add(new TaskOption.Builder(SOURCE_ENCODING).description("The encoding of the input file").defaultValue("[detect]").build());
			options.add(new TaskOption.Builder(SOURCE_LANGUAGE).description("The language of the input file").defaultValue(rootLang).build());
		}
		return options;
	}

}
