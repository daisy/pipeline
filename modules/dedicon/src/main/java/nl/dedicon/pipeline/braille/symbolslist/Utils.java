package nl.dedicon.pipeline.braille.symbolslist;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Symbols list utilities
 * 
 * @author Paul Rambags
 */
public class Utils  {
    
    /**
     * Convert an XdmNode to a Document node
     * 
     * @param node XdmNode
     * @return Document node
     * @throws ParserConfigurationException thrown on parse exceptions
     * @throws SAXException thrown on a SAX exception
     * @throws IOException thrown on an IO exception
     */
    public static Document convertToDocument(XdmNode node) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(node.toString())));
        return document;
    }
    
    /**
     * Convert a Document node to an XdmNode
     * 
     * @param document Document node
     * @param documentBuilder document builder
     * @param useFile if true, the resulting XdmNode will be saved in a temporary file so that it has an URI
     * @return XdmNode
     * @throws IOException thrown on an IO exception
     * @throws SaxonApiException thrown on a Saxon API exception
     */
    public static XdmNode convertToXdmNode(Document document, DocumentBuilder documentBuilder, boolean useFile) throws IOException, SaxonApiException {
        String xml = documentBuilder.wrap(document).toString();
        XdmNode node;
        if (useFile) {
            String className = new Object(){}.getClass().getName();
            File tempFile = File.createTempFile(className, ".tmp");
            tempFile.deleteOnExit();
            Files.write(tempFile.toPath(), xml.getBytes());
            node = documentBuilder.build(tempFile);
        } else {
            node = documentBuilder.build(new StreamSource(new StringReader(xml)));
        }
        return node;
    }

    /**
     * Get the first child with a given name
     * 
     * @param node node
     * @param name child node name
     * @return the first child with the given name, or null if it doesn't exist
     */
    public static Node getChild(Node node, String name) {
        if (node == null) {
            return null;
        }
        Node child = node.getFirstChild();
        while (child != null) {
            if (name.equals(child.getNodeName())) {
                return child;
            }
            child = child.getNextSibling();
        }
        return null;
    }
    
    /**
     * Add a new child node to a parent node
     * 
     * @param parent parent node
     * @param name name of the child node
     * @return the new child node
     */
    public static Element addChild(Node parent, String name) {
        Element child = parent.getOwnerDocument().createElement(name);
        parent.appendChild(child);
        return child;
    }
    
    /**
     * Get the string value of a child node
     * 
     * @param node XdmNode
     * @param childName child name
     * @return String value of the child, or null if it doesn't exist
     */
    public static String getValue(XdmNode node, QName childName) {
        XdmSequenceIterator childIterator = node.axisIterator(Axis.CHILD, childName);
        if (!childIterator.hasNext()) {
            return null;
        }
        return childIterator.next().getStringValue();
    }
    
    /**
     * Determine whether a unicode character is in the braille range
     * 
     * @param c unicode character
     * @return true if it is in the braile range, false otherwise
     */
    public static boolean isBraille(char c) {
        return 0x2800 <= c && c <= 0x283F;  // 28FF?
    }
}
