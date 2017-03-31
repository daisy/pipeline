package org.daisy.dotify.tasks.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.api.tasks.TaskOption;
import org.daisy.dotify.api.tasks.TaskOptionValue;
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
	private static final Logger logger = Logger.getLogger(XsltTask.class.getCanonicalName());
	final URL url;
	final Map<String, Object> options;
	List<TaskOption> uiOptions;
	
	/**
	 * <p>Create a new XSLT task. Use system property javax.xml.transform.TransformerFactory
	 * to set factory implementation if needed.</p>
	 * 
	 * <p>User options are collected from the xslt
	 * itself.</p>
	 * 
	 * <ul><li>The value of the attribute <code>@dotify:desc</code> on any top level
	 * <code>xsl:param</code> within the xslt will be used as description
	 * for the parameter.</li>
	 * 
	 * <li>The value of the attribute <code>@dotify:default</code> on any top level
	 * <code>xsl:param</code> within the xslt will be used as default
	 * value for the parameter. (Determining the default value from @select is
	 * unfortunately very tricky to do from another XSLT as it requires dynamic 
	 * XPath evaluation and, in the general cases, access to the input document
	 * as well.)</li>
	 * 
	 * <li>The value of the attribute <code>@dotify:values</code> on any top level
	 * <code>xsl:param</code> within the xslt will be used to enumerate
	 * acceptable values for the parameter.</li>
	 * </ul>
	 * <p>The namespace for <code>@dotify:*</code> should be 
	 * <code>http://brailleapps.github.io/ns/dotify</code></p>
	 * 
	 * @param name task name
	 * @param url relative path to XSLT
	 * @param options XSLT parameters
	 */
	public XsltTask(String name, URL url, Map<String, Object> options) {
		this(name, url, options, null);
	}
	/**
	 * Creates a new XSLT task. Use system property javax.xml.transform.TransformerFactory
	 * to set factory implementation if needed.
	 * @param name the task name
	 * @param url the relative path to the XSLT
	 * @param options the xslt parameters
	 * @param uiOptions the options presented to a user
	 */
	public XsltTask(String name, URL url, Map<String, Object> options, List<TaskOption> uiOptions) {
		super(name);
		this.url = url;
		this.options = options;
		this.uiOptions = uiOptions;
	}
	
	private List<TaskOption> buildOptions() {
		List<TaskOption> ret = new ArrayList<>();
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			XMLTools.transform(url, os, this.getClass().getResource("resource-files/list-params.xsl"), new HashMap<String, Object>(),  new net.sf.saxon.TransformerFactoryImpl());
			Properties px = new Properties();
			px.loadFromXML(new ByteArrayInputStream(os.toByteArray()));
			for (Entry<Object, Object> entry : px.entrySet()) {
				List<String> fields = splitFields(entry.getValue().toString());
				TaskOption.Builder builder = new TaskOption.Builder(entry.getKey().toString());
				if (fields.size()>0) {
					builder.defaultValue(fields.get(0));
					if (fields.size()>1) {
						if (!"".equals(fields.get(1))) {
							String[] values = fields.get(1).split("/");
							for (String value : values) {
								builder.addValue(new TaskOptionValue.Builder(value).build());
							}
						}
						if (fields.size()>2) {
							builder.description(fields.get(2));
						}
					}
				}
				ret.add(builder.build());
			}
		} catch (XMLToolsException | IOException e) {
			logger.log(Level.FINE, "Failed to compile options for xslt: " + url, e);
		} 
		return ret;
	}
	
	static List<String> splitFields(String input) {
		List<String> ret = new ArrayList<>();
		int last = 0;
		// not using split here, because the last field is wanted, even if it is empty
		for (int i=0; i<input.length(); i++) {
			if (input.charAt(i)=='\t') {
				ret.add(input.substring(last, i));
				last = i+1;
			}
		}
		ret.add(input.substring(last, input.length()));
		return ret;
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {
			XMLTools.transform(input.getFile(), output, url, options, new net.sf.saxon.TransformerFactoryImpl());
		} catch (XMLToolsException e) {
			throw new InternalTaskException("Error: ", e);
		}
		return new DefaultAnnotatedFile.Builder(output).extension("xml").mediaType("application/xml").build();
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public List<TaskOption> getOptions() {
		if (uiOptions==null) {
			this.uiOptions = buildOptions();
		}
		return uiOptions;
	}

}
