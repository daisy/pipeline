package org.daisy.pipeline.braille.pef.calabash.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.NoSuchElementException;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.braille.utils.pef.TextHandler;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableCatalogService;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.pef.TableRegistry;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Text2PEFStep extends DefaultStep implements XProcStep {
	
	private ReadablePipe source = null;
	private WritablePipe result = null;
	
	private static final QName _temp_dir = new QName("temp-dir");
	private static final QName _table = new QName("table");
	private static final QName _title = new QName("title");
	private static final QName _creator = new QName("creator");
	private static final QName _duplex = new QName("duplex");
	
	private final TableCatalogService tableCatalog;
	private final TableRegistry tableRegistry;
	
	private Text2PEFStep(XProcRuntime runtime,
	                     XAtomicStep step,
	                     TableCatalogService tableCatalog,
	                     TableRegistry tableRegistry) {
		super(runtime, step);
		this.tableCatalog = tableCatalog;
		this.tableRegistry = tableRegistry;
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		source = pipe;
	}
		
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}
	
	@Override
	public void reset() {
		source.resetReader();
		result.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			
			Query tableQuery = query(getOption(_table).getString());
			Table table = null;
			try {
				table = tableRegistry.get(tableQuery).iterator().next(); }
			catch (NoSuchElementException e) {
				throw new XProcException(step, "Could not find a table for query: " + tableQuery); }
			
			File tempDir = new File(new URI(getOption(_temp_dir).getString()));
			XdmNode text = source.read();
			
			// Write text document to file
			File textFile = File.createTempFile("text2pef.", ".txt", tempDir);
			OutputStream textStream = new FileOutputStream(textFile);
			OutputStreamWriter writer = new OutputStreamWriter(textStream, "UTF-8");
			writer.write(text.getStringValue());
			writer.close();
			
			// Parse text to PEF
			File pefFile = File.createTempFile("text2pef.", ".pef", tempDir);
			TextHandler.Builder b = new TextHandler.Builder(textFile, pefFile, tableCatalog);
			b.title(getOption(_title, ""));
			b.author(getOption(_creator, ""));
			b.duplex(getOption(_duplex, false));
			b.converterId(table.getIdentifier());
			TextHandler handler = b.build();
			handler.parse();
			textFile.delete();
			
			// Read PEF document
			XdmNode pef = runtime.getProcessor().newDocumentBuilder().build(pefFile);
			pefFile.delete();
			result.write(pef); }
		
		catch (Throwable e) {
			throw XProcStep.raiseError(e, step); }
	}
	
	@Component(
		name = "pef:text2pef",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/2008/pef}text2pef" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new Text2PEFStep(runtime, step, tableCatalog, tableRegistry);
		}
		
		private TableCatalogService tableCatalog;
		
		@Reference(
			name = "TableCatalog",
			unbind = "-",
			service = TableCatalogService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		public void setTableCatalog(TableCatalogService catalog) {
			tableCatalog = catalog;
		}
		
		@Reference(
			name = "TableRegistry",
			unbind = "-",
			service = TableRegistry.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindTableRegistry(TableRegistry registry) {
			tableRegistry = registry;
		}
		
		private TableRegistry tableRegistry;
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(Text2PEFStep.class);
	
}
