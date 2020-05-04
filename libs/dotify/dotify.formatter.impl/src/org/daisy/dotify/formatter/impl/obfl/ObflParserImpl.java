package org.daisy.dotify.formatter.impl.obfl;

import org.daisy.dotify.api.formatter.BlockBuilder;
import org.daisy.dotify.api.formatter.BlockContentBuilder;
import org.daisy.dotify.api.formatter.BlockPosition.VerticalAlignment;
import org.daisy.dotify.api.formatter.BlockProperties;
import org.daisy.dotify.api.formatter.CompoundField;
import org.daisy.dotify.api.formatter.ContentCollection;
import org.daisy.dotify.api.formatter.CurrentPageField;
import org.daisy.dotify.api.formatter.DynamicSequenceBuilder;
import org.daisy.dotify.api.formatter.Field;
import org.daisy.dotify.api.formatter.FieldList;
import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.FormattingTypes;
import org.daisy.dotify.api.formatter.ItemSequenceProperties;
import org.daisy.dotify.api.formatter.LayoutMasterBuilder;
import org.daisy.dotify.api.formatter.LayoutMasterProperties;
import org.daisy.dotify.api.formatter.Leader;
import org.daisy.dotify.api.formatter.MarginRegion;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.formatter.MarkerIndicatorRegion;
import org.daisy.dotify.api.formatter.MarkerReference.MarkerSearchDirection;
import org.daisy.dotify.api.formatter.MarkerReference.MarkerSearchScope;
import org.daisy.dotify.api.formatter.MarkerReferenceField;
import org.daisy.dotify.api.formatter.NoField;
import org.daisy.dotify.api.formatter.NumeralStyle;
import org.daisy.dotify.api.formatter.PageAreaBuilder;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.api.formatter.PageTemplateBuilder;
import org.daisy.dotify.api.formatter.Position;
import org.daisy.dotify.api.formatter.ReferenceListBuilder;
import org.daisy.dotify.api.formatter.RenameFallbackRule;
import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.api.formatter.SequenceProperties;
import org.daisy.dotify.api.formatter.SpanProperties;
import org.daisy.dotify.api.formatter.StringField;
import org.daisy.dotify.api.formatter.TableCellProperties;
import org.daisy.dotify.api.formatter.TableOfContents;
import org.daisy.dotify.api.formatter.TableProperties;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.formatter.TocEntryOnResumedRange;
import org.daisy.dotify.api.formatter.TocProperties;
import org.daisy.dotify.api.formatter.TransitionBuilder;
import org.daisy.dotify.api.formatter.TransitionBuilderProperties;
import org.daisy.dotify.api.formatter.TransitionBuilderProperties.ApplicationRange;
import org.daisy.dotify.api.formatter.VolumeContentBuilder;
import org.daisy.dotify.api.formatter.VolumeTemplateBuilder;
import org.daisy.dotify.api.formatter.VolumeTemplateProperties;
import org.daisy.dotify.api.obfl.ObflParser;
import org.daisy.dotify.api.obfl.ObflParserException;
import org.daisy.dotify.api.translator.Border;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.common.text.FilterLocale;
import org.daisy.dotify.formatter.impl.common.FactoryManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * <p>Provides a {@link ObflParser parser for OBFL}.</p>
 *
 * <p>The parser accepts OBFL input as an {@link XMLEventReader}. Based on the OBFL it populates the
 * supplied (empty) {@link Formatter} object.</p>
 *
 * @author Joel Håkansson
 */
public class ObflParserImpl extends XMLParserBase implements ObflParser {

    /*
     * List<MetaDataItem> objects are not only used to pass meta elements from OBFL to PEF but also
     * to pass along namespace binding information to make it easier for PEFMediaWriter to declare
     * namespace prefixes on the root element.
     */
    private List<MetaDataItem> meta;

    private Formatter formatter;
    private FilterLocale locale;
    private String mode;
    private boolean hyphGlobal;
    private final Logger logger;
    private final FactoryManager fm;
    private boolean normalizeSpace = true;

    Map<String, Node> xslts = new HashMap<>();
    Map<String, Node> fileRefs = new HashMap<>();
    Map<String, List<RendererInfo>> renderers = new HashMap<>();
    private Map<QName, String> externalReferenceObject = null;

    /**
     * Creates a new obfl parser with the specified factory manager.
     *
     * @param fm the factory manager
     */
    public ObflParserImpl(FactoryManager fm) {
        this.fm = fm;
        this.logger = Logger.getLogger(this.getClass().getCanonicalName());
    }

    public void setNormalizeSpace(boolean value) {
        this.normalizeSpace = value;
    }

    public boolean isNormalizingSpace() {
        return normalizeSpace;
    }

    @Override
    public void parse(XMLEventReader inputER, Formatter formatter) throws ObflParserException {
        this.formatter = formatter;
        FormatterConfiguration config = formatter.getConfiguration();
        this.locale = FilterLocale.parse(config.getLocale());
        this.mode = config.getTranslationMode();
        this.hyphGlobal = config.isHyphenating();
        this.meta = new ArrayList<>();
        XMLEvent event;
        TextProperties tp = new TextProperties.Builder(
            this.locale.toString()
        )
            .translationMode(mode)
            .hyphenate(hyphGlobal)
            .build();
        XMLEventIterator input;
        if (normalizeSpace) {
            input = new OBFLWsNormalizer(inputER, fm.getXmlEventFactory());
        } else {
            input = new XMLEventReaderAdapter(inputER);
        }
        try {
            while (input.hasNext()) {
                event = input.nextEvent();
                if (equalsStart(event, ObflQName.OBFL)) {
                    String loc = getAttr(event, ObflQName.ATTR_XML_LANG);
                    if (loc == null) {
                        throw new ObflParserException("Missing xml:lang on root element");
                    }
                    tp = getTextProperties(event, tp);
                } else if (equalsStart(event, ObflQName.META)) {
                    parseMeta(event, input);
                } else if (equalsStart(event, ObflQName.LAYOUT_MASTER)) {
                    parseLayoutMaster(event, input);
                } else if (equalsStart(event, ObflQName.SEQUENCE)) {
                    parseSequence(event, input, tp);
                } else if (equalsStart(event, ObflQName.TABLE_OF_CONTENTS)) {
                    parseTableOfContents(event, input, tp);
                } else if (equalsStart(event, ObflQName.VOLUME_TEMPLATE)) {
                    parseVolumeTemplate(event, input, tp);
                } else if (equalsStart(event, ObflQName.VOLUME_TRANSITION)) {
                    parseVolumeTransition(event, input, tp);
                } else if (equalsStart(event, ObflQName.COLLECTION)) {
                    parseCollection(event, input, tp);
                } else if (equalsStart(event, ObflQName.FILE_REFERENCE)) {
                    parseFileReference(event, input, fileRefs);
                } else if (equalsStart(event, ObflQName.XML_PROCESSOR)) {
                    parseProcessor(event, input, xslts);
                } else if (equalsStart(event, ObflQName.RENDERER)) {
                    parseRenderer(event, input, renderers);
                } else {
                    report(event);
                }
            }
            input.close();
        } catch (XMLStreamException e) {
            throw new ObflParserException(e);
        }
    }

    private void parseMeta(XMLEvent event, XMLEventIterator input) throws XMLStreamException {
        int level = 0;
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                level++;
                if (level == 1) {
                    StringBuilder sb = new StringBuilder();
                    QName name = event.asStartElement().getName();
                    while (input.hasNext()) {
                        event = input.nextEvent();
                        if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
                            level++;
                            warning(event, "Nested meta data not supported.");
                        } else if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
                            level--;
                        } else if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
                            sb.append(event.asCharacters().getData());
                        } else {
                            report(event);
                        }
                        if (level < 2) {
                            break;
                        }
                    }
                    meta.add(new MetaDataItem(name, sb.toString()));
                } else {
                    warning(event, "Nested meta data not supported.");
                }
            } else if (equalsEnd(event, ObflQName.META)) {
                break;
            } else if (event.getEventType() == XMLStreamConstants.END_ELEMENT) {
                level--;
            } else {
                report(event);
            }
        }
    }

    static void report(XMLEvent event) {
        if (event.isEndElement() || event.getEventType() == XMLEvent.COMMENT) {
            // ok
        } else if (event.isStartElement()) {
            String msg = "Unsupported context for element: " +
                event.asStartElement().getName() +
                buildLocationMsg(event.getLocation());
            //throw new UnsupportedOperationException(msg);
            Logger.getLogger(ObflParserImpl.class.getCanonicalName()).warning(msg);
        } else if (event.isStartDocument() || event.isEndDocument()) {
            // ok
        } else {
            Logger.getLogger(ObflParserImpl.class.getCanonicalName()).warning(event.toString());
        }
    }

    private void warning(XMLEvent event, String msg) {
        Logger.getLogger(this.getClass().getCanonicalName()).warning(msg + buildLocationMsg(event.getLocation()));
    }

    private static String buildLocationMsg(Location location) {
        int line = -1;
        int col = -1;
        if (location != null) {
            line = location.getLineNumber();
            col = location.getColumnNumber();
        }
        return (line > -1 ? " (at line: " + line + (col > -1 ? ", column: " + col : "") + ") " : "");
    }

    private void parseLayoutMaster(XMLEvent event, XMLEventIterator input) throws XMLStreamException {
        // TODO: This check can be removed after the concept of an alternative variable name
        // has been removed from the OBFL specification.
        // See https://github.com/mtmse/obfl/issues/13
        if (getAttr(event, "page-number-variable") != null) {
            throw new UnsupportedOperationException("Alternative variable names are not supported");
        }
        @SuppressWarnings("unchecked")
        Iterator<Attribute> i = event.asStartElement().getAttributes();
        int width = Integer.parseInt(getAttr(event, ObflQName.ATTR_PAGE_WIDTH));
        int height = Integer.parseInt(getAttr(event, ObflQName.ATTR_PAGE_HEIGHT));
        String masterName = getAttr(event, ObflQName.ATTR_NAME);
        //LayoutMasterImpl.Builder masterConfig = new LayoutMasterImpl.Builder(width, height, ef);
        LayoutMasterProperties.Builder masterConfig = new LayoutMasterProperties.Builder(width, height);
        HashMap<String, Object> border = new HashMap<>();
        while (i.hasNext()) {
            Attribute atts = i.next();
            String name = atts.getName().getLocalPart();
            String value = atts.getValue();
            if ("inner-margin".equals(name)) {
                masterConfig.innerMargin(Integer.parseInt(value));
            } else if ("outer-margin".equals(name)) {
                masterConfig.outerMargin(Integer.parseInt(value));
            } else if ("row-spacing".equals(name)) {
                masterConfig.rowSpacing(Float.parseFloat(value));
            } else if ("duplex".equals(name)) {
                masterConfig.duplex("true".equals(value));
            } else if (name.startsWith("border")) {
                border.put(name, value);
            }
        }
        if (!border.isEmpty()) {
            border.put(TextBorderFactory.FEATURE_MODE, mode);
            try {
                masterConfig.border(fm.getTextBorderFactory().newTextBorderStyle(border));
            } catch (TextBorderConfigurationException e) {
                Logger.getLogger(
                    this.getClass().getCanonicalName()
                ).log(
                    Level.WARNING,
                    "Failed to add border to block properties: " + border,
                    e
                );
            }
        }
        LayoutMasterBuilder master = formatter.newLayoutMaster(masterName, masterConfig.build());
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.TEMPLATE, ObflQName.DEFAULT_TEMPLATE)) {
                parseTemplate(master, event, input);
            } else if (equalsStart(event, ObflQName.PAGE_AREA)) {
                parsePageArea(master, event, input);
            } else if (equalsEnd(event, ObflQName.LAYOUT_MASTER)) {
                break;
            } else {
                report(event);
            }
        }
        //masters.put(masterName, masterConfig.build());
    }

    private void parsePageArea(
        LayoutMasterBuilder master,
        XMLEvent event,
        XMLEventIterator input
    ) throws XMLStreamException {
        String collection = getAttr(event, ObflQName.ATTR_COLLECTION);
        int maxHeight = Integer.parseInt(getAttr(event, ObflQName.ATTR_MAX_HEIGHT));
        PageAreaProperties.Builder config = new PageAreaProperties.Builder(collection, maxHeight);
        @SuppressWarnings("unchecked")
        Iterator<Attribute> i = event.asStartElement().getAttributes();
        while (i.hasNext()) {
            Attribute atts = i.next();
            String name = atts.getName().getLocalPart();
            String value = atts.getValue();
            if ("align".equals(name)) {
                config.align(PageAreaProperties.Alignment.valueOf(value.toUpperCase()));
            }
        }
        PageAreaBuilder builder = null;
        // Use global values here, because they are not inherited from anywhere
        TextProperties tp = new TextProperties.Builder(
            locale.toString()
        )
            .translationMode(mode)
            .hyphenate(hyphGlobal)
            .build();
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.FALLBACK)) {
                parseFallback(event, input, config);
            } else if (equalsStart(event, ObflQName.BEFORE)) {
                if (builder == null) {
                    builder = master.setPageArea(config.build());
                }
                parseBeforeAfter(event, input, builder.getBeforeArea(), tp);
            } else if (equalsStart(event, ObflQName.AFTER)) {
                if (builder == null) {
                    builder = master.setPageArea(config.build());
                }
                parseBeforeAfter(event, input, builder.getAfterArea(), tp);
            } else if (equalsEnd(event, ObflQName.PAGE_AREA)) {
                if (builder == null) {
                    builder = master.setPageArea(config.build());
                }
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseFallback(
        XMLEvent event,
        XMLEventIterator input,
        PageAreaProperties.Builder pap
    ) throws XMLStreamException {
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.RENAME)) {
                parseRename(event, input, pap);
            } else if (equalsEnd(event, ObflQName.FALLBACK)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseRename(
        XMLEvent event,
        XMLEventIterator input,
        PageAreaProperties.Builder pap
    ) throws XMLStreamException {
        String from = getAttr(event, "collection");
        String to = getAttr(event, "to");
        pap.addFallback(new RenameFallbackRule(from, to));
        scanEmptyElement(input, ObflQName.RENAME);
    }

    private void parseBeforeAfter(
        XMLEvent event,
        XMLEventIterator input,
        FormatterCore fc,
        TextProperties tp
    ) throws XMLStreamException {
        tp = getTextProperties(event, tp);
        fc.startBlock(blockBuilder(event.asStartElement()));
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                fc.addChars(event.asCharacters().getData(), tp);
            } else if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, fc, tp);
            } else if (processAsBlockContents(fc, event, input, tp, false)) {
                //done!
            } else if (equalsEnd(event, ObflQName.BEFORE, ObflQName.AFTER)) {
                fc.endBlock();
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTemplate(
        LayoutMasterBuilder master,
        XMLEvent event,
        XMLEventIterator input
    ) throws XMLStreamException {
        PageTemplateBuilder template;
        if (equalsStart(event, ObflQName.TEMPLATE)) {
            template = master.newTemplate(
                new OBFLCondition(
                    getAttr(event, ObflQName.ATTR_USE_WHEN),
                    fm.getExpressionFactory(),
                    OBFLVariable.PAGE_NUMBER,
                    OBFLVariable.VOLUME_NUMBER,
                    OBFLVariable.VOLUME_COUNT,
                    OBFLVariable.SHEET_COUNT,
                    OBFLVariable.VOLUME_SHEET_COUNT
                )
            );
        } else {
            template = master.newTemplate(null);
        }
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.HEADER)) {
                FieldList fields = parseHeaderFooter(event, input);
                if (fields != null) {
                    template.addToHeader(fields);
                }
            } else if (equalsStart(event, ObflQName.FOOTER)) {
                FieldList fields = parseHeaderFooter(event, input);
                if (fields != null) {
                    template.addToFooter(fields);
                }
            } else if (equalsStart(event, ObflQName.MARGIN_REGION)) {
                String align = getAttr(event, ObflQName.ATTR_ALIGN);
                MarginRegion region = parseMarginRegion(event, input);
                if ("right".equals(align.toLowerCase())) {
                    template.addToRightMargin(region);
                } else {
                    template.addToLeftMargin(region);
                }
            } else if (equalsEnd(event, ObflQName.TEMPLATE) || equalsEnd(event, ObflQName.DEFAULT_TEMPLATE)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private FieldList parseHeaderFooter(XMLEvent event, XMLEventIterator input) throws XMLStreamException {
        @SuppressWarnings("unchecked")
        Iterator<Attribute> i = event.asStartElement().getAttributes();
        Float rowSpacing = null;
        while (i.hasNext()) {
            Attribute atts = i.next();
            String name = atts.getName().getLocalPart();
            String value = atts.getValue();
            if ("row-spacing".equals(name)) {
                rowSpacing = Float.parseFloat(value);
            }
        }
        ArrayList<Field> fields = new ArrayList<>();
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.FIELD)) {
                String textStyle = getAttr(event, ObflQName.ATTR_TEXT_STYLE);
                String allowTextFlow = getAttr(event, ObflQName.ATTR_ALLOW_TEXT_FLOW);
                ArrayList<Field> compound = parseField(event, input);
                if ("true".equals(allowTextFlow)) {
                    if (!compound.isEmpty()) {
                        throw new RuntimeException("No content supported in " + ObflQName.FIELD
                                + " element when " + ObflQName.ATTR_ALLOW_TEXT_FLOW + " is 'true'");
                    }
                    compound.add(NoField.getInstance());
                }
                if (compound.size() == 1) {
                    fields.add(compound.get(0));
                } else {
                    CompoundField f = new CompoundField(textStyle);
                    f.addAll(compound);
                    fields.add(f);
                }
            } else if (equalsEnd(event, ObflQName.HEADER) || equalsEnd(event, ObflQName.FOOTER)) {
                break;
            } else {
                report(event);
            }
        }
        if (!fields.isEmpty()) {
            return new FieldList.Builder(fields).rowSpacing(rowSpacing).build();
        } else {
            return null;
        }
    }

    private ArrayList<Field> parseField(XMLEvent event, XMLEventIterator input) throws XMLStreamException {
        ArrayList<Field> compound = new ArrayList<>();
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.STRING)) {
                compound.add(new StringField(getAttr(event, "value"), getAttr(event, ObflQName.ATTR_TEXT_STYLE)));
            } else if (equalsStart(event, ObflQName.EVALUATE)) {
                //TODO: add variables...
                compound.add(
                    new StringField(
                        fm.getExpressionFactory().newExpression().evaluate(
                                getAttr(event, "expression")
                        ),
                        getAttr(event, ObflQName.ATTR_TEXT_STYLE)
                    )
                );
            } else if (equalsStart(event, ObflQName.CURRENT_PAGE)) {
                compound.add(new CurrentPageField(getNumeralStyle(event), getAttr(event, ObflQName.ATTR_TEXT_STYLE)));
            } else if (equalsStart(event, ObflQName.MARKER_REFERENCE)) {
                compound.add(parseMarkerReferenceField(event, true));
            } else if (equalsEnd(event, ObflQName.FIELD)) {
                break;
            } else {
                report(event);
            }
        }
        return compound;
    }

    private MarkerReferenceField parseMarkerReferenceField(
        XMLEvent event,
        boolean allowTextStyle
    ) throws XMLStreamException {
        String textStyle = getAttr(event, ObflQName.ATTR_TEXT_STYLE);
        if (!allowTextStyle && textStyle != null) {
            logger.log(Level.WARNING, "marker-reference can only have text-style attribute in field context");
        }
        return new MarkerReferenceField(
            getAttr(event, ObflQName.ATTR_MARKER),
            MarkerSearchDirection.valueOf(
                    getAttr(event, "direction").toUpperCase()
            ),
            MarkerSearchScope.valueOf(
                    getAttr(event, "scope").replace('-', '_').toUpperCase()
            ),
            allowTextStyle ? textStyle : null,
            toInt(getAttr(event, ObflQName.ATTR_START_OFFSET), 0)
        );
    }

    private MarginRegion parseMarginRegion(XMLEvent event, XMLEventIterator input) throws XMLStreamException {
        int width = 1;
        String value = getAttr(event, ObflQName.ATTR_WIDTH);
        try {
            if (value != null) {
                width = Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            warning(event, "Failed to parse integer: " + value);
        }
        MarginRegion ret = null;
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.INDICATORS)) {
                //this element is optional, it can only occur once
                ret = parseIndicatorRegion(event, input, width);
            } else if (equalsEnd(event, ObflQName.MARGIN_REGION)) {
                break;
            } else {
                report(event);
            }
        }
        if (ret == null) {
            ret = MarkerIndicatorRegion.ofWidth(width).build();
        }
        return ret;
    }

    private MarginRegion parseIndicatorRegion(
        XMLEvent event,
        XMLEventIterator input,
        int width
    ) throws XMLStreamException {
        MarkerIndicatorRegion.Builder builder = MarkerIndicatorRegion.ofWidth(width);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.MARKER_INDICATOR)) {
                String markers = getAttr(event, "markers");
                String indicator = getAttr(event, "indicator");
                if (markers == null || "".equals(markers)) {
                    warning(event, "@markers missing / has no value");
                } else {
                    if (indicator == null || "".equals(indicator)) {
                        warning(event, "@indicator missing / has no value");
                    } else {
                        String[] names = markers.split("\\s+");
                        for (String name : names) {
                            if (!"".equals(name)) {
                                builder.addIndicator(name, indicator);
                            }
                        }
                    }
                }
                scanEmptyElement(input, ObflQName.MARKER_INDICATOR);
            } else if (equalsEnd(event, ObflQName.INDICATORS)) {
                break;
            } else {
                report(event);
            }
        }
        return builder.build();
    }

    private int toInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    private void parseSequence(XMLEvent event, XMLEventIterator input, TextProperties tp) throws XMLStreamException {
        String masterName = getAttr(event, "master");
        tp = getTextProperties(event, tp);
        SequenceProperties.Builder builder = new SequenceProperties.Builder(masterName);
        String initialPageNumber = getAttr(event, ObflQName.ATTR_INITIAL_PAGE_NUMBER);
        if (initialPageNumber != null) {
            builder.initialPageNumber(Integer.parseInt(initialPageNumber));
        }
        String breakBefore = getAttr(event, "break-before");
        if (breakBefore != null) {
            builder.breakBefore(SequenceProperties.SequenceBreakBefore.valueOf(breakBefore.toUpperCase()));
        }
        String pageNumberCounter = getAttr(event, "page-number-counter");
        if (pageNumberCounter != null) {
            builder.pageCounterName(pageNumberCounter);
        }
        FormatterCore seq = formatter.newSequence(builder.build());
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, seq, tp);
            } else if (equalsStart(event, ObflQName.TABLE)) {
                parseTable(event, input, seq, tp);
            } else if (equalsStart(event, ObflQName.XML_DATA)) {
                parseXMLData(seq, event, input, tp);
            } else if (equalsEnd(event, ObflQName.SEQUENCE)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseXMLData(
        FormatterCore fc,
        XMLEvent event,
        XMLEventIterator input,
        TextProperties tp
    ) throws XMLStreamException {
        String renderer = getAttr(event, "renderer");
        DOMResult dr;
        try {
            Document d = fm.getDocumentBuilderFactory().newDocumentBuilder().newDocument();
            dr = new DOMResult(d);
            XMLEventWriter ew = fm.getXmlOutputFactory().createXMLEventWriter(dr);
            while (input.hasNext()) {
                event = input.nextEvent();
                if (equalsEnd(event, ObflQName.XML_DATA)) {
                    break;
                } else {
                    ew.add(event);
                }
            }
            ew.close();

            XMLDataRenderer qtd = filterRenderers(renderers.get(renderer), d, tp);
            fc.insertDynamicLayout(qtd);
        } catch (ParserConfigurationException | TransformerFactoryConfigurationError | FactoryConfigurationError e) {
            logger.log(Level.WARNING, "Failed to parse xml-data element.", e);
        }
    }

    /**
     * Filters the scenarios to only return those that apply to this data set.
     *
     * @param tdl
     * @param node
     * @param tp
     * @return returns the applicable scenarios
     * @throws ParserConfigurationException
     */
    private XMLDataRenderer filterRenderers(
        List<RendererInfo> tdl,
        Node node,
        TextProperties tp
    ) throws ParserConfigurationException {
        List<RenderingScenario> qtd = new ArrayList<>();
        {
            XPath x = fm.getXpathFactory().newXPath();
            for (RendererInfo td : tdl) {
                if (td.getQualifier() != null) {
                    x.setNamespaceContext(td.getNamespaceContext());
                    try {
                        if ((Boolean) x.evaluate(td.getQualifier(), node, XPathConstants.BOOLEAN)) {
                            qtd.add(
                                new XSLTRenderingScenario(
                                    this,
                                    configureTransformer(td),
                                    node,
                                    tp,
                                    fm.getExpressionFactory().newExpression(),
                                    td.getCost()
                                )
                            );
                        }
                    } catch (XPathExpressionException e) {
                        logger.log(Level.WARNING, "Failed to evaluate xpath expression.", e);
                    }
                } else {
                    qtd.add(
                        new XSLTRenderingScenario(
                            this,
                            configureTransformer(td),
                            node,
                            tp,
                            fm.getExpressionFactory().newExpression(),
                            td.getCost()
                        )
                    );
                }
            }
        }
        return new XMLDataRenderer(qtd);
    }

    private Transformer configureTransformer(RendererInfo n) {
        try {
            TransformerFactory tf = fm.getTransformerFactory();
            tf.setURIResolver(new URIResolver() {
                @Override
                public Source resolve(String href, String base) throws TransformerException {
                    if ("".equals(base)) {
                        Node d = fileRefs.get(href);
                        if (d != null) {
                            return new DOMSource(d);
                        }
                    }
                    return null;
                }
            });
            Transformer ret = tf.newTransformer(new DOMSource(n.getProcessor()));
            for (String name : n.getParams().keySet()) {
                ret.setParameter(name, n.getParams().get(name));
            }
            return ret;
        } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
            //TODO: throw what?
            throw new RuntimeException(e);
        }
    }

    private void parseFileReference(XMLEvent event, XMLEventIterator input, Map<String, Node> refs) {
        String uri = getAttr(event, ObflQName.ATTR_URI);
        DOMResult dr;
        try {
            Document d = fm.getDocumentBuilderFactory().newDocumentBuilder().newDocument();
            dr = new DOMResult(d);
            XMLEventWriter ew = fm.getXmlOutputFactory().createXMLEventWriter(dr);
            while (input.hasNext()) {
                event = input.nextEvent();
                if (equalsEnd(event, ObflQName.FILE_REFERENCE)) {
                    break;
                } else if (event.getEventType() == XMLEvent.COMMENT) {
                    //ignore
                } else {
                    ew.add(event);
                }
            }
            refs.put(uri, dr.getNode());
        } catch (ParserConfigurationException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void parseProcessor(XMLEvent event, XMLEventIterator input, Map<String, Node> xslts) {
        String name = getAttr(event, ObflQName.ATTR_NAME);
        DOMResult dr;
        try {
            Document d = fm.getDocumentBuilderFactory().newDocumentBuilder().newDocument();
            dr = new DOMResult(d);
            XMLEventWriter ew = fm.getXmlOutputFactory().createXMLEventWriter(dr);
            while (input.hasNext()) {
                event = input.nextEvent();
                if (equalsEnd(event, ObflQName.XML_PROCESSOR)) {
                    break;
                } else if (event.getEventType() == XMLEvent.COMMENT) {
                    //ignore
                } else {
                    ew.add(event);
                }
            }
            xslts.put(name, dr.getNode());
        } catch (ParserConfigurationException | XMLStreamException e) {
            //TODO: throw what?
            throw new RuntimeException(e);
        }
    }

    private void parseRenderer(
        XMLEvent event,
        XMLEventIterator input,
        Map<String,
        List<RendererInfo>> renderers
    ) throws XMLStreamException {
        String name = getAttr(event, ObflQName.ATTR_NAME);
        List<RendererInfo> opts = new ArrayList<>();
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.RENDERING_SCENARIO)) {
                opts.add(parseRenderingScenario(event, input));
            } else if (equalsEnd(event, ObflQName.RENDERER)) {
                break;
            } else {
                report(event);
            }
        }
        renderers.put(name, opts);
    }

    private RendererInfo parseRenderingScenario(XMLEvent event, XMLEventIterator input) throws XMLStreamException {
        NamespaceContext nc = event.asStartElement().getNamespaceContext();
        String processor = getAttr(event, ObflQName.ATTR_PROCESSOR);
        String qualifier = getAttr(event, ObflQName.ATTR_QUALIFIER);
        String cost = getAttr(event, ObflQName.ATTR_COST);
        RendererInfo.Builder builder = new RendererInfo.Builder(xslts.get(processor), nc, qualifier, cost);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.PARAMETER)) {
                String name = getAttr(event, ObflQName.ATTR_NAME);
                String value = getAttr(event, ObflQName.ATTR_VALUE);
                builder.addParameter(name, value);
                scanEmptyElement(input, ObflQName.PARAMETER);
            } else if (equalsEnd(event, ObflQName.RENDERING_SCENARIO)) {
                break;
            } else {
                report(event);
            }
        }
        return builder.build();
    }

    void parseBlock(
        XMLEvent event,
        XMLEventIterator input,
        FormatterCore fc,
        TextProperties tp
    ) throws XMLStreamException {
        externalReferenceObject = null;

        tp = getTextProperties(event, tp);
        fc.startBlock(blockBuilder(event.asStartElement()));
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                fc.addChars(event.asCharacters().getData(), tp);
            } else if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, fc, tp);
            } else if (equalsStart(event, ObflQName.XML_DATA)) {
                parseXMLData(fc, event, input, tp);
            } else if (equalsStart(event, ObflQName.TABLE)) {
                parseTable(event, input, fc, tp);
            } else if (processAsBlockContents(fc, event, input, tp, false)) {
                //done
            } else if (equalsEnd(event, ObflQName.BLOCK)) {
                fc.endBlock();
                break;
            } else {
                report(event);
            }
        }
    }

    void parseBlock(
        XMLEvent event,
        XMLEventIterator input,
        BlockBuilder fc,
        TextProperties tp
    ) throws XMLStreamException {
        tp = getTextProperties(event, tp);
        fc.startBlock(blockBuilder(event.asStartElement()));
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                fc.addChars(event.asCharacters().getData(), tp);
            } else if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, fc, tp);
            } else if (processAsBlockContents(fc, event, input, tp, false)) {
                //done
            } else if (equalsEnd(event, ObflQName.BLOCK)) {
                fc.endBlock();
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseSpan(
        XMLEvent event,
        XMLEventIterator input,
        BlockContentBuilder fc,
        TextProperties tp,
        boolean inTocEntryOnResumed
    ) throws XMLStreamException {
        tp = getTextProperties(event, tp);
        String id = getAttr(event, ObflQName.ATTR_ID);
        SpanProperties.Builder propsBuilder = new SpanProperties.Builder();
        if (id != null && !"".equals(id)) {
            propsBuilder.identifier(id);
        }
        fc.startSpan(propsBuilder.build());
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                fc.addChars(event.asCharacters().getData(), tp);
            } else if (equalsStart(event, ObflQName.STYLE)) {
                parseStyle(event, input, fc, tp, inTocEntryOnResumed);
            } else if (equalsStart(event, ObflQName.LEADER)) {
                parseLeader(fc, event, input);
            } else if (equalsStart(event, ObflQName.MARKER)) {
                parseMarker(fc, event);
            } else if (equalsStart(event, ObflQName.BR)) {
                fc.newLine();
                scanEmptyElement(input, ObflQName.BR);
            } else if (equalsStart(event, ObflQName.ANCHOR)) {
                fc.insertAnchor(parseAnchor(event));
            } else if (equalsEnd(event, ObflQName.SPAN)) {
                fc.endSpan();
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseStyle(
        XMLEvent event,
        XMLEventIterator input,
        BlockContentBuilder fc,
        TextProperties tp,
        boolean inTocEntryOnResumed
    ) throws XMLStreamException {
        String name = getAttr(event, "name");
        boolean ignore = formatter.getConfiguration().getIgnoredStyles().contains(name);
        if (!ignore) {
            fc.startStyle(name);
        }
        boolean hasEvents = false;
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                fc.addChars(event.asCharacters().getData(), tp);
            } else if (equalsStart(event, ObflQName.STYLE)) {
                parseStyle(event, input, fc, tp, inTocEntryOnResumed);
            } else if (equalsStart(event, ObflQName.MARKER)) {
                parseMarker(fc, event);
            } else if (equalsStart(event, ObflQName.BR)) {
                fc.newLine();
                scanEmptyElement(input, ObflQName.BR);
            } else if (equalsStart(event, ObflQName.ANCHOR)) {
                fc.insertAnchor(parseAnchor(event));
            } else if (equalsStart(event, ObflQName.EVALUATE)) {
                parseEvaluate(fc, event, input, tp, inTocEntryOnResumed);
            } else if (equalsStart(event, ObflQName.PAGE_NUMBER)) {
                parsePageNumber(fc, event, input);
            } else if (equalsStart(event, ObflQName.MARKER_REFERENCE)) {
                parseMarkerReference(fc, event, input, tp);
            } else if (equalsEnd(event, ObflQName.STYLE)) {
                if (!ignore) {
                    if (!hasEvents) {
                        fc.addChars("", tp);
                    }
                    fc.endStyle();
                }
                break;
            } else {
                report(event);
            }
            hasEvents = true;
        }
    }

    private BlockProperties blockBuilder(StartElement el) {
        Iterator<?> atts = el.getAttributes();
        BlockProperties.Builder builder = new BlockProperties.Builder();
        HashMap<String, Object> border = new HashMap<>();
        String underlinePattern = null;
        HashMap<String, Object> underline = new HashMap<>();
        while (atts.hasNext()) {
            Attribute att = (Attribute) atts.next();
            String name = att.getName().getLocalPart();
            if ("margin-left".equals(name)) {
                builder.leftMargin(Integer.parseInt(att.getValue()));
            } else if ("margin-right".equals(name)) {
                builder.rightMargin(Integer.parseInt(att.getValue()));
            } else if ("margin-top".equals(name)) {
                builder.topMargin(Integer.parseInt(att.getValue()));
            } else if ("margin-bottom".equals(name)) {
                builder.bottomMargin(Integer.parseInt(att.getValue()));
            } else if ("padding-left".equals(name)) {
                builder.leftPadding(Integer.parseInt(att.getValue()));
            } else if ("padding-right".equals(name)) {
                builder.rightPadding(Integer.parseInt(att.getValue()));
            } else if ("padding-top".equals(name)) {
                builder.topPadding(Integer.parseInt(att.getValue()));
            } else if ("padding-bottom".equals(name)) {
                builder.bottomPadding(Integer.parseInt(att.getValue()));
            } else if ("text-indent".equals(name)) {
                builder.textIndent(Integer.parseInt(att.getValue()));
            } else if ("first-line-indent".equals(name)) {
                builder.firstLineIndent(Integer.parseInt(att.getValue()));
            } else if ("right-text-indent".equals(name)) {
                builder.rightTextIndent(Integer.parseInt(att.getValue()));
            } else if ("right-last-line-indent".equals(name)) {
                logger.warning("right-last-line-indent attribute is not supported, ignoring." + toLocation(el));
            } else if ("list-type".equals(name)) {
                builder.listType(FormattingTypes.ListStyle.valueOf(att.getValue().toUpperCase()));
            } else if ("list-style".equals(name)) {
                String typeStr = getAttr(el, "list-type");
                if (typeStr != null) {
                    FormattingTypes.ListStyle type = FormattingTypes.ListStyle.valueOf(typeStr.toUpperCase());
                    if (FormattingTypes.ListStyle.OL == type) {
                        try {
                            builder.listNumberFormat(ObflParserImpl.parseNumeralStyle(att.getValue()));
                        } catch (IllegalArgumentException e) {
                            logger.log(Level.WARNING, "Failed to parse as a number format: " + att.getValue(), e);
                        }
                    } else {
                        builder.defaultListLabel(att.getValue());
                    }
                } else {
                    logger.info("list-style has no effect, missing @list-type." + toLocation(el));
                }
            } else if ("list-item-label".equals(name)) {
                builder.listItemLabel(att.getValue());
            } else if ("break-before".equals(name)) {
                builder.breakBefore(FormattingTypes.BreakBefore.valueOf(att.getValue().toUpperCase()));
            } else if ("keep".equals(name)) {
                if (att.getValue().equalsIgnoreCase("all")) {
                    logger.warning(
                        "@keep=all has been deprecated since 2016, " +
                        "let's take a while to think about that. Use @keep=page"
                    );
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                builder.keep(FormattingTypes.Keep.valueOf(att.getValue().toUpperCase()));
            } else if ("orphans".equals(name)) {
                builder.orphans(Integer.parseInt(att.getValue()));
            } else if ("widows".equals(name)) {
                builder.widows(Integer.parseInt(att.getValue()));
            } else if ("keep-with-next".equals(name)) {
                builder.keepWithNext(Integer.parseInt(att.getValue()));
            } else if ("keep-with-previous-sheets".equals(name)) {
                builder.keepWithPreviousSheets(Integer.parseInt(att.getValue()));
            } else if ("keep-with-next-sheets".equals(name)) {
                builder.keepWithNextSheets(Integer.parseInt(att.getValue()));
            } else if ("volume-keep-priority".equals(name)) {
                builder.volumeKeepPriority(Integer.parseInt(att.getValue()));
            } else if ("block-indent".equals(name)) {
                builder.blockIndent(Integer.parseInt(att.getValue()));
            } else if ("id".equals(name)) {
                builder.identifier(att.getValue());
            } else if ("align".equals(name)) {
                builder.align(FormattingTypes.Alignment.valueOf(att.getValue().toUpperCase()));
            } else if ("vertical-position".equals(name)) {
                builder.verticalPosition(Position.parsePosition(att.getValue()));
            } else if ("vertical-align".equals(name)) {
                builder.verticalAlignment(VerticalAlignment.valueOf(att.getValue().toUpperCase()));
            } else if ("row-spacing".equals(name)) {
                builder.rowSpacing(Float.parseFloat(att.getValue()));
            } else if (name.startsWith("border")) {
                border.put(name, att.getValue());
            } else if ("underline-pattern".equals(name)) {
                if (!att.getValue().equalsIgnoreCase("none") && !att.getValue().isEmpty()) {
                    underlinePattern = att.getValue();
                }
            } else if (name.startsWith("underline-")) {
                underline.put(name.replaceAll("^underline", "border-bottom"), att.getValue());
            }
        }
        if (!border.isEmpty()) {
            border.put(TextBorderFactory.FEATURE_MODE, mode);
            try {
                builder.textBorderStyle(fm.getTextBorderFactory().newTextBorderStyle(border));
            } catch (TextBorderConfigurationException e) {
                logger.log(Level.WARNING, "Failed to add border to block properties: " + border, e);
            }
        }
        if (underlinePattern == null && !underline.isEmpty()) {
            underline.put(TextBorderFactory.FEATURE_MODE, mode);
            try {
                TextBorderStyle underlineStyle = fm.getTextBorderFactory().newTextBorderStyle(underline);
                if (underlineStyle != null) {
                    underlinePattern = underlineStyle.getBottomBorder();
                }
            } catch (TextBorderConfigurationException e) {
                // TODO: this will show border-bottom-* properties
                logger.log(Level.WARNING, "Failed to add underline to block properties: " + underline, e);
            }
        }
        if (underlinePattern != null && !underlinePattern.isEmpty()) {
            builder.underlineStyle(underlinePattern);
        }
        return builder.build();
    }

    private Border borderBuilder(Iterator<?> atts) {
        BorderBuilder builder = new BorderBuilder();
        while (atts.hasNext()) {
            Attribute att = (Attribute) atts.next();
            String name = att.getName().getLocalPart();
            if (name.startsWith("border")) {
                builder.put(name, att.getValue());
            }
        }
        return builder.build();
    }


    private void parseLeader(
        BlockContentBuilder fc,
        XMLEvent event,
        XMLEventIterator input
    ) throws XMLStreamException {
        Leader.Builder builder = new Leader.Builder();
        @SuppressWarnings("unchecked")
        Iterator<Attribute> atts = event.asStartElement().getAttributes();
        while (atts.hasNext()) {
            Attribute att = atts.next();
            String name = att.getName().getLocalPart();
            if ("align".equals(name)) {
                builder.align(Leader.Alignment.valueOf(att.getValue().toUpperCase()));
            } else if ("position".equals(name)) {
                builder.position(Position.parsePosition(att.getValue()));
            } else if ("pattern".equals(name)) {
                builder.pattern(att.getValue());
            } else {
                report(event);
            }
        }
        scanEmptyElement(input, ObflQName.LEADER);
        fc.insertLeader(builder.build());
    }

    private static void parseMarker(BlockContentBuilder fc, XMLEvent event) throws XMLStreamException {
        String markerName = getAttr(event, "class");
        String markerValue = getAttr(event, "value");
        fc.insertMarker(new Marker(markerName, markerValue));
    }

    private String parseAnchor(XMLEvent event) {
        return getAttr(event, "item");
    }

    void parseTable(
        XMLEvent event,
        XMLEventIterator input,
        FormatterCore fc,
        TextProperties tp
    ) throws XMLStreamException {
        int tableColSpacing = toInt(getAttr(event, ObflQName.ATTR_TABLE_COL_SPACING), 0);
        int tableRowSpacing = toInt(getAttr(event, ObflQName.ATTR_TABLE_ROW_SPACING), 0);
        int preferredEmptySpace = toInt(getAttr(event, ObflQName.ATTR_TABLE_PREFERRED_EMPTY_SPACE), 2);
        BlockProperties bp = blockBuilder(event.asStartElement());
        Border b = borderBuilder(event.asStartElement().getAttributes());
        TableProperties.Builder tableProps = new TableProperties.Builder()
                .tableColSpacing(tableColSpacing)
                .tableRowSpacing(tableRowSpacing)
                .preferredEmptySpace(preferredEmptySpace)
                .margin(bp.getMargin())
                .padding(bp.getPadding())
                .border(b);
        String rowSpacingStr = getAttr(event, "row-spacing");
        if (rowSpacingStr != null) {
            try {
                tableProps.rowSpacing(Float.parseFloat(rowSpacingStr));
            } catch (NumberFormatException e) {
            }
        }
        fc.startTable(tableProps.build());
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.THEAD)) {
                parseTHeadTBody(event, input, fc, tp);
            } else if (equalsStart(event, ObflQName.TBODY)) {
                parseTHeadTBody(event, input, fc, tp);
            } else if (equalsStart(event, ObflQName.TR)) {
                parseTR(event, input, fc, tp);
            } else if (equalsEnd(event, ObflQName.TABLE)) {
                fc.endTable();
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTHeadTBody(
        XMLEvent event,
        XMLEventIterator input,
        FormatterCore fc,
        TextProperties tp
    ) throws XMLStreamException {
        if (equalsStart(event, ObflQName.THEAD)) {
            fc.beginsTableHeader();
        } else {
            fc.beginsTableBody();
        }
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.TR)) {
                parseTR(event, input, fc, tp);
            } else if (equalsEnd(event, ObflQName.THEAD, ObflQName.TBODY)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTR(
        XMLEvent event,
        XMLEventIterator input,
        FormatterCore fc,
        TextProperties tp
    ) throws XMLStreamException {
        fc.beginsTableRow();
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.TD)) {
                parseTD(event, input, fc, tp);
            } else if (equalsEnd(event, ObflQName.TR)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTD(
        XMLEvent event,
        XMLEventIterator input,
        FormatterCore fs,
        TextProperties tp
    ) throws XMLStreamException {
        tp = getTextProperties(event, tp);
        int colSpan = toInt(getAttr(event, ObflQName.ATTR_COL_SPAN), 1);
        int rowSpan = toInt(getAttr(event, ObflQName.ATTR_ROW_SPAN), 1);
        BlockProperties bp = blockBuilder(event.asStartElement());
        Border b = borderBuilder(event.asStartElement().getAttributes());
        TableCellProperties tcp = new TableCellProperties.Builder()
                .colSpan(colSpan)
                .rowSpan(rowSpan)
                .padding(bp.getPadding())
                .textBlockProperties(bp.getTextBlockProperties())
                .border(b)
                .build();
        FormatterCore fc = fs.beginsTableCell(tcp);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                fc.addChars(event.asCharacters().getData(), tp);
            } else if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, fc, tp);
            } else if (processAsBlockContents(fc, event, input, tp, false)) {
                //done
            } else if (equalsEnd(event, ObflQName.TD)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTableOfContents(
        XMLEvent event,
        XMLEventIterator input,
        TextProperties tp
    ) throws XMLStreamException, ObflParserException {
        String tocName = getAttr(event, ObflQName.ATTR_NAME);
        tp = getTextProperties(event, tp);
        TableOfContents toc = formatter.newToc(tocName);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.TOC_BLOCK)) {
                parseTocBlock(event, input, toc, tp);
            } else if (equalsEnd(event, ObflQName.TABLE_OF_CONTENTS)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseCollection(
        XMLEvent event,
        XMLEventIterator input,
        TextProperties tp
    ) throws XMLStreamException {
        String id = getAttr(event, ObflQName.ATTR_NAME);
        ContentCollection coll = formatter.newCollection(id);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.ITEM)) {
                parseCollectionItem(event, input, coll, tp);
            } else if (equalsEnd(event, ObflQName.COLLECTION)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTocBlock(
        XMLEvent event,
        XMLEventIterator input,
        TableOfContents toc,
        TextProperties tp
    ) throws XMLStreamException, ObflParserException {
        toc.startBlock(blockBuilder(event.asStartElement()));
        tp = getTextProperties(event, tp);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.TOC_BLOCK)) {
                parseTocBlock(event, input, toc, tp);
            } else if (equalsStart(event, ObflQName.TOC_ENTRY)) {
                parseTocEntry(event, input, toc, tp);
            } else if (equalsStart(event, ObflQName.TOC_ENTRY_ON_RESUMED)) {
                parseTocEntryOnResumed(event, input, toc, tp);
            } else if (equalsEnd(event, ObflQName.TOC_BLOCK)) {
                toc.endBlock();
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTocEntry(
        XMLEvent event,
        XMLEventIterator input,
        TableOfContents toc,
        TextProperties tp
    ) throws XMLStreamException {
        String refId = getAttr(event, "ref-id");
        tp = getTextProperties(event, tp);
        toc.startEntry(refId);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                toc.addChars(event.asCharacters().getData(), tp);
            } else if (processAsBlockContents(toc, event, input, tp, false)) {
                //done!
            } else if (equalsEnd(event, ObflQName.TOC_ENTRY)) {
                toc.endEntry();
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTocEntryOnResumed(
        XMLEvent event,
        XMLEventIterator input,
        TableOfContents toc,
        TextProperties tp
    ) throws XMLStreamException, ObflParserException {
        TocEntryOnResumedRange range = new TocEntryOnResumedRange(getAttr(event, "range"));
        tp = getTextProperties(event, tp);
        toc.startEntryOnResumed(range);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                toc.addChars(event.asCharacters().getData(), tp);
            } else if (processAsBlockContents(toc, event, input, tp, true)) {
                //done!
            } else if (equalsEnd(event, ObflQName.TOC_ENTRY_ON_RESUMED)) {
                toc.endEntry();
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseCollectionItem(
        XMLEvent event,
        XMLEventIterator input,
        ContentCollection coll,
        TextProperties tp
    ) throws XMLStreamException {
        tp = getTextProperties(event, tp);
        coll.startItem(blockBuilder(event.asStartElement()));
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.isCharacters()) {
                coll.addChars(event.asCharacters().getData(), tp);
            } else if (equalsStart(event, ObflQName.ITEM)) {
                parseCollectionItem(event, input, coll, tp);
                Logger.getLogger(this.getClass().getCanonicalName()).warning("Nested collection items.");
            } else if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, coll, tp);
            } else if (processAsBlockContents(coll, event, input, tp, false)) {
                //done!
            } else if (equalsEnd(event, ObflQName.ITEM)) {
                coll.endItem();
                break;
            } else {
                report(event);
            }
        }
    }

    boolean processAsBlockContents(
        BlockContentBuilder fc,
        XMLEvent event,
        XMLEventIterator input,
        TextProperties tp,
        boolean inTocEntryOnResumed
    ) throws XMLStreamException {
        if (equalsStart(event, ObflQName.LEADER)) {
            parseLeader(fc, event, input);
            return true;
        } else if (equalsStart(event, ObflQName.MARKER)) {
            parseMarker(fc, event);
            return true;
        } else if (equalsStart(event, ObflQName.BR)) {
            fc.newLine();
            scanEmptyElement(input, ObflQName.BR);
            return true;
        } else if (equalsStart(event, ObflQName.EVALUATE)) {
            parseEvaluate(fc, event, input, tp, inTocEntryOnResumed);
            return true;
        } else if (equalsStart(event, ObflQName.STYLE)) {
            parseStyle(event, input, fc, tp, inTocEntryOnResumed);
            return true;
        } else if (equalsStart(event, ObflQName.SPAN)) {
            parseSpan(event, input, fc, tp, inTocEntryOnResumed);
            return true;
        } else if (equalsStart(event, ObflQName.ANCHOR)) {
            fc.insertAnchor(parseAnchor(event));
            return true;
        } else if (equalsStart(event, ObflQName.PAGE_NUMBER)) {
            parsePageNumber(fc, event, input);
            return true;
        } else if (equalsStart(event, ObflQName.EXTERNAL_REFERENCE)) {
            parseExternalReference(fc, event, input);
            return true;
        } else if (equalsStart(event, ObflQName.MARKER_REFERENCE)) {
            parseMarkerReference(fc, event, input, tp);
            return true;
        } else {
            return false;
        }
    }

    private NumeralStyle getNumeralStyle(XMLEvent event) {
        String styleStr = getAttr(event, "style");
        if (styleStr != null) {
            logger.warning(
                "@style has been deprecated since 2015. Let's take a while to think about that. " +
                "Use @number-format instead." + toLocation(event));
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            styleStr = getAttr(event, "number-format");
        }
        return numeralStyleFromAt(styleStr, event.getLocation());
    }

    /**
     * @param styleStr the string to parse
     * @param location location in case of an error
     * @return returns a NumeralStyle
     */
    private NumeralStyle numeralStyleFromAt(String styleStr, Location location) {
        NumeralStyle style = NumeralStyle.DEFAULT;
        try {
            style = NumeralStyle.valueOf(styleStr.replace('-', '_').toUpperCase());
        } catch (Exception e) {
            if (styleStr != null) {
                logger.warning("Unsupported value '" + styleStr + "'" + toLocation(location));
            }
        }

        return style;
    }


    /**
     * Gets a numeral style from a string. Either one of the enum names
     * as strings (for example upper-alpha or UPPER_ALPHA) or one of
     * 'A', 'a', 'I', 'i', '01' or '1'.
     *
     * @param str the string to parse
     * @return returns a numeral style for the string
     * @throws IllegalArgumentException if the string cannot be interpreted
     * @throws NullPointerException     if the string is null
     */
    static NumeralStyle parseNumeralStyle(String str) {
        if (str == null) {
            throw new NullPointerException("Null argument not supported.");
        }
        try {
            return NumeralStyle.valueOf(str.replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException e) {
            switch (str) {
                case "A":
                    return NumeralStyle.UPPER_ALPHA;
                case "a":
                    return NumeralStyle.LOWER_ALPHA;
                case "I":
                    return NumeralStyle.UPPER_ROMAN;
                case "i":
                    return NumeralStyle.LOWER_ROMAN;
                case "01":
                    return NumeralStyle.DECIMAL_LEADING_ZERO;
                case "1":
                    return NumeralStyle.DECIMAL;
                default:
                    throw new IllegalArgumentException("Cannot interpret string: " + str);
            }
        }
    }

    private String toLocation(XMLEvent event) {
        return toLocation(event.getLocation());
    }

    private String toLocation(Location l) {
        StringBuilder sb = new StringBuilder();
        if (l != null) {
            if (l.getLineNumber() > -1) {
                sb.append("line: ").append(l.getLineNumber());
            }
            if (l.getColumnNumber() > -1) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append("column: ").append(l.getColumnNumber());
            }
        }
        return (sb.length() > 0 ? " (at " + sb.toString() + ")" : "");
    }

    private void parsePageNumber(
        BlockContentBuilder fc,
        XMLEvent event,
        XMLEventIterator input
    ) throws XMLStreamException {
        String refId = getAttr(event, "ref-id");
        NumeralStyle style = getNumeralStyle(event);
        scanEmptyElement(input, ObflQName.PAGE_NUMBER);
        fc.insertPageReference(refId, style);
    }

    private void parseExternalReference(
            BlockContentBuilder fc,
            XMLEvent event,
            XMLEventIterator input
    ) throws XMLStreamException {
        Iterator<Attribute> it = event.asStartElement().getAttributes();
        if (externalReferenceObject == null) {
            externalReferenceObject = new HashMap<>();
        }
        while (it.hasNext()) {
            Attribute a = it.next();

            if (a.getName().getPrefix() == null || a.getName().getPrefix().isEmpty()) {
                continue;
            }

            MetaDataItem newItem = new MetaDataItem(
                new QName("http://www.w3.org/2000/xmlns/", a.getName().getPrefix(), "xmlns"),
                a.getName().getNamespaceURI()
            );
            if (!checkIfAlreadyContainsPrefix(meta, newItem)) {
                meta.add(newItem);
            }
            externalReferenceObject.put(a.getName(), a.getValue());
        }
        fc.insertExternalReference(externalReferenceObject);
    }

    private boolean checkIfAlreadyContainsPrefix(List<MetaDataItem> meta, MetaDataItem newItem) {
        for (MetaDataItem metaItem : meta) {
            if (metaItem.getKey().getPrefix().equalsIgnoreCase(newItem.getKey().getPrefix())) {
                return true;
            }
        }
        return false;
    }


    private void parseMarkerReference(
        BlockContentBuilder fc,
        XMLEvent event, XMLEventIterator input,
        TextProperties tp
    ) throws XMLStreamException {
        fc.insertMarkerReference(parseMarkerReferenceField(event, false), tp);
        scanEmptyElement(input, ObflQName.MARKER_REFERENCE);
    }

    private void parseEvaluate(
        BlockContentBuilder fc,
        XMLEvent event, XMLEventIterator input,
        TextProperties tp,
        boolean inTocEntryOnResumed
    ) throws XMLStreamException {
        String expr = getAttr(event, "expression");
        scanEmptyElement(input, ObflQName.EVALUATE);
        final OBFLDynamicContent dynamic;
        if (inTocEntryOnResumed) {
            dynamic = new OBFLDynamicContent(expr, fm.getExpressionFactory(),
                    OBFLVariable.PAGE_NUMBER,
                    OBFLVariable.VOLUME_NUMBER,
                    OBFLVariable.VOLUME_COUNT,
                    OBFLVariable.SHEET_COUNT,
                    OBFLVariable.VOLUME_SHEET_COUNT,
                    OBFLVariable.STARTED_VOLUME_NUMBER,
                    OBFLVariable.STARTED_VOLUME_FIRST_CONTENT_PAGE_NUMBER);
        } else {
            dynamic = new OBFLDynamicContent(expr, fm.getExpressionFactory(),
                    OBFLVariable.PAGE_NUMBER,
                    OBFLVariable.VOLUME_NUMBER,
                    OBFLVariable.VOLUME_COUNT,
                    OBFLVariable.SHEET_COUNT,
                    OBFLVariable.VOLUME_SHEET_COUNT,
                    OBFLVariable.STARTED_VOLUME_NUMBER,
                    OBFLVariable.STARTED_PAGE_NUMBER);
        }
        fc.insertEvaluate(dynamic, tp);
    }

    private void parseVolumeTemplate(
        XMLEvent event,
        XMLEventIterator input,
        TextProperties tp
    ) throws XMLStreamException {
        // TODO: This check can be removed after the concept of an alternative variable name
        // has been removed from the OBFL specification.
        // See https://github.com/mtmse/obfl/issues/13
        if (getAttr(event, "volume-count-variable") != null || getAttr(event, "volume-number-variable") != null) {
            throw new UnsupportedOperationException("Alternative variable names are not supported");
        }
        String useWhen = getAttr(event, ObflQName.ATTR_USE_WHEN);
        String splitterMax = getAttr(event, "sheets-in-volume-max");
        OBFLCondition condition = new OBFLCondition(
            useWhen,
            fm.getExpressionFactory(),
            OBFLVariable.PAGE_NUMBER,
            OBFLVariable.VOLUME_NUMBER,
            OBFLVariable.VOLUME_COUNT,
            OBFLVariable.SHEET_COUNT,
            OBFLVariable.VOLUME_SHEET_COUNT);
        VolumeTemplateProperties vtp = new VolumeTemplateProperties.Builder(Integer.parseInt(splitterMax))
                .condition(condition)
                .build();
        VolumeTemplateBuilder template = formatter.newVolumeTemplate(vtp);
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.PRE_CONTENT)) {
                parsePreVolumeContent(event, input, template.getPreVolumeContentBuilder(), tp);
            } else if (equalsStart(event, ObflQName.POST_CONTENT)) {
                parsePostVolumeContent(event, input, template.getPostVolumeContentBuilder(), tp);
            } else if (equalsEnd(event, ObflQName.VOLUME_TEMPLATE)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parsePreVolumeContent(
        XMLEvent event,
        XMLEventIterator input,
        VolumeContentBuilder template,
        TextProperties tp
    ) throws XMLStreamException {
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.SEQUENCE)) {
                parseVolumeSequence(event, input, template, tp);
            } else if (equalsStart(event, ObflQName.TOC_SEQUENCE)) {
                parseTocSequence(event, input, template, tp);
            } else if (equalsStart(event, ObflQName.DYNAMIC_SEQUENCE)) {
                parseItemSequence(event, input, template, tp);
            } else if (equalsEnd(event, ObflQName.PRE_CONTENT)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseVolumeTransition(
        XMLEvent event,
        XMLEventIterator input,
        TextProperties tp
    ) throws XMLStreamException {
        TransitionBuilder template = formatter.getTransitionBuilder();
        TransitionBuilderProperties.Builder propsBuilder = new TransitionBuilderProperties.Builder();
        String rangeStr = getAttr(event, "range");
        if (rangeStr != null) {
            ApplicationRange range = ApplicationRange.parse(rangeStr);
            propsBuilder.applicationRange(range);
        } else {
            propsBuilder.applicationRange(ApplicationRange.PAGE);
        }
        template.setProperties(propsBuilder.build());
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.BLOCK_INTERRUPTED)) {
                throw new RuntimeException(String.format("%s is not supported.", ObflQName.BLOCK_INTERRUPTED));
                //parseTransitionBlock(event, input, template.getBlockInterruptedBuilder(), tp);
            } else if (equalsStart(event, ObflQName.BLOCK_RESUMED)) {
                throw new RuntimeException(String.format("%s is not supported.", ObflQName.BLOCK_RESUMED));
                //parseTransitionBlock(event, input, template.getBlockResumedBuilder(), tp);
            } else if (equalsStart(event, ObflQName.SEQUENCE_INTERRUPTED)) {
                parseTransitionSequence(event, input, template.getSequenceInterruptedBuilder(), tp);
            } else if (equalsStart(event, ObflQName.SEQUENCE_RESUMED)) {
                parseTransitionSequence(event, input, template.getSequenceResumedBuilder(), tp);
            } else if (equalsStart(event, ObflQName.ANY_INTERRUPTED)) {
                parseTransitionSequence(event, input, template.getAnyInterruptedBuilder(), tp);
            } else if (equalsStart(event, ObflQName.ANY_RESUMED)) {
                parseTransitionSequence(event, input, template.getAnyResumedBuilder(), tp);
            } else if (equalsEnd(event, ObflQName.VOLUME_TRANSITION)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTransitionSequence(
        XMLEvent event,
        XMLEventIterator input,
        BlockBuilder builder,
        TextProperties tp
    ) throws XMLStreamException {
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, builder, tp);
            } else if (
                equalsEnd(
                    event,
                    ObflQName.SEQUENCE_INTERRUPTED,
                    ObflQName.SEQUENCE_RESUMED,
                    ObflQName.ANY_INTERRUPTED,
                    ObflQName.ANY_RESUMED
                )
            ) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parsePostVolumeContent(
        XMLEvent event,
        XMLEventIterator input,
        VolumeContentBuilder template,
        TextProperties tp
    ) throws XMLStreamException {
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.SEQUENCE)) {
                parseVolumeSequence(event, input, template, tp);
            } else if (equalsStart(event, ObflQName.TOC_SEQUENCE)) { // TODO: update OBFL specification
                parseTocSequence(event, input, template, tp);
            } else if (equalsStart(event, ObflQName.DYNAMIC_SEQUENCE)) {
                parseItemSequence(event, input, template, tp);
            } else if (equalsEnd(event, ObflQName.POST_CONTENT)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseVolumeSequence(
        XMLEvent event,
        XMLEventIterator input,
        VolumeContentBuilder template,
        TextProperties tp
    ) throws XMLStreamException {
        String masterName = getAttr(event, "master");
        tp = getTextProperties(event, tp);
        SequenceProperties.Builder builder = new SequenceProperties.Builder(masterName);
        String initialPageNumber = getAttr(event, ObflQName.ATTR_INITIAL_PAGE_NUMBER);
        if (initialPageNumber != null) {
            builder.initialPageNumber(Integer.parseInt(initialPageNumber));
        }
        String pageNumberCounter = getAttr(event, "page-number-counter");
        if (pageNumberCounter != null) {
            builder.pageCounterName(pageNumberCounter);
        }
        template.newSequence(builder.build());
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, template, tp);
            } else if (equalsStart(event, ObflQName.TABLE)) {
                parseTable(event, input, template, tp);
            } else if (equalsStart(event, ObflQName.XML_DATA)) {
                parseXMLData(template, event, input, tp);
            } else if (equalsEnd(event, ObflQName.SEQUENCE)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseTocSequence(
        XMLEvent event,
        XMLEventIterator input,
        VolumeContentBuilder template,
        TextProperties tp
    ) throws XMLStreamException {
        String masterName = getAttr(event, "master");
        String tocName = getAttr(event, "toc");
        tp = getTextProperties(event, tp);
        TocProperties.TocRange range = TocProperties.TocRange.valueOf(getAttr(event, "range").toUpperCase());
        TocProperties.Builder builder = new TocProperties.Builder(masterName, tocName, range);
        String initialPageNumber = getAttr(event, ObflQName.ATTR_INITIAL_PAGE_NUMBER);
        if (initialPageNumber != null) {
            builder.initialPageNumber(Integer.parseInt(initialPageNumber));
        }
        String pageNumberCounter = getAttr(event, "page-number-counter");
        if (pageNumberCounter != null) {
            builder.pageCounterName(pageNumberCounter);
        }
        template.newTocSequence(builder.build());
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.ON_TOC_START)) {
                template.newOnTocStart(
                    new OBFLCondition(
                        getAttr(event, ObflQName.ATTR_USE_WHEN),
                        fm.getExpressionFactory(),
                        OBFLVariable.STARTED_VOLUME_NUMBER,
                        OBFLVariable.STARTED_PAGE_NUMBER,
                        OBFLVariable.PAGE_NUMBER,
                        OBFLVariable.VOLUME_NUMBER,
                        OBFLVariable.VOLUME_COUNT,
                        OBFLVariable.SHEET_COUNT,
                        OBFLVariable.VOLUME_SHEET_COUNT
                    )
                );
                parseOnEvent(event, input, template, ObflQName.ON_TOC_START, tp);
            } else if (equalsStart(event, ObflQName.ON_VOLUME_START)) {
                template.newOnVolumeStart(
                    new OBFLCondition(
                        getAttr(event, ObflQName.ATTR_USE_WHEN),
                        fm.getExpressionFactory(),
                        OBFLVariable.STARTED_VOLUME_NUMBER,
                        OBFLVariable.STARTED_PAGE_NUMBER,
                        OBFLVariable.PAGE_NUMBER,
                        OBFLVariable.VOLUME_NUMBER,
                        OBFLVariable.VOLUME_COUNT,
                        OBFLVariable.SHEET_COUNT,
                        OBFLVariable.VOLUME_SHEET_COUNT
                    )
                );
                parseOnEvent(event, input, template, ObflQName.ON_VOLUME_START, tp);
            } else if (equalsStart(event, ObflQName.ON_VOLUME_END)) {
                template.newOnVolumeEnd(
                    new OBFLCondition(
                        getAttr(event, ObflQName.ATTR_USE_WHEN),
                        fm.getExpressionFactory(),
                        OBFLVariable.STARTED_VOLUME_NUMBER,
                        OBFLVariable.STARTED_PAGE_NUMBER,
                        OBFLVariable.PAGE_NUMBER,
                        OBFLVariable.VOLUME_NUMBER,
                        OBFLVariable.VOLUME_COUNT,
                        OBFLVariable.SHEET_COUNT,
                        OBFLVariable.VOLUME_SHEET_COUNT
                    )
                );
                parseOnEvent(event, input, template, ObflQName.ON_VOLUME_END, tp);
            } else if (equalsStart(event, ObflQName.ON_TOC_END)) {
                template.newOnTocEnd(
                    new OBFLCondition(
                        getAttr(event, ObflQName.ATTR_USE_WHEN),
                        fm.getExpressionFactory(),
                        OBFLVariable.STARTED_VOLUME_NUMBER,
                        OBFLVariable.STARTED_PAGE_NUMBER,
                        OBFLVariable.PAGE_NUMBER,
                        OBFLVariable.VOLUME_NUMBER,
                        OBFLVariable.VOLUME_COUNT,
                        OBFLVariable.SHEET_COUNT,
                        OBFLVariable.VOLUME_SHEET_COUNT
                    )
                );
                parseOnEvent(event, input, template, ObflQName.ON_TOC_END, tp);
            } else if (equalsEnd(event, ObflQName.TOC_SEQUENCE)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseItemSequence(
        XMLEvent event,
        XMLEventIterator input,
        VolumeContentBuilder template,
        TextProperties tp
    ) throws XMLStreamException {
        String masterName = getAttr(event, "master");
        tp = getTextProperties(event, tp);
        SequenceProperties.Builder builder = new SequenceProperties.Builder(masterName);
        String initialPageNumber = getAttr(event, ObflQName.ATTR_INITIAL_PAGE_NUMBER);
        if (initialPageNumber != null) {
            builder.initialPageNumber(Integer.parseInt(initialPageNumber));
        }
        String pageNumberCounter = getAttr(event, "page-number-counter");
        if (pageNumberCounter != null) {
            builder.pageCounterName(pageNumberCounter);
        }
        DynamicSequenceBuilder dsb = template.newDynamicSequence(builder.build());
        FormatterCore context = null;
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.INSERT_REFS_LIST)) {
                parseRefsList(event, input, dsb, tp);
                context = null;
            } else if (equalsStart(event, ObflQName.BLOCK)) {
                if (context == null) {
                    context = dsb.newStaticContext();
                }
                parseBlock(event, input, context, tp);
            } else if (equalsEnd(event, ObflQName.DYNAMIC_SEQUENCE)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseRefsList(
        XMLEvent event,
        XMLEventIterator input,
        DynamicSequenceBuilder dsb,
        TextProperties tp
    ) throws XMLStreamException {
        String collection = getAttr(event, "collection");
        tp = getTextProperties(event, tp);
        ItemSequenceProperties.Range range =
                ItemSequenceProperties.Range.valueOf(getAttr(event, "range").toUpperCase());
        ItemSequenceProperties.Builder builder = new ItemSequenceProperties.Builder(collection, range);

        ReferenceListBuilder rlb = dsb.newReferencesListContext(builder.build());

        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.ON_PAGE_START)) {
                parseOnEvent(event, input, rlb.newOnPageStart(), ObflQName.ON_PAGE_START, tp);
            } else if (equalsStart(event, ObflQName.ON_PAGE_END)) {
                parseOnEvent(event, input, rlb.newOnPageEnd(), ObflQName.ON_PAGE_END, tp);
            } else if (equalsStart(event, ObflQName.ON_VOLUME_START)) {
                parseOnEvent(event, input, rlb.newOnVolumeStart(), ObflQName.ON_VOLUME_START, tp);
            } else if (equalsStart(event, ObflQName.ON_VOLUME_END)) {
                parseOnEvent(event, input, rlb.newOnVolumeEnd(), ObflQName.ON_VOLUME_END, tp);
            } else if (equalsStart(event, ObflQName.ON_COLLECTION_START)) {
                parseOnEvent(event, input, rlb.newOnCollectionStart(), ObflQName.ON_COLLECTION_START, tp);
            } else if (equalsStart(event, ObflQName.ON_COLLECTION_END)) {
                parseOnEvent(event, input, rlb.newOnCollectionEnd(), ObflQName.ON_COLLECTION_END, tp);
            } else if (equalsEnd(event, ObflQName.INSERT_REFS_LIST)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void parseOnEvent(
        XMLEvent event,
        XMLEventIterator input,
        FormatterCore fc,
        QName end,
        TextProperties tp
    ) throws XMLStreamException {
        while (input.hasNext()) {
            event = input.nextEvent();
            if (equalsStart(event, ObflQName.BLOCK)) {
                parseBlock(event, input, fc, tp);
            } else if (equalsEnd(event, end)) {
                break;
            } else {
                report(event);
            }
        }
    }

    private void scanEmptyElement(XMLEventIterator input, QName element) throws XMLStreamException {
        XMLEvent event;
        while (input.hasNext()) {
            event = input.nextEvent();
            if (event.getEventType() != XMLStreamConstants.END_ELEMENT) {
                throw new RuntimeException("Unexpected input");
            } else if (equalsEnd(event, element)) {
                break;
            }
        }
    }

    private static String getAttr(XMLEvent event, String attr) {
        return getAttr(event, new QName(attr));
    }

    private static String getAttr(XMLEvent event, QName attr) {
        Attribute ret = event.asStartElement().getAttributeByName(attr);
        if (ret == null) {
            return null;
        } else {
            return ret.getValue();
        }
    }

    private TextProperties getTextProperties(XMLEvent event, TextProperties defaults) {
        String loc = getLang(event, defaults.getLocale());
        boolean hyph = getHyphenate(event, defaults.isHyphenating());
        String trans = getTranslate(event, defaults.getTranslationMode());
        return new TextProperties.Builder(loc.toString()).translationMode(trans).hyphenate(hyph).build();
    }

    private String getLang(XMLEvent event, String locale) {
        String lang = getAttr(event, ObflQName.ATTR_XML_LANG);
        if (lang != null) {
            if ("".equals(lang)) {
                return null;
            } else {
                //we're doing the parsing only to get the validation
                return FilterLocale.parse(lang).toString();
            }
        }
        return locale;
    }

    private boolean getHyphenate(XMLEvent event, boolean hyphenate) {
        String hyph = getAttr(event, ObflQName.ATTR_HYPHENATE);
        if (hyph != null) {
            if ("".equals(hyph)) {
                return hyphGlobal;
            } else {
                return "true".equals(hyph);
            }
        }
        return hyphenate;
    }

    private String getTranslate(XMLEvent event, String translate) {
        String tr = getAttr(event, ObflQName.ATTR_TRANSLATE);
        if (tr != null) {
            if ("".equals(tr)) {
                return mode;
            } else {
                return tr;
            }
        }
        return translate;
    }

    @Override
    public List<MetaDataItem> getMetaData() {
        return meta;
    }

    FactoryManager getFactoryManager() {
        return fm;
    }

}
