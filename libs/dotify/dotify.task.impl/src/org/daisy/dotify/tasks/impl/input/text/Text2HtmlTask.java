package org.daisy.dotify.tasks.impl.input.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;

class Text2HtmlTask extends ReadWriteTask {
	private static final String SOURCE_ENCODING = "source-encoding";
	private static final String SOURCE_LANGUAGE = "source-language";
	private static final String DEFAULT_ENCODING = "utf-8";
	private final String encoding;
	private final String rootLang;
	private final Map<String, Object> params;
	private static List<UserOption> options = null;
	
	Text2HtmlTask(String name, String rootLang, Map<String, Object> params) {
		this(name, rootLang, getEncoding(params), params);
	}

	Text2HtmlTask(String name, String rootLang, String encoding, Map<String, Object> params) {
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
			Text2HtmlWriter fw = new Text2HtmlWriter(input.getPath().toFile(), output, encoding!=null?encoding:
				XMLTools.detectBomEncoding(Files.readAllBytes(input.getPath()))
					.map(v->v.name())
					.orElse(DEFAULT_ENCODING));
			fw.setRootLang(getLanguage());
			fw.parse();
		} catch (FileNotFoundException e) {
			throw new InternalTaskException("FileNotFoundException", e);
		} catch (IOException e) {
			throw new InternalTaskException("IOException", e);
		}
		return new DefaultAnnotatedFile.Builder(output.toPath()).extension("html").mediaType("application/xhtml+xml").build();
	}

	@Override
	@Deprecated
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public List<UserOption> getOptions() {
		if (options==null) {
			options = new ArrayList<>();
			options.add(new UserOption.Builder(SOURCE_ENCODING).description("The encoding of the input file").defaultValue("[detect]").build());
			options.add(new UserOption.Builder(SOURCE_LANGUAGE).description("The language of the input file").defaultValue(rootLang).build());
		}
		return options;
	}

}
