package org.daisy.dotify.common.xml;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

class TransformerTools {

    static Source toSource(Object source) throws XMLToolsException {
        if (source instanceof File) {
            return new StreamSource((File) source);
        } else if (source instanceof String) {
            return new StreamSource((String) source);
        } else if (source instanceof URL) {
            try {
                // Compare to {@link StreamSource#StreamSource(File)}
                return new StreamSource(((URL) source).toURI().toASCIIString());
            } catch (URISyntaxException e) {
                throw new XMLToolsException(e);
            }
        } else if (source instanceof URI) {
            return new StreamSource(((URI) source).toASCIIString());
        } else if (source instanceof Source) {
            return (Source) source;
        } else {
            throw new XMLToolsException("Failed to create source: " + source);
        }
    }

    static Result toResult(Object result) throws XMLToolsException {
        if (result instanceof File) {
            return new StreamResult((File) result);
        } else if (result instanceof OutputStream) {
            return new StreamResult((OutputStream) result);
        } else if (result instanceof String) {
            return new StreamResult((String) result);
        } else if (result instanceof URL) {
            try {
                return new StreamResult(((URL) result).toURI().toASCIIString());
            } catch (URISyntaxException e) {
                throw new XMLToolsException(e);
            }
        } else if (result instanceof URI) {
            return new StreamResult(((URI) result).toASCIIString());
        } else if (result instanceof Result) {
            return (Result) result;
        } else {
            throw new XMLToolsException("Failed to create result: " + result);
        }
    }
}
