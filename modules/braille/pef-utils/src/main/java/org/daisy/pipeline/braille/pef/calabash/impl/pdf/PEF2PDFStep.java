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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

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

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.xpath.XPathFactoryImpl;

import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.pipeline.braille.pef.TableRegistry;
import org.daisy.common.file.URLs;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.shell.CommandRunner;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.Mult;
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
import org.daisy.pipeline.braille.css.EmbossedMedium;
import org.daisy.pipeline.css.Dimension;
import org.daisy.pipeline.css.Dimension.Unit;
import org.daisy.pipeline.css.Medium;
import static org.daisy.pipeline.file.FileUtils.cResultDocument;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Node;

public class PEF2PDFStep extends DefaultStep implements XProcStep {

	private static final Logger logger = LoggerFactory.getLogger(PEF2PDFStep.class);

	private static final QName _SOURCE = new QName("source");
	private static final net.sf.saxon.s9api.QName _HREF = new net.sf.saxon.s9api.QName("href");
	private static final net.sf.saxon.s9api.QName _TABLE = new net.sf.saxon.s9api.QName("table");
	private static final net.sf.saxon.s9api.QName _OFFSET_X = new net.sf.saxon.s9api.QName("offset-x");
	private static final net.sf.saxon.s9api.QName _OFFSET_Y = new net.sf.saxon.s9api.QName("offset-y");
	private static final net.sf.saxon.s9api.QName _SCALE_FONT = new net.sf.saxon.s9api.QName("scale-font");
	private static final net.sf.saxon.s9api.QName _FONT_COLOR = new net.sf.saxon.s9api.QName("font-color");
	private static final net.sf.saxon.s9api.QName _MEDIUM = new net.sf.saxon.s9api.QName("medium");
	private static final String DEFAULT_TABLE = "org.daisy.braille.impl.table.DefaultTableProvider.TableType.EN_US";

	private static final URL font = URLs.getResourceFromJAR("odt2braille8.ttf", PEF2PDFStep.class);
	/*
	 * pdfbox does not support OpenType fonts, nor does it support the font-face properties
	 * ascent-override, descent-override and size-adjust. NotCourierSans-Bold.ttf was created from
	 * NotCourierSans-Bold.otf using the Font Squirrel Webfont Generator tool
	 * (https://www.fontsquirrel.com/tools/webfont-generator). I used these settings:
	 *
	 * - Upload Fonts: NotCourierSans-Bold.otf
	 * - Font Formats: TrueType
	 * - Vertical Metrics: Custom Adjustment:
	 *   - Ascent: 70%
	 *   - Descent: 30%
	 * - X-height Matching: 83%
	 */
	private static final URL fallbackFont = URLs.getResourceFromJAR("NotCourierSans-Bold.ttf", PEF2PDFStep.class);

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
			Medium medium = SaxonHelper.objectFromItem(
				SaxonHelper.getSingleItem(getOption(_MEDIUM).getValue().getUnderlyingValue()),
				Medium.class);
			double offsetX = Dimension.parse(getOption(_OFFSET_X).getString()).toUnit(Unit.MM).getValue().doubleValue();
			double offsetY = Dimension.parse(getOption(_OFFSET_Y).getString()).toUnit(Unit.MM).getValue().doubleValue();
			double scaleFont = parseNumberOrPercentage(getOption(_SCALE_FONT).getString());
			if (scaleFont <= 0)
				throw new IllegalArgumentException("Font scaling factor must be a positive number, but got: " + scaleFont);
			String fontColor = getOption(_FONT_COLOR).getString();
			try {
				fontColor = String.format("#%06x", ColorFactory.valueOf(fontColor).getRGB() & 0xffffff);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Can not parse font color: " + fontColor, e);
			}
			logger.debug("Storing PEF to PDF using table: " + table);
			new PEF2PDF(pdfFile, table, medium, offsetX, offsetY, scaleFont, fontColor).transform(
				ImmutableMap.of(_SOURCE, XMLCalabashInputValue.of(source)),
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
	private static final QName _ROWGAP = new QName("rowgap");
	private static final QName DP2_ASCII = new QName(DP2_NS, "ascii");
	private static final QName DP2_ASCII_BRAILLE_CHARSET = new QName(DP2_NS, "ascii-braille-charset");

	public class PEF2PDF implements XMLTransformer {

		private final File pdf;
		private final Table table;
		private final Medium medium;
		private final double offsetX;
		private final double offsetY;
		private final double scaleFont;
		private final String fontColor;

		public PEF2PDF(File pdf, Table table, Medium medium, double offsetX, double offsetY, double scaleFont, String fontColor) {
			this.pdf = pdf;
			this.table = table;
			this.medium = medium;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.scaleFont = scaleFont;
			this.fontColor = fontColor;
		}

		@Override
		public Runnable transform(Map<QName,InputValue<?>> input, Map<QName,OutputValue<?>> output) {
			input = XMLTransformer.validateInput(input, ImmutableMap.of(_SOURCE, InputType.MANDATORY_NODE_SINGLE));
			output = XMLTransformer.validateOutput(output, null);
			Mult<? extends XMLInputValue<?>> source = ((XMLInputValue<?>)input.get(_SOURCE)).mult(2);
			return () -> {
				try {
					int maxColumns = 0; // cells per line
					int maxRows = 0; // lines
					int totalVolumes = 0; {
						Node node = source.get().asNodeIterator().next();
						// we know it's a Saxon object because this is called from the XProcStep
						XPath xpath = new XPathFactoryImpl(
							((NodeOverNodeInfo)node).getUnderlyingNodeInfo().getConfiguration()
						).newXPath();
						xpath.setNamespaceContext(
							new NamespaceContext() {
								public String getNamespaceURI(String prefix) {
									return "pef".equals(prefix) ? PEF_NS : null; }
								public String getPrefix(String namespaceURI) {
									return PEF_NS.equals(namespaceURI) ? "pef" : null; }
								public Iterator<String> getPrefixes(String namespaceURI) {
									return PEF_NS.equals(namespaceURI)
										? Collections.singleton("pef").iterator()
										: null; }});
						maxColumns = ((Double)xpath.evaluate("max(//pef:*/@cols/number(.))", node, XPathConstants.NUMBER))
							.intValue();
						maxRows = ((Double)xpath.evaluate("max(//pef:*/@rows/number(.))", node, XPathConstants.NUMBER))
							.intValue();
						totalVolumes = ((Double)xpath.evaluate("count(//pef:volume)", node, XPathConstants.NUMBER))
							.intValue();
					}
					BaseURIAwareXMLStreamReader pef = source.get().asXMLStreamReader();
					ByteArrayOutputStream htmlBytes = new ByteArrayOutputStream();
					Writer html = new OutputStreamWriter(htmlBytes, StandardCharsets.UTF_8);
					BrailleConverter bc = table.newBrailleConverter();
					boolean tableMatchesBrailleCharset = false;
					double cellHeight = 10;
					double cellWidth = 6;
					if (medium.getType() == Medium.Type.EMBOSSED && medium instanceof EmbossedMedium) {
						EmbossedMedium embossedMedium = (EmbossedMedium)medium;
						cellHeight = embossedMedium.getEm();
						cellWidth = embossedMedium.getCh();
					}
					double fontSize = cellHeight;
					double letterSpacing = 0;
					if (cellHeight < 2 * cellWidth)
						letterSpacing = cellWidth - cellHeight / 2;
					double lineHeight = 1; // em
					if (cellHeight > 2 * cellWidth) {
						fontSize = 2 * cellWidth;
						lineHeight = cellHeight / (2 * cellWidth);
					}
					letterSpacing += (1 - scaleFont) * fontSize / 2;
					fontSize *= scaleFont;
					lineHeight /= scaleFont;
					double pageHeight = maxRows * cellHeight; // mm
					double pageWidth = maxColumns * cellWidth; // mm
					LinkedList<QName> elementStack = new LinkedList<>();
					LinkedList<Boolean> duplexStack = new LinkedList<>();
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
											"@font-face {\n" +
											"	font-family: NotCourierSans;\n" +
											"	src: url(data:font/truetype;charset=utf8;base64,"
											         + Base64.getEncoder().encodeToString(ByteStreams.toByteArray(fallbackFont.openStream())) + ")\n" +
											"	     format(\"truetype\");\n" +
											"}\n" +
											"@page {\n" +
											"	margin: 0;\n" +
											"}\n" +
											"body {\n" +
											"	font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\n" +
											"	color: " + fontColor + ";\n" +
											"	margin: 0;\n" +
											"	font-size: " + fontSize + "mm;\n" +
											"}\n" +
											".volume, .page {\n" +
											"	page-break-before: always;\n" +
											"}\n" +
											".page {\n" +
											// Margins are expected to be specified in @page rules, and therefore included in
											// the PEF, so don't add any if we want the braille and print versions to match.
											// We do however support tweaking the position of the content by providing X/Y offset
											// options.
											"	margin-left: " + offsetX + "mm;\n" +
											"	margin-top: " + offsetY + "mm;\n" +
											"	height: " + (pageHeight - offsetY) + "mm;\n" +
											"	width: " + (pageWidth - offsetX) + "mm;\n" +
											"	overflow: hidden;\n" +
											"}\n" +
											".row {\n" +
											"    font-family: odt2braille, NotCourierSans;\n" +
											"    white-space: pre;\n" +
											"    letter-spacing: " + letterSpacing + "mm;\n" +
											"    height: " + lineHeight + "em;\n" +
											"}\n" +
											".row[rowgap=\"1\"] {\n" +
											"    height: " + (1.25 * lineHeight) + "em;\n" +
											"}\n" +
											".row[rowgap=\"2\"] {\n" +
											"    height: " + (1.5 * lineHeight) + "em;\n" +
											"}\n" +
											".row[rowgap=\"3\"] {\n" +
											"    height: " + (1.75 * lineHeight) + "em;\n" +
											"}\n" +
											".row[rowgap=\"4\"] {\n" +
											"    height: " + (2 * lineHeight) + "em;\n" +
											"}\n" +
											"		/*]]>*/\n" +
											"		</style>\n" +
											"		<bookmarks>\n");
										for (int v = 1; v <= totalVolumes; v++)
											html.write(
												"			<bookmark name=\"Volume " + v + "\" href=\"#volume-" + v + "\"/>\n");
										html.write(
											"		</bookmarks>\n" +
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
											rowgapStack.push(getRowgap(pef, rowgapStack));
											elementStack.push(elemName);
											volumeCount++;
											html.write("		<div class=\"volume\" id=\"volume-" + volumeCount + "\">\n");
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
					try (OutputStream os = new FileOutputStream(pdf)) {
						new PdfRendererBuilder()
							.withHtmlContent(new String(htmlBytes.toByteArray(), StandardCharsets.UTF_8),
							                 pef.getBaseURI().toString())
							.useDefaultPageSize((int)Math.ceil(pageWidth),
							                    // for some reason a small extra of 0,5% is
							                    // needed to fit all the rows on the page
							                    (int)Math.ceil(pageHeight * 1.005),
							                    PageSizeUnits.MM)
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

		public int getRowgap(XMLStreamReader pef, LinkedList<Integer> stack) {
			for (int i = 0; i < pef.getAttributeCount(); i++)
				if (_ROWGAP.equals(pef.getAttributeName(i)))
					return Integer.parseInt(pef.getAttributeValue(i)); // assume non-negative integer
			return stack.getFirst(); // throws NoSuchElementException if stack is empty, which can not
			                         // happen if PEF is valid
		}
	}

	private static double parseNumberOrPercentage(String value) throws IllegalArgumentException {
		boolean isPercentage = false;
		if (value.endsWith("%")) {
			value = value.substring(0, value.length() - 1);
			isPercentage = true;
		}
		try {
			double number = Double.valueOf(value);
			return isPercentage ? number / 100 : number;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Can not parse percentage: " + value, e);
		}
	}
}
