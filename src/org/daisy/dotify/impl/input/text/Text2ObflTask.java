package org.daisy.dotify.impl.input.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.cr.InternalTaskException;
import org.daisy.dotify.api.cr.ReadWriteTask;

class Text2ObflTask extends ReadWriteTask {
	private final String encoding;
	private final String rootLang;
	private final Map<String, Object> params;
	
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
	public void execute(File input, File output) throws InternalTaskException {
		try {
			Text2ObflWriter fw = new Text2ObflWriter(input, output, encoding);
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
	}

}
