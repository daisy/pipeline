package org.daisy.dotify.impl.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.common.text.StringFilter;
import org.daisy.dotify.common.xml.EntityResolverCache;
import org.daisy.dotify.common.xml.XMLResolverAdapter;

/**
 * <p>Task that runs a list of StringFilters on the character data of the input file.</p>
 * <p>Input file type requirement: XML</p>
 * 
 * @author  Joel Håkansson
 * @version 4 maj 2009
 * @since 1.0
 */
public class TextNodeTask extends ReadWriteTask {
	private StringFilter filters;

	/**
	 * Create a new TextNodeTask.
	 * @param name task name
	 * @param filters ArrayList of StringFilters
	 */
	public TextNodeTask(String name, StringFilter filters) {
		super(name);
		this.filters = filters;
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
		inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);        
        inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
        
		inFactory.setXMLResolver(new XMLResolverAdapter(new EntityResolverCache()));
		TextNodeFilter tnf = null;

		try {
			tnf = new TextNodeFilter(inFactory.createXMLEventReader(new FileInputStream(input.getFile())), new FileOutputStream(output), filters);
			tnf.filter();
		} catch (FileNotFoundException e) {
			throw new InternalTaskException("FileNotFoundException:", e);
		} catch (XMLStreamException e) {
			throw new InternalTaskException("XMLStreamException:", e);
		} finally {
			if (tnf!=null) {
				try { tnf.close(); } catch (IOException e) { }
			}
		}
		return new DefaultAnnotatedFile.Builder(output).extension("xml").mediaType("application/xml").build();
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

}
