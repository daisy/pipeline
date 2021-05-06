package org.daisy.dotify.common.xml;

import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class NamespaceContextBuilderTest {
    private static final String BOUND_URI = "www.example.com/bound";
    private static final String BOUND_PREFIX = "test";
    private static final String DEFAULT_NS = "www.example.com/default";

    @Test
    public void testNsContext_01() {
        NamespaceContext nc = new NamespaceContextBuilder().add(BOUND_PREFIX, BOUND_URI).build();
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, nc.getPrefix(XMLConstants.NULL_NS_URI));
        nc = new NamespaceContextBuilder(DEFAULT_NS).add(BOUND_PREFIX, BOUND_URI).build();
        assertEquals(XMLConstants.DEFAULT_NS_PREFIX, nc.getPrefix(DEFAULT_NS));
        assertEquals("test", nc.getPrefix("www.example.com/bound"));
        assertEquals(null, nc.getPrefix("www.example.com/unbound"));
        assertEquals(XMLConstants.XML_NS_PREFIX, nc.getPrefix(XMLConstants.XML_NS_URI));
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE, nc.getPrefix(XMLConstants.XMLNS_ATTRIBUTE_NS_URI));
    }

    @Test
    public void testNsContext_02() {
        NamespaceContext nc = new NamespaceContextBuilder().add(BOUND_PREFIX, BOUND_URI).build();
        assertEquals(XMLConstants.NULL_NS_URI, nc.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
        nc = new NamespaceContextBuilder(DEFAULT_NS).add(BOUND_PREFIX, BOUND_URI).build();
        assertEquals(DEFAULT_NS, nc.getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX));
        assertEquals(BOUND_URI, nc.getNamespaceURI(BOUND_PREFIX));
        assertEquals(XMLConstants.NULL_NS_URI, nc.getNamespaceURI("prefix"));
        assertEquals(XMLConstants.XML_NS_URI, nc.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
        assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, nc.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
    }

}
