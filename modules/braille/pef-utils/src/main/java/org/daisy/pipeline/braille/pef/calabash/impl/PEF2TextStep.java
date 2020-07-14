package org.daisy.pipeline.braille.pef.calabash.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.api.embosser.FileFormat;
import org.daisy.braille.api.table.Table;
import org.daisy.braille.api.validator.ValidatorFactoryService;
import org.daisy.braille.pef.PEFFileSplitter;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.pef.PEFHandler.Alignment;
import org.daisy.braille.pef.UnsupportedWidthException;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.pef.FileFormatProvider;
import org.daisy.pipeline.braille.pef.TableProvider;

import org.xml.sax.SAXException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PEF2TextStep extends DefaultStep implements XProcStep {
	
	private static final QName _dir_href = new QName("dir-href");
	private static final QName _file_format = new QName("file-format");
	private static final QName _table = new QName("table");
	private static final QName _line_breaks = new QName("line-breaks");
	private static final QName _page_breaks = new QName("page-breaks");
	private static final QName _pad = new QName("pad");
	private static final QName _charset = new QName("charset");
	private static final QName _name_pattern = new QName("name-pattern");
	private static final QName _number_width = new QName("number-width");
	private static final QName _single_volume_name = new QName("single-volume-name");
	
	private static final Query EN_US = mutableQuery().add("id", "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US");
	
	private final org.daisy.pipeline.braille.common.Provider<Query,FileFormat> fileFormatProvider;
	private final org.daisy.pipeline.braille.common.Provider<Query,Table> tableProvider;
	private final ValidatorFactoryService validatorFactory;
	
	private ReadablePipe source = null;
	
	private PEF2TextStep(XProcRuntime runtime,
	                     XAtomicStep step,
	                     org.daisy.pipeline.braille.common.Provider<Query,FileFormat> fileFormatProvider,
	                     org.daisy.pipeline.braille.common.Provider<Query,Table> tableProvider,
	                     ValidatorFactoryService validatorFactory) {
		super(runtime, step);
		this.fileFormatProvider = fileFormatProvider;
		this.tableProvider = tableProvider;
		this.validatorFactory = validatorFactory;
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		source = pipe;
	}
	
	@Override
	public void reset() {
		source.resetReader();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		MutableQuery q = mutableQuery(query(getOption(_file_format, "")));
		addOption(_line_breaks, q);
		addOption(_page_breaks, q);
		addOption(_pad, q);
		addOption(_charset, q);
		RuntimeValue tableQuery = getOption(_table);
		if (tableQuery != null) {
			Table table;
			try {
				logger.debug("Finding table for query: " + tableQuery);
				table = tableProvider.get(query(tableQuery.getString())).iterator().next();
				logger.debug("Found table: " + table); }
			catch (NoSuchElementException e) {
				
				// this fallback is done because in dtbook-to-pef we use the
				// query (locale:...) which does not always match something
				// FIXME: https://github.com/daisy/pipeline-mod-braille/issues/75
				logger.warn("Table not found, falling back to en-US table.");
				table = tableProvider.get(EN_US).iterator().next(); }
			q.add("table", table.getIdentifier()); }
		logger.debug("Finding file format for query: " + q);
		Iterable<FileFormat> fileFormats = fileFormatProvider.get(q);
		if (!fileFormats.iterator().hasNext()) {
			logger.error("pef:pef2text failed: no file format found for query: " + q);
			throw new XProcException(step.getNode(), "pef:pef2text failed: no file format found for query: " + q); }
		for (FileFormat fileFormat : fileFormats) {
			try {
				logger.debug("Storing PEF to file format: " + fileFormat);
				
				// Initialize output directory
				File textDir = new File(new URI(getOption(_dir_href).getString()));
				textDir.mkdirs();
				
				// Read source PEF
				ByteArrayOutputStream s = new ByteArrayOutputStream();
				Serializer serializer = runtime.getProcessor().newSerializer();
				serializer.setOutputStream(s);
				serializer.setCloseOnCompletion(true);
				serializer.serializeNode(source.read());
				serializer.close();
				InputStream pefStream = new ByteArrayInputStream(s.toByteArray());
				s.close();
				
				// Parse pattern
				String singleVolumeName = getOption(_single_volume_name, "");
				String pattern = getOption(_name_pattern, "");
				if (pattern.isEmpty())
					pattern = "volume-{}";
				int match = pattern.indexOf("{}");
				if (match < 0 || match != pattern.lastIndexOf("{}")) {
					// Output to single file
					convertPEF2Text(pefStream,
							new File(textDir, singleVolumeName + fileFormat.getFileExtension()), fileFormat);
				} else {
					// Split PEF
					pattern = pattern.replaceAll("'", "''")
							.replaceAll("([0#\\.,;%\u2030\u00A4-]+)", "'$1'");
					// Recalculate after replacement
					match = pattern.indexOf("{}");
					File splitDir = new File(textDir, "split");
					splitDir.mkdir();
					PEFFileSplitter splitter = new PEFFileSplitter(validatorFactory);
					String prefix = PEFFileSplitter.PREFIX;
					String postfix = PEFFileSplitter.POSTFIX;
					splitter.split(pefStream, splitDir, prefix, postfix);
					File[] pefFiles = splitDir.listFiles();
					String formatPattern = pattern.substring(0, match);
					int nWidth; {
						try {
							nWidth = Integer.parseInt(getOption(_number_width, "")); }
						catch (NumberFormatException e) {
							nWidth = 0; }}
					if (nWidth == 0)
						formatPattern += "###"; // Assume max 999 volumes
					else
						while (nWidth > 0) { formatPattern += "0"; nWidth--; }
					formatPattern += pattern.substring(match + 2);
					NumberFormat format = new DecimalFormat(formatPattern);
					for (File pefFile : pefFiles) {
						InputStream is = new FileInputStream(pefFile);
						if (pefFiles.length == 1 && !singleVolumeName.isEmpty()) {
							// Output to single file
							convertPEF2Text(is, new File(textDir, singleVolumeName + fileFormat.getFileExtension()), fileFormat);
						} else {
							String pefName = pefFile.getName();
							if (pefName.length() <= prefix.length() + postfix.length()
							    || !pefName.substring(0, prefix.length()).equals(prefix)
							    || !pefName.substring(pefName.length() - postfix.length()).equals(postfix)) {
								is.close();
								throw new RuntimeException("Coding error");
							}
							String textName = format.format(
									Integer.parseInt(pefName.substring(prefix.length(), pefName.length() - postfix.length())));
							convertPEF2Text(is,
									new File(textDir, textName + fileFormat.getFileExtension()),
									fileFormat);
						}
						is.close();
						if (!pefFile.delete()) pefFile.deleteOnExit();
					}
					pefStream.close();
					if (!splitDir.delete()) splitDir.deleteOnExit();
				}
				return; }
			catch (Exception e) {
				logger.error("Storing PEF to file format '" + fileFormat + "' failed", e); }}
		logger.error("pef:pef2text failed");
		throw new XProcException(step.getNode(), "pef:pef2text failed");
	}
	
	private void convertPEF2Text(InputStream pefStream, File textFile, FileFormat fileFormat)
			throws ParserConfigurationException, SAXException, IOException, UnsupportedWidthException {
		// Create EmbosserWriter
		OutputStream textStream = new FileOutputStream(textFile);
		EmbosserWriter writer = fileFormat.newEmbosserWriter(textStream);
		
		// Parse PEF to text
		PEFHandler.Builder builder = new PEFHandler.Builder(writer);
		builder.range(null).align(Alignment.LEFT).offset(0);
		parsePefFile(pefStream, builder.build());
		textStream.close();
	}
	
	private void addOption(QName option, MutableQuery query) {
		RuntimeValue v = getOption(option);
		if (v != null)
			query.add(option.getLocalName(), v.getString());
	}
	
	@Component(
		name = "pef:pef2text",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/2008/pef}pef2text" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new PEF2TextStep(runtime, step, fileFormatProvider, tableProvider, validatorFactory);
		}
		
		@Reference(
			name = "FileFormatProvider",
			unbind = "unbindFileFormatProvider",
			service = FileFormatProvider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindFileFormatProvider(FileFormatProvider provider) {
			fileFormatProviders.add(provider);
		}
		
		protected void unbindFileFormatProvider(FileFormatProvider provider) {
			fileFormatProviders.remove(provider);
			this.fileFormatProvider.invalidateCache();
		}
		
		private List<FileFormatProvider> fileFormatProviders = new ArrayList<FileFormatProvider>();
		private org.daisy.pipeline.braille.common.Provider.util.MemoizingProvider<Query,FileFormat> fileFormatProvider
		= memoize(dispatch(fileFormatProviders));
		
		@Reference(
			name = "TableProvider",
			unbind = "unbindTableProvider",
			service = TableProvider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindTableProvider(TableProvider provider) {
			tableProviders.add(provider);
		}
		
		protected void unbindTableProvider(TableProvider provider) {
			tableProviders.remove(provider);
			this.tableProvider.invalidateCache();
		}
		
		private List<TableProvider> tableProviders = new ArrayList<TableProvider>();
		private org.daisy.pipeline.braille.common.Provider.util.MemoizingProvider<Query,Table> tableProvider
		= memoize(dispatch(tableProviders));
		
		@Reference(
			name = "ValidatorFactoryService",
			service = ValidatorFactoryService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindValidatorFactory(ValidatorFactoryService factory) {
			validatorFactory = factory;
		}
		
		private ValidatorFactoryService validatorFactory = null;
	}
	
	// copied from org.daisy.braille.facade.PEFConverterFacade because it is no longer static
	/**
	 * Parses the given input stream using the supplied PEFHandler.
	 * @param is the input stream
	 * @param ph the PEFHandler
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws UnsupportedWidthException
	 */
	private static void parsePefFile(InputStream is, PEFHandler ph)
			throws ParserConfigurationException, SAXException, IOException, UnsupportedWidthException {
		
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser sp = spf.newSAXParser();
		try {
			sp.parse(is, ph); }
		catch (SAXException e) {
			if (ph.hasWidthError())
				throw new UnsupportedWidthException(e);
			else
				throw e; }
	}
	
	private static final Logger logger = LoggerFactory.getLogger(PEF2TextStep.class);
	
}
