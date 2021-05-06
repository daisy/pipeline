package org.daisy.braille.utils.pef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Provides a file comparator for PEF.
 *
 * @author Joel Håkansson
 */
public class PEFFileCompare {
    private static final Logger logger = Logger.getLogger(PEFFileCompare.class.getCanonicalName());

    private static final Supplier<InputStream> def = () -> fromPath("resource-files/strip-meta.xsl");
    private final TransformerFactory factory;
    private final Templates templates;
    private int pos = -1;

    /**
     * Creates a new pef file comparator.
     */
    public PEFFileCompare() {
        this(def);
    }

    /**
     * Creates a new comparator with the specified normalization resource.
     *
     * @param nr the normalization resource
     * @deprecated
     */
    @Deprecated
    public PEFFileCompare(NormalizationResource nr) {
        this((Supplier<InputStream>) () -> nr.getNormalizationResourceAsStream());
    }

    /**
     * Creates a new comparator with the specified normalization resource.
     *
     * @param nr the normalization resource
     */
    public PEFFileCompare(Supplier<InputStream> nr) {
        this.factory = TransformerFactory.newInstance();
        try {
            this.factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
        } catch (IllegalArgumentException iae) {
            logger.throwing("PEFFileCompare", iae.getMessage(), iae);
        }
        this.templates = init(factory, new StreamSource(nr.get()));
    }

    private static Templates init(TransformerFactory factory, Source xslt) {
        try {
            return factory.newTemplates(xslt);
        } catch (TransformerConfigurationException e) {
            return null;
        }
    }

    /**
     * Creates a new comparator with the specified normalization path.
     *
     * @param path the path to the normalization resource
     */
    public PEFFileCompare(String path) {
        this((Supplier<InputStream>) () -> fromPath(path));
    }

    /**
     * Creates a new comparator with the specified normalization url.
     *
     * @param nr the url to the normalization resource
     */
    public PEFFileCompare(URL nr) {
        this((Supplier<InputStream>) () -> fromURL(nr));
    }

    /**
     * Compares the two files.
     *
     * @param f1 the first file
     * @param f2 the second file
     * @return returns true if the files are equal, false otherwise
     * @throws PEFFileCompareException if comparison fails
     */
    public boolean compare(File f1, File f2) throws PEFFileCompareException {
        return compare(new StreamSource(f1), new StreamSource(f2));
    }

    /**
     * Compares two stream sources.
     *
     * @param xml1 the first source
     * @param xml2 the second source
     * @return returns true if the files are equal, false otherwise
     * @throws PEFFileCompareException if comarison fails
     */
    public boolean compare(StreamSource xml1, StreamSource xml2) throws PEFFileCompareException {
        if (templates == null) {
            throw new PEFFileCompareException("No template.");
        }
        pos = -1;

        try {
            File t1 = File.createTempFile("FileCompare", ".tmp");
            t1.deleteOnExit();
            File t2 = File.createTempFile("FileCompare", ".tmp");
            t2.deleteOnExit();

            try {

                templates.newTransformer().transform(xml1, new StreamResult(t1));
                templates.newTransformer().transform(xml2, new StreamResult(t2));

                XMLFileCompare fc = new XMLFileCompare(factory);
                boolean ret = fc.compareXML(new FileInputStream(t1), new FileInputStream(t2));
                pos = fc.getPos();
                return ret;
            } catch (TransformerConfigurationException e) {
                throw new PEFFileCompareException(e);
            } catch (TransformerException e) {
                throw new PEFFileCompareException(e);
            } catch (IOException e) {
                throw new PEFFileCompareException(e);
            } finally {
                if (!t1.delete()) {
                    logger.warning("Delete failed");
                }
                if (!t2.delete()) {
                    logger.warning("Delete failed");
                }
            }
        } catch (IOException e) {
            throw new PEFFileCompareException("Failed to create temp files.", e);
        }
    }

    /**
     * Gets the byte position of the first difference.
     *
     * @return returns the position of the first failure
     */
    public int getPos() {
        return pos;
    }

    private static InputStream fromURL(URL url) {
        try {
            return url.openStream();
        } catch (IOException e) {
            return null;
        }
    }

    private static InputStream fromPath(String path) {
        return PEFFileCompare.class.getResourceAsStream(path);
    }

}
