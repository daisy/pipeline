/*
 * This class was taken from
 * org.daisy.dotify.formatter.impl.writer.PEFMediaWriter and slightly
 * modified because we want to extend it and
 * org.daisy.dotify.formatter.impl.writer is a private package in
 * OSGi.
 */

package org.daisy.pipeline.braille.dotify.calabash.impl;

import org.daisy.dotify.api.writer.AttributeItem;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterException;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.io.StateObject;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;


/**
 * PagedMediaWriter implementation that outputs PEF 2008-1.
 *
 * @author Joel Håkansson
 */
class PEFMediaWriter implements PagedMediaWriter {
    private static final String DC_NAMESPACE_URI = "http://purl.org/dc/elements/1.1/";
    private static final Logger logger = Logger.getLogger(PEFMediaWriter.class.getCanonicalName());
    protected PrintStream pst;
    private boolean hasOpenVolume;
    private boolean hasOpenSection;
    private boolean hasOpenPage;
    private int cCols;
    private int cRows;
    private int cRowgap;
    private boolean cDuplex;
	protected final StateObject state;
    private int errorCount = 0;

    /*
     * List<MetaDataItem> objects are not only used to pass meta elements from OBFL to PEF but also
     * to pass along namespace binding information to make it easier for PEFMediaWriter to declare
     * namespace prefixes on the root element.
     */
    protected final List<MetaDataItem> metadata;
    private Map<String, String> namespaces = new HashMap<>();
    private int namespaceCounter = 0;

    /**
     * Create a new PEFMediaWriter.
     */
    public PEFMediaWriter() {
        hasOpenVolume = false;
        hasOpenSection = false;
        hasOpenPage = false;
        cCols = 0;
        cRows = 0;
        cRowgap = 0;
        cDuplex = true;
        state = new StateObject("Writer");
        this.metadata = new ArrayList<>();
    }

    @Override
    public void prepare(List<MetaDataItem> meta) {
        state.assertUnopened();
        metadata.addAll(meta);
    }

    @Override
    public void open(OutputStream os) throws PagedMediaWriterException {
        open(os, null);
    }

    private void open(OutputStream os, List<MetaDataItem> data) throws PagedMediaWriterException {
        if (data != null) {
            metadata.addAll(data);
        }
        state.assertUnopened();
        state.open();
        try {
            pst = new PrintStream(os, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new PagedMediaWriterException("Cannot open PrintStream with UTF-8.", e);
        }
        hasOpenVolume = false;
        hasOpenSection = false;
        hasOpenPage = false;
        pst.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pst.print("<pef version=\"2008-1\" xmlns=\"http://www.daisy.org/ns/2008/pef\"");

        Iterator<MetaDataItem> metaIT = metadata.iterator();
        while (metaIT.hasNext()) {
            MetaDataItem metaDataItem = metaIT.next();
            if ("xmlns".equals(metaDataItem.getKey().getPrefix())) {
                pst.print(" " + metaDataItem.getKey().getPrefix());
                pst.print(":" + metaDataItem.getKey().getLocalPart());
                pst.print("=\"" + metaDataItem.getValue() + "\"");
                metaIT.remove();
                namespaces.put(metaDataItem.getValue(), metaDataItem.getKey().getLocalPart());
            }
        }
        pst.println(">");
        pst.println("<head>");
        List<MetaDataItem> meta = organizeMetadata(metadata);
        Map<String, String> ns = getNamespaces(meta);

        pst.print("<meta");
        for (Entry<String, String> entry : ns.entrySet()) {
            pst.print(" ");
            pst.print("xmlns:");
            pst.print(entry.getValue());
            pst.print("=\"");
            pst.print(entry.getKey());
            pst.print("\"");
        }
        pst.println(">");

        if (meta != null) {
            for (MetaDataItem item : meta) {
                String name = ns.get(item.getKey().getNamespaceURI()) + ":" + item.getKey().getLocalPart();
                pst.print("<" + name);
                AttributeItem att = item.getAttribute();
                if (att != null) {
                    pst.print(" ");
                    pst.print(att.getName());
                    pst.print("=\"");
                    pst.print(escape(att.getValue()));
                    pst.print("\"");
                }
                pst.println(">" + escape(item.getValue()) + "</" + name + ">");
            }
        }

        pst.println("</meta>");
        pst.println("</head>");
        pst.println("<body>");
    }

    private static List<MetaDataItem> organizeMetadata(List<MetaDataItem> meta) {
        List<MetaDataItem> dc = new ArrayList<>();
        List<MetaDataItem> other = new ArrayList<>();
        MetaDataItem identifier = null;
        MetaDataItem date = null;
        for (MetaDataItem item : meta) {
            if (DC_NAMESPACE_URI.equals(item.getKey().getNamespaceURI())) {
                if ("identifier".equals(item.getKey().getLocalPart())) {
                    // we'll use the last defined identifier
                    identifier = item;
                } else if ("date".equals(item.getKey().getLocalPart())) {
                    // we'll use the last defined date
                    date = item;
                } else if ("format".equals(item.getKey().getLocalPart())) {
                    // ignore this item
                } else {
                    dc.add(item);
                }
            } else {
                other.add(item);
            }
        }
        if (identifier == null) {
            identifier = new MetaDataItem(new QName(DC_NAMESPACE_URI, "identifier", "dc"), "identifier?");
        }
        if (date == null) {
            date = new MetaDataItem(
                    new QName(DC_NAMESPACE_URI, "date", "dc"),
                    new SimpleDateFormat("yyyy-MM-dd").format(new Date())
            );
        }
        List<MetaDataItem> ret = new ArrayList<>();
        ret.add(new MetaDataItem(new QName(DC_NAMESPACE_URI, "format", "dc"), "application/x-pef+xml"));
        ret.add(identifier);
        ret.add(date);
        ret.addAll(dc);
        ret.addAll(other);
        return ret;
    }

    private static Map<String, String> getNamespaces(List<MetaDataItem> meta) {
        Map<String, String> ret = new HashMap<>();
        Map<String, String> prefixes = new HashMap<>();
        // Go through all items to check if named prefixes are used
        for (MetaDataItem item : meta) {
            String value = item.getKey().getPrefix();
            if (!"".equals(value)) {
                prefixes.put(item.getKey().getNamespaceURI(), value);
            }
        }
        int i = 1;
        for (MetaDataItem item : meta) {
            String value = prefixes.get(item.getKey().getNamespaceURI());
            if (value == null || "".equals(value)) {
                do {
                    value = "ns" + i;
                    i++;
                    //Handle the unlikely event that someone used ns[i] as their named namespace above
                } while (prefixes.containsValue(value));
                prefixes.put(item.getKey().getNamespaceURI(), value);
            }
            ret.put(item.getKey().getNamespaceURI(), value);
        }
        return ret;
    }

    @Override
    public void newPage() {
        state.assertOpen();
        closeOpenPage();
        if (!hasOpenSection) {
            throw new IllegalStateException("No open section.");
        }
        pst.println("<page>");
        hasOpenPage = true;
    }

    //performance optimization over nonBraillePattern.matcher(row.getChars()).matches()
    private boolean validate(Row row) {
        char c;
        for (int i = 0; i < row.getChars().length(); i++) {
            c = row.getChars().charAt(i);
            if (c < 0x2800 || c > 0x28FF) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void newRow(Row row) {
        state.assertOpen();

        if (errorCount < 10 && logger.isLoggable(Level.WARNING) && !validate(row)) {
            logger.warning(
                    "Non-braille characters in output" +
                        (errorCount == 9 ? " (supressing additional messages of this kind)" : "") + ": " +
                        row.getChars()
            );
            errorCount++;
        }
        pst.print("<row");

        Object externalReference = row.getExternalReference();
        if (externalReference instanceof Map) {
            Map<QName, String> metadata = (Map<QName, String>) externalReference;
            if (metadata != null && !metadata.isEmpty()) {
                for (Map.Entry<QName, String> entry : metadata.entrySet()) {
                    QName name = entry.getKey();

                    String prefix = namespaces.get(name.getNamespaceURI());
                    if (prefix == null) {
                        prefix = "ns" + namespaceCounter++;
                        pst.print(" xmlns:" + prefix + "=\"" + name.getNamespaceURI() + "\"");
                    }

                    pst.print(" " + prefix + ":" + name.getLocalPart() + "=\"" + entry.getValue() + "\"");

                }
            }
        }

        if (row.getRowSpacing() != null) {
            pst.print(" rowgap=\"" + (int) Math.floor((row.getRowSpacing() - 1) * 4) + "\"");
        }

        pst.println((row.getChars().length() > 0 ? ">" + row.getChars() + "</row>" : "/>"));
    }

    @Override
    public void newRow() {
        state.assertOpen();
        pst.println("<row/>");
    }

    @Override
    public void newVolume(SectionProperties master) {
        state.assertOpen();
        closeOpenVolume();
        cCols = master.getPageWidth();
        cRows = master.getPageHeight();
        cRowgap = (int) Math.floor((master.getRowSpacing() - 1) * 4);
        cDuplex = master.duplex();
        pst.println("<volume cols=\"" + cCols +
                "\" rows=\"" + cRows +
                "\" rowgap=\"" + cRowgap +
                "\" duplex=\"" + cDuplex +
                "\">");
        hasOpenVolume = true;
    }

    @Override
    public void newSection(SectionProperties master) {
        state.assertOpen();
        if (!hasOpenVolume) {
            newVolume(master);
        }
        closeOpenSection();
        pst.print("<section");

        if (cCols != master.getPageWidth()) {
            pst.print(" cols=\"" + master.getPageWidth() + "\"");
        }
        if (cRows != master.getPageHeight()) {
            pst.print(" rows=\"" + master.getPageHeight() + "\"");
        }
        if (cRowgap != (int) Math.floor((master.getRowSpacing() - 1) * 4)) {
            pst.print(" rowgap=\"" + (int) Math.floor((master.getRowSpacing() - 1) * 4) + "\"");
        }
        if (cDuplex != master.duplex()) {
            pst.print(" duplex=\"" + master.duplex() + "\"");
        }
        pst.println(">");
        hasOpenSection = true;
    }

    //performance optimization over text.replaceAll()
    private String escape(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private void closeOpenVolume() {
        state.assertOpen();
        closeOpenSection();
        if (hasOpenVolume) {
            pst.println("</volume>");
            hasOpenVolume = false;
        }
    }

    private void closeOpenSection() {
        state.assertOpen();
        closeOpenPage();
        if (hasOpenSection) {
            pst.println("</section>");
            hasOpenSection = false;
        }
    }

    private void closeOpenPage() {
        state.assertOpen();
        if (hasOpenPage) {
            pst.println("</page>");
            hasOpenPage = false;
        }
    }

    @Override
    public void close() {
        if (state.isClosed()) {
            return;
        }
        state.assertOpen();
        closeOpenVolume();
        pst.println("</body>");
        pst.println("</pef>");
        pst.close();
        state.close();
    }

}
