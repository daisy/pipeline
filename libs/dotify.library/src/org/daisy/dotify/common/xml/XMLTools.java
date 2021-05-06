package org.daisy.dotify.common.xml;

import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;

/**
 * Provides some xml tools.
 *
 * @author Joel Håkansson
 */
public class XMLTools {
    private static final Logger logger = Logger.getLogger(XMLTools.class.getCanonicalName());

    static final Pattern XML_DECL = Pattern.compile(
            "\\A\uFEFF?\\s*<\\?xml[^>]*?encoding\\s*=\\s*[\"'](?<ENCODING>[^'\"]*)[\"'].*\\?>"
    );
    private static final Optional<Charset> UTF_32_BE = forName("UTF-32BE");
    private static final Optional<Charset> UTF_32_LE = forName("UTF-32LE");
    private static final Optional<Charset> IBM_500 = forName("IBM500");

    // With bom
    private static final byte[] USC_4_BE = new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF};
    private static final byte[] USC_4_LE = new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x00};
    private static final byte[] USC_4_2143 = new byte[]{0x00, 0x00, (byte) 0xFF, (byte) 0xFE};
    private static final byte[] USC_4_3412 = new byte[]{(byte) 0xFE, (byte) 0xFF, 0x00, 0x00};

    // NO bom
    private static final byte[] UTF_16_BE = new byte[]{0x00, (byte) 0x3C, 0x00, (byte) 0x3F};
    private static final byte[] UTF_16_LE = new byte[]{(byte) 0x3C, 0x00, (byte) 0x3F, 0x00};
    private static final byte[] UTF_8 = new byte[]{(byte) 0x3C, (byte) 0x3F, 0x78, 0x6D};
    private static final byte[] EBCDIC = new byte[]{(byte) 0x4C, (byte) 0x6F, (byte) 0xA7, (byte) 0x94};

    private XMLTools() {
    }

    static Optional<String> getDeclaredEncoding(
            byte[] data,
            Charset preliminaryEncoding
    ) throws XmlEncodingDetectionException {
        try (Reader r = new InputStreamReader(new ByteArrayInputStream(data), preliminaryEncoding)) {
            StringBuilder sb = new StringBuilder();
            int c = r.read();
            if (c == '\uFEFF') {
                c = -2;
            }
            // Append BOM or any whitespace characters
            while (c == -2 || Character.isWhitespace((int) c)) {
                sb.append((char) c);
                c = r.read();
            }
            // Read the next 5 characters to determine if an XML declaration is present
            for (int i = 0; i < 5 && c != -1; i++) {
                sb.append((char) c);
                c = r.read();
            }
            boolean closing = false;
            if (sb.length() >= 5 && "<?xml".equals(sb.substring(sb.length() - 5))) {
                while (c != -1 && sb.length() < 4000) {
                    // give up after 4000 characters (this should only happen if the file is broken, but
                    // technically it is possible to have a longer xml-declaration than that)
                    sb.append((char) c);
                    c = r.read();
                    // The xml declaration never contains ? or >.
                    if (c == '?') {
                        closing = true;
                    } else if (c == '>' && closing) {
                        sb.append((char) c);
                        break;
                    } else {
                        closing = false;
                    }
                }
                return getDeclaredEncoding(sb.toString());
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new XmlEncodingDetectionException("Failed to read.", e);
        }
    }

    /**
     * Gets the declared encoding from the given string. If the string
     * doesn't start with an XML declaration, an empty optional is returned.
     *
     * @param text the xml
     * @return returns a string with the declared encoding
     */
    public static Optional<String> getDeclaredEncoding(String text) {
        Matcher m = XML_DECL.matcher(text);
        String enc;
        if (m.find() && (enc = m.group("ENCODING")) != null) {
            return Optional.of(enc);
        }
        return Optional.empty();
    }

    /**
     * Detects XML encoding based on this algorithm:
     * <a href="https://www.w3.org/TR/xml/#sec-guessing">https://www.w3.org/TR/xml/#sec-guessing</a>.
     * In accordance with this specification, it is assumed that the XML declaration
     * is not preceded by whitespace (if present).
     * Note that some encodings mentioned in the specification are not supported
     * because they are not supported by the JVM.
     *
     * @param data the input bytes
     * @return returns the name of the detected charset
     * @throws IllegalArgumentException      if the length of the data is less than 4 bytes
     * @throws XmlEncodingMismatchException  if the declared encoding doesn't match the detected encoding and
     *                                       the detected encoding is an exact match
     * @throws XmlEncodingDetectionException if detection fails
     */
    public static String detectXmlEncoding(byte[] data) throws XmlEncodingDetectionException {
        if (data.length < 4) {
            throw new IllegalArgumentException();
        }
        PreliminaryCharset preliminary = guessCharset(data);
        if (preliminary == null) {
            throw new XmlEncodingDetectionException("Could not detect encoding.");
        }
        Optional<String> specifiedEncoding = getDeclaredEncoding(data, preliminary.getCharset());
        if (specifiedEncoding.isPresent()) {
            String returnEncoding = specifiedEncoding.get();
            if (preliminary.isExactMatch()) {
                if (!preliminary.getCharset().name().toUpperCase().startsWith(returnEncoding.toUpperCase())) {
                    String msg = MessageFormat.format(
                            "The specified encoding ({0}) doesn''t match detected encoding ({1}).",
                            returnEncoding,
                            preliminary.getCharset().name()
                    );
                    throw new XmlEncodingMismatchException(msg, preliminary.getCharset().name(), returnEncoding);
                }
                return preliminary.getCharset().name();
            } else {
                return returnEncoding;
            }
        } else if (preliminary.isExactMatch()) {
            return preliminary.getCharset().name();
        } else {
            throw new XmlEncodingDetectionException("Could not detect encoding.");
        }
    }

    /**
     * Tries to detect a Unicode encoding from the supplied data based
     * on the presence of a BOM. If the file doesn't start with a BOM,
     * an empty optional is returned.
     *
     * @param data the data to detect encoding on
     * @return returns the encoding detected from the BOM
     * @throws UnsupportedCharsetException if the charset could be detected but not created
     */
    public static Optional<Charset> detectBomEncoding(byte[] data) {
        return Optional.ofNullable(guessCharsetFromBom(data.length > 4 ? Arrays.copyOf(data, 4) : data));
    }

    /**
     * Finds group of encodings that can be used to decode the declaration (if any).
     *
     * @param data the data
     * @return returns a preliminary charset, based on the first bytes of the file
     * @throws IllegalArgumentException if the length of the data is less than 4 bytes
     */
    private static PreliminaryCharset guessCharset(byte[] data) {
        // Based on https://www.w3.org/TR/xml/#sec-guessing
        if (data.length < 4) {
            throw new IllegalArgumentException();
        }
        byte[] signature = Arrays.copyOf(data, 4);
        int i;
        // With BOM
        Charset charsetFromBom;
        try {
            charsetFromBom = guessCharsetFromBom(signature);
        } catch (UnsupportedCharsetException e) {
            return null;
        }
        if (charsetFromBom != null) {
            return new PreliminaryCharset.Builder(charsetFromBom).bom(true).exactMatch(true).build();
        } else if ((i = detectUcs4WithoutBom(signature)) > -1) { // No BOM
            if (i == 1) {
                //BE
                return UTF_32_BE.map(
                        v -> new PreliminaryCharset.Builder(v).bom(false).exactMatch(false).build()
                ).orElse(null);
            } else if (i == 3) {
                //LE
                return UTF_32_LE.map(
                        v -> new PreliminaryCharset.Builder(v).bom(false).exactMatch(false).build()
                ).orElse(null);
            } else {
                // not supported
                return null;
            }
        } else if (Arrays.equals(signature, UTF_16_BE)) {
            // UTF-16, big endian
            return new PreliminaryCharset.Builder(StandardCharsets.UTF_16BE).bom(false).exactMatch(false).build();
        } else if (Arrays.equals(signature, UTF_16_LE)) {
            // UTF-16, little endian
            return new PreliminaryCharset.Builder(StandardCharsets.UTF_16LE).bom(false).exactMatch(false).build();
        } else if (Arrays.equals(signature, UTF_8)) {
            // UTF-8 no BOM
            return new PreliminaryCharset.Builder(StandardCharsets.UTF_8).bom(false).exactMatch(false).build();
        } else if (Arrays.equals(signature, EBCDIC)) {
            return IBM_500.map(
                    v -> new PreliminaryCharset.Builder(v).bom(false).exactMatch(false).build()
            ).orElse(null);
        }
        // UTF-8 without encoding declaration or corrupt
        return new PreliminaryCharset.Builder(StandardCharsets.UTF_8).bom(false).exactMatch(true).build();
    }

    /**
     * Guess the charset from a BOM.
     *
     * @param signature a byte signature, 0-4 bytes long
     * @return returns the charset if detected or null if the charset could not be detected.
     * @throws UnsupportedCharsetException if the charset could be detected but not created
     */
    private static Charset guessCharsetFromBom(byte[] signature) throws UnsupportedCharsetException {
        if (signature.length < 2) {
            // No Unicode encoding has a byte order mark < 2 bytes
            return null;
        } else if (Arrays.equals(signature, USC_4_BE)) {
            return UTF_32_BE.orElseThrow(() -> new UnsupportedCharsetException("UTF-32BE"));
        } else if (Arrays.equals(signature, USC_4_LE)) {
            // Note that this test must come before UTF-16 below
            return UTF_32_LE.orElseThrow(() -> new UnsupportedCharsetException("UTF-32LE"));
        } else if (Arrays.equals(signature, USC_4_2143)) {
            // Not supported by the JVM
            throw new UnsupportedCharsetException("USC-4-2143");
        } else if (Arrays.equals(signature, USC_4_3412)) {
            // Note that this test must come before UTF-16 below
            // Not supported by the JVM
            throw new UnsupportedCharsetException("USC-4-3412");
        } else if (signature[0] == (byte) 0xFE && signature[1] == (byte) 0xFF) {
            // UTF-16, big endian
            return StandardCharsets.UTF_16BE;
        } else if (signature[0] == (byte) 0xFF && signature[1] == (byte) 0xFE) {
            // UTF-16, little endian
            return StandardCharsets.UTF_16LE;
        } else if (
            signature.length > 2 &&
            signature[0] == (byte) 0xEF &&
            signature[1] == (byte) 0xBB &&
            signature[2] == (byte) 0xBF
        ) {
            // UTF-8 with BOM
            return StandardCharsets.UTF_8;
        } else {
            return null;
        }
    }

    /**
     * Detects if the supplied data is XML encoded with UCS4 without BOM.
     * Returns the index of the non-zero byte, or -1 if the data isn't
     * a match for UCS4 encoded XML.
     *
     * @param data the input data
     * @return returns the non-zero byte
     */
    private static int detectUcs4WithoutBom(byte[] data) {
        if (data.length != 4) {
            throw new IllegalArgumentException("Expected 4 bytes");
        }
        int seen = -1;
        int i;
        for (i = 0; i < data.length; i++) {
            if (data[i] == 0x3C) {
                if (seen == -1) {
                    seen = i;
                } else {
                    return -1;
                }
            } else if (data[i] != 0x00) {
                return -1;
            }
        }
        return i;
    }

    private static Optional<Charset> forName(String charset) {
        try {
            return Optional.of(Charset.forName(charset));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * <p>Transforms the xml with the specified parameters. By default, this method will set up a caching
     * entity resolver, which will reduce the amount of fetching of dtd's from the Internet.</p>
     *
     * <p>This method will attempt to create Source and Result objects from the supplied source, result and xslt
     * objects. This process supports several types of objects from which Sources and Results are typically created,
     * such as files, strings and URLs.</p>
     *
     * <p>This method will create its own instance of a transformer factory.</p>
     *
     * @param source the source xml
     * @param result the result xml
     * @param xslt   the xslt
     * @param params xslt parameters
     * @throws XMLToolsException if the transformation is unsuccessful
     */
    public static void transform(
        Object source,
        Object result,
        Object xslt,
        Map<String, Object> params
    ) throws XMLToolsException {
        transform(
            TransformerTools.toSource(source),
            TransformerTools.toResult(result),
            TransformerTools.toSource(xslt),
            params
        );
    }

    /**
     * <p>Transforms the xml with the specified parameters. By default, this method will set up a caching entity
     * resolver, which will reduce the amount of fetching of dtd's from the Internet.</p>
     *
     * <p>This method will attempt to create Source and Result objects from the supplied source, result and xslt
     * objects. This process supports several types of objects from which Sources and Results are typically created,
     * such as files, strings and URLs.</p>
     *
     * @param source  the source xml
     * @param result  the result xml
     * @param xslt    the xslt
     * @param params  xslt parameters
     * @param factory the transformer factory
     * @throws XMLToolsException if the transformation is unsuccessful
     */
    public static void transform(
        Object source,
        Object result,
        Object xslt,
        Map<String, Object> params,
        TransformerFactory factory
    ) throws XMLToolsException {
        transform(
            TransformerTools.toSource(source),
            TransformerTools.toResult(result),
            TransformerTools.toSource(xslt),
            params,
            factory
        );
    }

    /**
     * Transforms the xml with the specified parameters. By default, this method will set up a caching entity
     * resolver, which will reduce the amount of fetching of dtd's from the Internet.
     *
     * <p>This method will create its own instance of a transformer factory.</p>
     *
     * @param source the source xml
     * @param result the result xml
     * @param xslt   the xslt
     * @param params xslt parameters
     * @throws XMLToolsException if the transformation is unsuccessful
     */
    public static void transform(
        Source source,
        Result result,
        Source xslt,
        Map<String, Object> params
    ) throws XMLToolsException {
        transform(source, result, xslt, params, TransformerFactory.newInstance());
    }

    /**
     * <p>Transforms the xml with the specified parameters. By default, this method will set up a caching entity
     * resolver, which will reduce the amount of fetching of dtd's from the Internet.</p>
     *
     * @param source  the source xml
     * @param result  the result xml
     * @param xslt    the xslt
     * @param params  xslt parameters
     * @param factory the transformer factory
     * @throws XMLToolsException if the transformation is unsuccessful
     */
    public static void transform(
        Source source,
        Result result,
        Source xslt,
        Map<String, Object> params,
        TransformerFactory factory
    ) throws XMLToolsException {
        transform(
            source,
            result,
            xslt,
            TransformerEnvironment.builder().transformerFactory(factory).parameters(params).build()
        );
    }

    /**
     * <p>Transforms the xml with the specified parameters. By default, this method will set up a caching entity
     * resolver, which will reduce the amount of fetching of dtd's from the Internet.</p>
     *
     * @param source the source xml
     * @param result the result xml
     * @param xslt   the xslt
     * @param env    the transformer environment
     * @param <T>    the type of exception thrown
     * @throws T if the transformation is unsuccessful
     */
    public static <T extends Exception> void transform(
        Object source,
        Object result,
        Object xslt,
        TransformerEnvironment<T> env
    ) throws T {
        transform(
            env.asSource(source),
            env.asResult(result),
            env.asSource(xslt),
            env
        );
    }

    /**
     * <p>Transforms the xml with the specified parameters. By default, this method will set up a caching entity
     * resolver, which will reduce the amount of fetching of dtd's from the Internet.</p>
     *
     * @param source the source xml
     * @param result the result xml
     * @param xslt   the xslt
     * @param env    the transformer environment
     * @param <T>    the type of exception thrown
     * @throws T if the transformation is unsuccessful
     */
    public static <T extends Exception> void transform(
        Source source,
        Result result,
        Source xslt,
        TransformerEnvironment<T> env
    ) throws T {
        Transformer transformer = env.newTransformer(xslt);

        for (String name : env.getParameters().keySet()) {
            transformer.setParameter(name, env.getParameters().get(name));
        }

        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        try {
            transformer.setURIResolver(new CachingURIResolver(parserFactory));
        } catch (XMLToolsException e) {
            env.toThrowable(e);
        }
        //Create a SAXSource, hook up an entityresolver
        if (source.getSystemId() != null && source.getSystemId().length() > 0) {
            try {
                if (source instanceof SAXSource) {
                    transformer.transform(setEntityResolver((SAXSource) source), result);
                } else {
                    SAXParser parser = parserFactory.newSAXParser();
                    parser.getXMLReader().setFeature("http://xml.org/sax/features/validation", false);
                    try (InputStream is = new URLCache().openStream(new URI(source.getSystemId()).toURL())) {
                        InputSource isource = new InputSource(is);
                        isource.setSystemId(source.getSystemId());
                        SAXSource saxSource = new SAXSource(parser.getXMLReader(), isource);
                        saxSource.setSystemId(source.getSystemId());
                        transformer.transform(setEntityResolver(saxSource), result);
                    }
                }
            } catch (TransformerException e) {
                throw env.toThrowable(e);
            } catch (Exception e) {
                logger.throwing(XMLTools.class.getCanonicalName(), "transform", e);
            }
        } else {
            throw env.toThrowable(new XMLToolsException(
                "No system id on source, see https://github.com/brailleapps/dotify.common/issues/4."
            ));
        }
    }

    private static SAXSource setEntityResolver(SAXSource source) {
        if (source.getXMLReader().getEntityResolver() == null) {
            source.getXMLReader().setEntityResolver(new EntityResolverCache());
        }
        return source;
    }

    /**
     * Returns true if the specified file is well formed XML.
     *
     * @param f the file
     * @return returns true if the file is well formed XML, false otherwise
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final boolean isWellformedXML(File f) throws XMLToolsException {
        return parseXML(f) != null;
    }

    /**
     * Returns true if the contents at the specified URI is well formed XML.
     *
     * @param uri the URI
     * @return returns true if the contents at the specified URI is well formed XML, false otherwise
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final boolean isWellformedXML(URI uri) throws XMLToolsException {
        return parseXML(uri) != null;
    }

    /**
     * Returns true if the specified source is well formed XML.
     *
     * @param source the source
     * @return returns true if the source is well formed XML, false otherwise
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final boolean isWellformedXML(InputSource source) throws XMLToolsException {
        return parseXML(source) != null;
    }

    /**
     * Asserts that the specified file is well formed and returns some root node information.
     *
     * @param f the file
     * @return returns the root node, or null if file is not well formed
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final XMLInfo parseXML(File f) throws XMLToolsException {
        return parseXML(f, false);
    }

    /**
     * Asserts that the contents at the specified URI is well formed and returns some root node information.
     *
     * @param uri the URI
     * @return returns the root node, or null if the contents at the specified URI is not well formed
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final XMLInfo parseXML(URI uri) throws XMLToolsException {
        return parseXML(uri, false);
    }

    /**
     * Asserts that the source is well formed and returns some root node information.
     *
     * @param source the source
     * @return returns the root node, or null if the source is not well formed
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final XMLInfo parseXML(InputSource source) throws XMLToolsException {
        return parseXML(source, false);
    }

    /**
     * Returns some root node information and optionally asserts that the specified
     * file is well formed.
     *
     * @param f    the file
     * @param peek true if the parsing should stop after reading the root element. If true,
     *             the file may or may not be well formed beyond the first start tag.
     * @return returns the root node, or null if file is not well formed
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final XMLInfo parseXML(File f, boolean peek) throws XMLToolsException {
        return parseXML(f.toURI(), peek);
    }

    /**
     * Returns some root node information and optionally asserts that the contents at the
     * specified URI is well formed.
     *
     * @param uri  the URI
     * @param peek true if the parsing should stop after reading the root element. If true,
     *             the contents at the specified URI may or may not be well formed beyond the first start tag.
     * @return returns the root node, or null if file is not well formed
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final XMLInfo parseXML(URI uri, boolean peek) throws XMLToolsException {
        try (InputStream is = uri.toURL().openStream()) {
            InputSource source = new InputSource(is);
            source.setSystemId(uri.toASCIIString());
            return parseXML(source, peek);
        } catch (IOException e) {
            throw new XMLToolsException(e);
        }
    }

    /**
     * Returns some root node information and optionally asserts that the contents at the
     * specified source is well formed.
     *
     * @param source the source
     * @param peek   true if the parsing should stop after reading the root element. If true,
     *               the source may or may not be well formed beyond the first start tag.
     * @return returns the root node, or null if file is not well formed
     * @throws XMLToolsException if a parser cannot be configured or if parsing fails
     */
    public static final XMLInfo parseXML(InputSource source, boolean peek) throws XMLToolsException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser saxParser = null;
        try {
            saxParser = factory.newSAXParser();
        } catch (ParserConfigurationException e) {
            throw new XMLToolsException("Failed to set up XML parser.", e);
        } catch (SAXException e) {
            throw new XMLToolsException("Failed to set up XML parser.", e);
        }
        XMLHandler dh = new XMLHandler(peek);
        try {
            XMLReader reader = saxParser.getXMLReader();
            if (dh != null) {
                reader.setContentHandler(dh);
                reader.setEntityResolver(dh);
                //since we sometimes have loadDTD turned off,
                //we use lexical handler to get the pub and sys id of prolog
                reader.setProperty("http://xml.org/sax/properties/lexical-handler", dh);
                reader.setErrorHandler(dh);
                reader.setDTDHandler(dh);
            }
            saxParser.getXMLReader().parse(source);
        } catch (StopParsing e) {
            //thrown if peek is true
        } catch (SAXException e) {
            return null;
        } catch (IOException e) {
            throw new XMLToolsException(e);
        }
        return dh.root;
    }

    private static class XMLHandler extends DefaultHandler implements LexicalHandler {
        private final EntityResolver resolver;
        private final boolean peek;
        private final XMLInfo.Builder builder;
        private XMLInfo root = null;

        XMLHandler(boolean peek) {
            this.resolver = new EntityResolverCache();
            this.peek = peek;
            this.builder = new XMLInfo.Builder();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {
            if (this.root == null) {
                this.root = builder.uri(uri).localName(localName).qName(qName).attributes(attributes).build();
                if (peek) {
                    throw new StopParsing();
                }
            }
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            if (root == null) {
                //set prolog entity in builder
                builder.publicId(publicId);
                builder.systemId(systemId);
            }
            return resolver.resolveEntity(publicId, systemId);
        }

        @Override
        public void startDTD(String name, String publicId, String systemId) throws SAXException {
            builder.publicId(publicId);
            builder.systemId(systemId);
        }

        @Override
        public void endDTD() throws SAXException {
            // no-op
        }

        @Override
        public void startEntity(String name) throws SAXException {
            // no-op
        }

        @Override
        public void endEntity(String name) throws SAXException {
            // no-op
        }

        @Override
        public void startCDATA() throws SAXException {
            // no-op
        }

        @Override
        public void endCDATA() throws SAXException {
            // no-op
        }

        @Override
        public void comment(char[] ch, int start, int length)
                throws SAXException {
            // no-op
        }
    }

    private static class StopParsing extends SAXException {

        private static final long serialVersionUID = -4335028194855324300L;

    }

}
