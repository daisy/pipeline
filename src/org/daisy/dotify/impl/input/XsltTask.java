package org.daisy.dotify.impl.input;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;

/**
 * <p>Task that runs an XSLT conversion.</p>
 * <p>Input file type requirement: XML</p>
 * 
 * @author  Joel Hakansson
 * @version 4 maj 2009
 * @since 1.0
 */
public class XsltTask extends ReadWriteTask {
	final URL url;
	final Map<String, Object> options;
	
	/**
	 * Create a new XSLT task. Use system property javax.xml.transform.TransformerFactory
	 * to set factory implementation if needed.
	 * @param name task name
	 * @param url relative path to XSLT
	 * @param options XSLT parameters
	 */
	public XsltTask(String name, URL url, Map<String, Object> options) {
		super(name);
		this.url = url;
		this.options = options;
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {
			XMLTools.transform(input.getFile(), output, url, options, new net.sf.saxon.TransformerFactoryImpl());
		} catch (XMLToolsException e) {
			throw new InternalTaskException("Error: ", e);
		}
		return new DefaultAnnotatedFile.Builder(output).extension("xslt").mediaType("application/xslt+xml").build();
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

}
