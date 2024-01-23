package org.daisy.pipeline.braille.pef.calabash.impl.pdf; // dedicated package to make openhtmltopdf dependency optional

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.PageSizeUnits;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.pipeline.braille.pef.TableRegistry;
import org.daisy.common.file.URLs;
import org.daisy.common.shell.CommandRunner;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.OutputValue;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.file.FileUtils.cResultDocument;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PEF2PDFStep extends DefaultStep implements XProcStep {

	private static final Logger logger = LoggerFactory.getLogger(PEF2PDFStep.class);

	private static final QName _SOURCE = new QName("source");
	private static final net.sf.saxon.s9api.QName _HREF = new net.sf.saxon.s9api.QName("href");
	private static final net.sf.saxon.s9api.QName _TABLE = new net.sf.saxon.s9api.QName("table");
	private static final String DEFAULT_TABLE = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";

	private static final URL font = URLs.getResourceFromJAR("odt2braille8.ttf", PEF2PDFStep.class);

	private final TableRegistry tableRegistry;
	private ReadablePipe source = null;
	private WritablePipe result = null;

	private PEF2PDFStep(XProcRuntime runtime,
	                    XAtomicStep step,
	                    TableRegistry tableRegistry) {
		super(runtime, step);
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
			URI href = null; {
				try {
					href = URI.create(getOption(_HREF).getString());
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Not a valid URI: " + href);
				}
			}
			File pdfFile = null; {
				try {
					pdfFile = new File(href);
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Not a valid file URI: " + href);
				}
			}
			if (pdfFile.exists())
				throw new RuntimeException("File exists: " + pdfFile);
			Table table = null; {
				MutableQuery q = mutableQuery(query(getOption(_TABLE, "")));
				logger.debug("Finding table for query: " + q);
				try {
					table = tableRegistry.get(q).iterator().next();
				} catch (NoSuchElementException e) {
					try {
						table = tableRegistry.get(mutableQuery().add("id", DEFAULT_TABLE)).iterator().next();
						logger.warn("Can not find a table for query: " + q);
						logger.warn("Falling back to default");
					} catch (NoSuchElementException ee) {
						throw new IllegalArgumentException("Could not find a table for query: " + q);
					}
				}
			}
			logger.debug("Storing PEF to PDF using table: " + table);
			new PEF2PDF(pdfFile, table).transform(
				ImmutableMap.of(_SOURCE, new XMLCalabashInputValue(source)),
				ImmutableMap.of()
			).run();
			result.write(runtime.getProcessor().newDocumentBuilder().build(new StreamSource(cResultDocument(pdfFile.toURI().toString()))));
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	@Component(
		name = "pxi:pef2pdf",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}pef2pdf" }
	)
	public static class Provider implements XProcStepProvider {

		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new PEF2PDFStep(runtime, step, tableRegistry);
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

	private static final String PEF_NS = "http://www.daisy.org/ns/2008/pef";
	private static final String DP2_NS = "http://www.daisy.org/ns/pipeline/";

	private static final QName PEF_PEF = new QName(PEF_NS, "pef");
	private static final QName PEF_HEAD = new QName(PEF_NS, "head");
	private static final QName PEF_META = new QName(PEF_NS, "meta");
	private static final QName PEF_BODY = new QName(PEF_NS, "body");
	private static final QName PEF_VOLUME = new QName(PEF_NS, "volume");
	private static final QName PEF_SECTION = new QName(PEF_NS, "section");
	private static final QName PEF_PAGE = new QName(PEF_NS, "page");
	private static final QName PEF_ROW = new QName(PEF_NS, "row");
	private static final QName _DUPLEX = new QName("duplex");
	private static final QName _COLS = new QName("cols");
	private static final QName _ROWS = new QName("rows");
	private static final QName _ROWGAP = new QName("rowgap");
	private static final QName DP2_ASCII = new QName(DP2_NS, "ascii");
	private static final QName DP2_ASCII_BRAILLE_CHARSET = new QName(DP2_NS, "ascii-braille-charset");

	public class PEF2PDF implements XMLTransformer {

		private final File pdf;
		private final Table table;

		public PEF2PDF(File pdf, Table table) {
			this.pdf = pdf;
			this.table = table;
		}

		@Override
		public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output) {
			input = XMLTransformer.validateInput(input, ImmutableMap.of(_SOURCE, InputType.MANDATORY_NODE_SEQUENCE));
			output = XMLTransformer.validateOutput(output, null);
			XMLInputValue<?> source = (XMLInputValue<?>)input.get(_SOURCE);
			return () -> {
				try {
					org.daisy.common.stax.BaseURIAwareXMLStreamReader pef = source.asXMLStreamReader();
					ByteArrayOutputStream htmlBytes = new ByteArrayOutputStream();
					Writer html = new OutputStreamWriter(htmlBytes, StandardCharsets.UTF_8);
					BrailleConverter bc = table.newBrailleConverter();
					boolean tableMatchesBrailleCharset = false;
					int maxColumns = 0; // in cells per line
					int maxRows = 0; // in lines
					int marginLeft = 10; // in mm
					int marginTop = 10; // in mm
					LinkedList<QName> elementStack = new LinkedList<>();
					LinkedList<Boolean> duplexStack = new LinkedList<>();
					LinkedList<Integer> colsStack = new LinkedList<>();
					LinkedList<Integer> rowsStack = new LinkedList<>();
					LinkedList<Integer> rowgapStack = new LinkedList<>();
					int volumeCount = 0;
					int pagesInSection = 0;
					events: while (true)
						try {
							int event = pef.next();
							switch (event) {
							case START_ELEMENT: {
								QName elemName = pef.getName();
								switch (elementStack.size()) {
								case 0:
									if (PEF_PEF.equals(elemName)) {
										html.write(
											"<!DOCTYPE html>\n" +
											"<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
											"	<head>\n" +
											"		<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
											"		<style type=\"text/css\">\n" +
											"		/*<![CDATA[*/\n" +
											"@font-face {\n" +
											"	font-family: odt2braille;\n" +
											"	src: url(data:font/truetype;charset=utf8;base64,"
											         // since Java 9 there is also InputStream.readAllBytes()
											         + Base64.getEncoder().encodeToString(ByteStreams.toByteArray(font.openStream())) + ")\n" +
											"	     format(\"truetype\");\n" +
											"}\n" +
											"@page {\n" +
											String.format("	margin-left: %dmm;\n", marginLeft) +
											String.format("	margin-top: %dmm;\n", marginTop) +
											"	margin-right: 0mm;\n" +
											"	margin-bottom: 0mm;\n" +
											"}\n" +
											"body {\n" +
											"	font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\n" +
											"	margin: 0;\n" +
											"	font-size: 25px;\n" +
											"}\n" +
											"h1.bookmark {\n" +
											"	position: absolute;\n" +
											"	left: -1000mm;\n" +
											"	font-size: 1px;\n" +
											"}\n" +
											".page {\n" +
											"	page-break-before: always;\n" +
											"}\n" +
											".row {\n" +
											"    font-family: odt2braille;\n" +
											"    letter-spacing: 0px;\n" +
											"    white-space: pre;\n" +
											"    letter-spacing: 0px;\n" +
											"    font-size: 125%;\n" +
											"    height: 1em;\n" +
											"}\n" +
											".row[rowgap=\"1\"] {\n" +
											"    height: 1.25em;\n" +
											"}\n" +
											".row[rowgap=\"2\"] {\n" +
											"    height: 1.5em;\n" +
											"}\n" +
											".row[rowgap=\"3\"] {\n" +
											"    height: 1.75em;\n" +
											"}\n" +
											".row[rowgap=\"4\"] {\n" +
											"    height: 2em;\n" +
											"}\n" +
											"		/*]]>*/\n" +
											"		</style>\n" +
											"	</head>\n" +
											"	<body>\n");
										elementStack.push(elemName);
										continue events;
									}
									break;
								case 1:
									if (PEF_HEAD.equals(elemName) || PEF_BODY.equals(elemName)) {
										elementStack.push(elemName);
										continue events;
									}
									break;
								case 2:
									if (elementStack.get(0).equals(PEF_HEAD)) {
										if (PEF_META.equals(elemName)) {
											elementStack.push(elemName);
											continue events;
										}
									} else { // BODY
										if (PEF_VOLUME.equals(elemName)) {
											duplexStack.push(getDuplex(pef, duplexStack));
											colsStack.push(getCols(pef, colsStack));
											rowsStack.push(getRows(pef, rowsStack));
											rowgapStack.push(getRowgap(pef, rowgapStack));
											elementStack.push(elemName);
											volumeCount++;
											// FIXME: bookmarks supported by wkhtmltopdf but not by openhtmltopdf
											html.write("		<div class=\"volume\">\n" +
											           "			<h1 class=\"bookmark\">Volume " + volumeCount + "</h1>\n");
											continue events;
										}
									}
									break;
								case 3:
									if (elementStack.get(0).equals(PEF_META)) {
										if (DP2_ASCII_BRAILLE_CHARSET.equals(elemName)) {
											String charset = pef.getElementText(); // consumes whole element
											if (charset.equals(table.getIdentifier()))
												tableMatchesBrailleCharset = true;
											continue events;
										}
									} else {// VOLUME
										if (PEF_SECTION.equals(elemName)) {
											pagesInSection = 0;
											int cols = getCols(pef, colsStack);
											if (cols > maxColumns)
												maxColumns = cols;
											int rows = getRows(pef, rowsStack);
											if (rows > maxRows)
												maxRows = rows;
											duplexStack.push(getDuplex(pef, duplexStack));
											rowgapStack.push(getRowgap(pef, rowgapStack));
											elementStack.push(elemName);
											continue events;
										}
									}
									break;
								case 4:
									if (elementStack.get(0).equals(PEF_SECTION)) {
										if (PEF_PAGE.equals(elemName)) {
											pagesInSection++;
											rowgapStack.push(getRowgap(pef, rowgapStack));
											elementStack.push(elemName);
											html.write("			<div class=\"page\">\n");
											continue events;
										}
									}
									break;
								case 5:
									if (elementStack.get(0).equals(PEF_PAGE)) {
										if (PEF_ROW.equals(elemName)) {
											int rowgap = getRowgap(pef, rowgapStack);
											String row = pef.getElementText(); // consumes whole element
											if (tableMatchesBrailleCharset) {
												String attr = pef.getAttributeValue(DP2_ASCII.getNamespaceURI(), DP2_ASCII.getLocalPart());
												if (attr != null)
													row = attr;
												else
													row = bc.toText(row);
											} else
												row = bc.toText(row);
											html.write("				<div class=\"row\" rowgap=\"" + rowgap + "\">");
											html.write(row.replace("&", "&amp;")
											              .replace("<", "&lt;")
											              .replace(">", "&gt;") + "</div>\n");
											continue events;
										}
									}
									break;
								default:
								}
								pef.getElementText(); // consume whole element
								break;
							}
							case END_ELEMENT: {
								QName elemName = elementStack.pop();
								if (PEF_PAGE.equals(elemName)) {
									rowgapStack.pop();
									html.write("			</div>\n");
								} else if (PEF_SECTION.equals(elemName)) {
									if (duplexStack.pop()) {
										if (pagesInSection % 2 != 0) {
											// insert empty page
											html.write("			<div class=\"page\">\n");
											html.write("			</div>\n");
										}
									}
									rowgapStack.pop();
								} else if (PEF_VOLUME.equals(elemName)) {
									duplexStack.pop();
									colsStack.pop();
									rowsStack.pop();
									rowgapStack.pop();
									html.write("		</div>\n");
								} else if (PEF_PEF.equals(elemName)) {
									html.write("	</body>\n" +
									           "</html>\n");
								}
								break;
							}
							default:
							}
						} catch (NoSuchElementException e) {
							break;
						}
					html.flush();
					pdf.getParentFile().mkdirs();
					int pageWidth = (int)Math.ceil(4.2 * maxColumns) + 2 * marginLeft;
					int pageHeight = (int)Math.ceil(8.44 * maxRows) + 2 * marginTop;
					try (OutputStream os = new FileOutputStream(pdf)) {
						new PdfRendererBuilder()
							.withHtmlContent(new String(htmlBytes.toByteArray(), StandardCharsets.UTF_8),
							                 pef.getBaseURI().toString())
							.useDefaultPageSize(pageWidth, pageHeight, PageSizeUnits.MM)
							.toStream(os)
							.run();
					}
				} catch (Throwable e) {
					throw new TransformerException(e);
				}
			};
		}

		public boolean getDuplex(XMLStreamReader pef, LinkedList<Boolean> stack) {
			// not using XMLStreamReader#getAttributeValue(String, String) because it is buggy
			// String attr = pef.getAttributeValue(_DUPLEX.getNamespaceURI(), _DUPLEX.getLocalPart());
			for (int i = 0; i < pef.getAttributeCount(); i++)
				if (_DUPLEX.equals(pef.getAttributeName(i)))
					return Boolean.parseBoolean(pef.getAttributeValue(i)); // assume "true" or "false"
			return stack.getFirst(); // throws NoSuchElementException if stack is empty, which can not
			                         // happen if PEF is valid
		}

		public int getCols(XMLStreamReader pef, LinkedList<Integer> stack) {
			for (int i = 0; i < pef.getAttributeCount(); i++)
				if (_COLS.equals(pef.getAttributeName(i)))
					return Integer.parseInt(pef.getAttributeValue(i)); // assume positive integer
			return stack.getFirst(); // throws NoSuchElementException if stack is empty, which can not
			                         // happen if PEF is valid
		}

		public int getRows(XMLStreamReader pef, LinkedList<Integer> stack) {
			for (int i = 0; i < pef.getAttributeCount(); i++)
				if (_ROWS.equals(pef.getAttributeName(i)))
					return Integer.parseInt(pef.getAttributeValue(i)); // assume positive integer
			return stack.getFirst(); // throws NoSuchElementException if stack is empty, which can not
			                         // happen if PEF is valid
		}

		public int getRowgap(XMLStreamReader pef, LinkedList<Integer> stack) {
			for (int i = 0; i < pef.getAttributeCount(); i++)
				if (_ROWGAP.equals(pef.getAttributeName(i)))
					return Integer.parseInt(pef.getAttributeValue(i)); // assume non-negative integer
			return stack.getFirst(); // throws NoSuchElementException if stack is empty, which can not
			                         // happen if PEF is valid
		}
	}
}
