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
import java.util.Map;
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

import org.daisy.braille.utils.pef.PEFFileSplitter;
import org.daisy.braille.utils.pef.PEFHandler;
import org.daisy.braille.utils.pef.PEFHandler.Alignment;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.pef.FileFormatRegistry;

import org.xml.sax.SAXException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PEF2TextStep extends DefaultStep implements XProcStep {
	
	private static final QName _output_dir = new QName("output-dir");
	private static final QName _file_format = new QName("file-format");
	private static final QName _line_breaks = new QName("line-breaks");
	private static final QName _page_breaks = new QName("page-breaks");
	private static final QName _pad = new QName("pad");
	private static final QName _charset = new QName("charset");
	private static final QName _name_pattern = new QName("name-pattern");
	private static final QName _number_width = new QName("number-width");
	private static final QName _single_volume_name = new QName("single-volume-name");
	
	private final FileFormatRegistry fileFormatRegistry;
	
	private ReadablePipe source = null;
	
	private PEF2TextStep(XProcRuntime runtime,
	                     XAtomicStep step,
	                     FileFormatRegistry fileFormatRegistry) {
		super(runtime, step);
		this.fileFormatRegistry = fileFormatRegistry;
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
		q.removeAll("blank-last-page"); // has been handled in pef2text.xpl
		q.removeAll("sheets-multiple-of-two"); // has been handled in pef2text.xpl
		addOption(_line_breaks, q);
		addOption(_page_breaks, q);
		addOption(_pad, q);
		addOption(_charset, q);
		logger.debug("Finding file format for query: " + q);
		Iterable<FileFormat> fileFormats = fileFormatRegistry.get(q);
		if (!fileFormats.iterator().hasNext()) {
			throw new XProcException(step, "No file format found for query: " + q); }
		for (FileFormat fileFormat : fileFormats) {
			try {
				logger.debug("Storing PEF to file format: " + fileFormat);
				
				// Initialize output directory
				File textDir = new File(new URI(getOption(_output_dir).getString()));
				textDir.mkdirs();
				
				// Read source PEF
				ByteArrayOutputStream s = new ByteArrayOutputStream();
				Serializer serializer = runtime.getProcessor().newSerializer();
				serializer.setOutputStream(s);
				serializer.setCloseOnCompletion(true);
				serializer.setOutputProperty(Serializer.Property.INDENT, "yes");
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
					logger.error("name-pattern is invalid: '" + pattern + "'");
					if (singleVolumeName.isEmpty())
						throw new RuntimeException("name-pattern and single-volume-name may not both be empty");
				}
				if ((fileFormat.supportsVolumes() && !singleVolumeName.isEmpty())
				    || match < 0 || match != pattern.lastIndexOf("{}")) {
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
					// FIXME: to validating the result PEFs, get a PEFValidator instance
					// (implemented in dotify.task.impl) through the streamline API.
					PEFFileSplitter splitter = new PEFFileSplitter(x -> true);
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
		throw new XProcException(step, "pef:pef2text failed");
	}
	
	private void convertPEF2Text(InputStream pefStream, File textFile, FileFormat fileFormat)
			throws ParserConfigurationException, SAXException, IOException, UnsupportedWidthException {
		OutputStream textStream = new FileOutputStream(textFile);
		if ("pef".equals(fileFormat.getIdentifier())) {

			// just write pefStream to textFile without parsing it
			byte[] buf = new byte[153600];
			int length;
			while ((length = pefStream.read(buf)) > 0)
				textStream.write(buf, 0, length);
		} else {
			EmbosserWriter writer = fileFormat.newEmbosserWriter(textStream);
			PEFHandler.Builder builder = new PEFHandler.Builder(writer);
			builder.range(null).align(Alignment.LEFT).offset(0);
			parsePefFile(pefStream, builder.build());
		}
		textStream.close();
	}
	
	private void addOption(QName option, MutableQuery query) {
		RuntimeValue v = getOption(option);
		if (v != null && !"".equals(v.getString()))
			query.add(option.getLocalName(), v.getString());
	}
	
	@Component(
		name = "pxi:pef2text",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}pef2text" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new PEF2TextStep(runtime, step, fileFormatRegistry);
		}
		
		@Reference(
			name = "FileFormatRegistry",
			unbind = "-",
			service = FileFormatRegistry.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindFileFormatRegistry(FileFormatRegistry registry) {
			fileFormatRegistry = registry;
		}
		
		private FileFormatRegistry fileFormatRegistry;
		
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
