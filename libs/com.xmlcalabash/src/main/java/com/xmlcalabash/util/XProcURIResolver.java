package com.xmlcalabash.util;

import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.StandardModuleURIResolver;
import net.sf.saxon.lib.StandardUnparsedTextResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.trans.XPathException;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.Configuration;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.xmlresolver.CatalogSource;
import org.xmlresolver.Resolver;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 29, 2008
 * Time: 4:04:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class XProcURIResolver implements URIResolver, EntityResolver, ModuleURIResolver, UnparsedTextURIResolver {
    private Logger logger = LoggerFactory.getLogger(XProcURIResolver.class);
    private URIResolver uriResolver = null;
    private EntityResolver entityResolver = null;
    private ModuleURIResolver moduleURIResolver = null;
    private UnparsedTextURIResolver unparsedTextResolver = null;
    private XProcRuntime runtime = null;
    private Hashtable<String,XdmNode> cache = new Hashtable<String,XdmNode> ();
    private Resolver catalogResolver = null;
    private static boolean useCache = true; // FIXME: this is supposed to be temporary!

    public XProcURIResolver(XProcRuntime runtime) {
        this.runtime = runtime;
    }

    public void setUnderlyingURIResolver(URIResolver resolver) {
        uriResolver = resolver;
        // Resolvers are chained together, but there's no way to traverse the
        // chain. We want to be able to do catalog resolution, so we cache
        // the first catalog resolver that we see.
        if ((catalogResolver == null) && (resolver instanceof org.xmlresolver.Resolver)) {
            catalogResolver = (Resolver) resolver;
        }
    }

    public URIResolver getUnderlyingURIResolver() {
        return uriResolver;
    }

    public void setUnderlyingEntityResolver(EntityResolver resolver) {
        entityResolver = resolver;
    }

    public EntityResolver getUnderlyingEntityResolver() {
        return entityResolver;
    }

    public void setUnderlyingUnparsedTextURIResolver(UnparsedTextURIResolver resolver) {
        unparsedTextResolver = resolver;
    }

    public UnparsedTextURIResolver getUnderlyingUnparsedTextURIResolver() {
        return unparsedTextResolver;
    }

    public void setUnderlyingModuleURIResolver(ModuleURIResolver resolver) {
        moduleURIResolver = resolver;
    }

    public ModuleURIResolver getUnderlyingModuleURIResolver() {
        return moduleURIResolver;
    }

    public void addCatalogs(List<String> catalogs) {
        if (catalogResolver != null) {
            for (String catalog : catalogs) {
                logger.debug("Adding catalog to resolver: " + catalog);
                try {
                    URL cat = new URL(catalog);
                    InputSource source = new InputSource(cat.openStream());
                    source.setSystemId(catalog);

                    CatalogSource catsource = new CatalogSource.InputSourceCatalogSource(source);
                    catalogResolver.getCatalog().addSource(catsource);
                } catch (MalformedURLException e) {
                    logger.info("Malformed catalog URI in jar file: " + catalog);
                } catch (IOException e) {
                    logger.info("I/O error reading catalog URI in jar file: " + catalog);
                }
            }
        } else {
            logger.info("Not adding catalogs to resolver, no catalog resolver is known");
        }
    }

    public void cache(XdmNode doc, URI baseURI) {
        XdmNode root = S9apiUtils.getDocumentElement(doc);

        // We explicitly use the base URI of the root element so that if it has an xml:base
        // attribute, that becomes the base URI of the document.

        URI docURI = baseURI.resolve(root.getBaseURI());
        if (useCache) {
            cache.put(docURI.toASCIIString(), doc);
        }
    }

    public Source resolve(String href, String base) throws TransformerException {
        logger.trace("URIResolver(" + href + "," + base + ")");

        String uri = null;
        if (base == null) {
            try {
                URL url = new URL(href);
                uri = url.toURI().toASCIIString();
            } catch (MalformedURLException mue) {
                logger.trace("MalformedURLException on " + href);
            } catch (URISyntaxException use) {
                logger.trace("URISyntaxException on " + href);
            }
        } else {
            try {
                URI baseURI = new URI(base);
                uri = baseURI.resolve(href).toASCIIString();
            } catch (URISyntaxException use) {
                logger.trace("URISyntaxException resolving base and href: " + base + " : " + href);
            }
        }

        logger.trace("Resolved: " + uri);

        if (cache.containsKey(uri)) {
            logger.trace("Returning cached document.");
            return cache.get(uri).asSource();
        }

        if (uriResolver != null) {
            logger.trace("uriResolver.resolve(" + href + "," + base + ")");
            Source resolved = uriResolver.resolve(href, base);

            // FIXME: This is a grotesque hack. This is wrong. Wrong. Wrong.
            // To support caching, XMLResolver (xmlresolver.org) returns a Source even when it hasn't
            // found the resource. Problem is, it doesn't setup the entity resolver correctly for that
            // resource. So we hack at it here...
            if (resolved != null && resolved instanceof SAXSource) {
                SAXSource ssource = (SAXSource) resolved;
                XMLReader reader = ssource.getXMLReader();
                if (reader == null) {
                    try {
                        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                        saxParserFactory.setNamespaceAware(true); // Must be namespace aware
                        reader = saxParserFactory.newSAXParser().getXMLReader();
                        reader.setEntityResolver(this);
                        ssource.setXMLReader(reader);
                    } catch (SAXException se) {
                        // nop?
                    } catch (ParserConfigurationException pce) {
                        // nop?
                    }
                }
            }
            return resolved;
        }
        
        return null;
    }

    public XdmNode parse(String href, String base) {
        return parse(href, base, false);
    }

    public XdmNode parse(String href, String base, boolean dtdValidate) {
        Source source = null;
        href = URIUtils.encode(href);
        logger.trace("Attempting to parse: " + href + " (" + base + ")");

        try {
            source = resolve(href, base);
        } catch (TransformerException te) {
            throw XProcException.dynamicError(9, te);
        }

        if (source == null) {
            try {
                URI resURI; {
                    resURI = new URI(href);
                    if (!resURI.isAbsolute()) {
                        URI baseURI = new URI(base);
                        String path = baseURI.toASCIIString();
                        int pos = path.indexOf("!");
                        if (pos > 0 && (path.startsWith("jar:file:") || path.startsWith("jar:http:") || path.startsWith("jar:https:"))) {
                            // You can't resolve() against jar: scheme URIs because they appear to be opaque.
                            // I wonder if what follows is kosher...
                            String fakeURIstr = "http://example.com";
                            String subpath = path.substring(pos+1);
                            if (subpath.startsWith("/")) {
                                fakeURIstr += subpath;
                            } else {
                                fakeURIstr += "/" + subpath;
                            }
                            URI fakeURI = new URI(fakeURIstr);
                            resURI = fakeURI.resolve(href);
                            fakeURIstr = path.substring(0,pos+1) + resURI.getPath();
                            resURI = new URI(fakeURIstr);
                        } else {
                            resURI = baseURI.resolve(href);
                        }
                    }
                }

                source = new SAXSource(new InputSource(resURI.toASCIIString()));

                XMLReader reader = ((SAXSource) source).getXMLReader();
                if (reader == null) {
                    try {
                        reader = XMLReaderFactory.createXMLReader();
                        ((SAXSource) source).setXMLReader(reader);
                        reader.setEntityResolver(this);
                    } catch (SAXException se) {
                        // nop?
                    }
                }
            } catch (URISyntaxException use) {
                throw new XProcException(use);
            }
        }

        DocumentBuilder builder = runtime.getProcessor().newDocumentBuilder();
        builder.setDTDValidation(dtdValidate);
        builder.setLineNumbering(true);
        builder.setTreeModel(TreeModel.getTreeModel(runtime.getProcessor().getUnderlyingConfiguration().getTreeModel()));

        try {
            return builder.build(source);
        } catch (SaxonApiException sae) {
            String msg = sae.getMessage();
            if (msg.contains("validation")) {
                throw XProcException.stepError(27, sae);
            } else if (msg.contains("HTTP response code: 403 ")) {
                throw XProcException.dynamicError(21);
            } else {
                throw XProcException.dynamicError(11, sae);
            }
        }
    }

    public XdmNode parse(InputSource isource) {
        try {
            // Make sure the builder uses our entity resolver
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setEntityResolver(this);
            SAXSource source = new SAXSource(reader, isource);
            DocumentBuilder builder = runtime.getProcessor().newDocumentBuilder();
            builder.setLineNumbering(true);
            builder.setDTDValidation(false);
            builder.setTreeModel(TreeModel.getTreeModel(runtime.getProcessor().getUnderlyingConfiguration().getTreeModel()));
            return builder.build(source);
        } catch (SaxonApiException sae) {
            String msg = sae.getMessage();
            if (msg.contains("validation")) {
                throw XProcException.stepError(27, sae);
            } else if (msg.contains("HTTP response code: 403 ")) {
                throw XProcException.dynamicError(21);
            } else {
                throw XProcException.dynamicError(11, sae);
            }
        } catch (SAXException e) {
            throw new XProcException(e);
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        logger.trace("ResolveEntity(" + publicId + "," + systemId + ")");

        if (systemId == null) {
            return null;
        }

        try {
            URI baseURI = new URI(systemId);
            String uri = baseURI.toASCIIString();
            if (cache.containsKey(uri)) {
                logger.trace("Returning cached document.");
                return S9apiUtils.xdmToInputSource(runtime, cache.get(uri));
            }
        } catch (URISyntaxException use) {
            logger.trace("URISyntaxException resolving entityResolver systemId: " + systemId);
        } catch (SaxonApiException sae) {
            throw new XProcException(sae);
        }

        if (entityResolver != null) {
            InputSource r = entityResolver.resolveEntity(publicId, systemId);
            return r;
        } else {
            return null;
        }
    }

    @Override
    public StreamSource[] resolve(String moduleURI, String baseURI, String[] locations)
            throws XPathException {
        if (moduleURIResolver != null) {
            return moduleURIResolver.resolve(moduleURI, baseURI, locations);
        }
        ModuleURIResolver resolver = new StandardModuleURIResolver();
        return resolver.resolve(moduleURI, baseURI, locations);
    }

    @Override
    public Reader resolve(URI uri, String encoding, Configuration configuration) throws XPathException {
        if (unparsedTextResolver == null) {
            // If there's no resolver, let Saxon do it...
            unparsedTextResolver = new StandardUnparsedTextResolver();
        }

        return unparsedTextResolver.resolve(uri, encoding, configuration);
    }
}
