package org.daisy.dotify.common.xml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Provides a builder for namespace contexts making it easy to provide namespace contexts
 * to for example xpath evaluations.
 *
 * @author Joel Håkansson
 */
public class NamespaceContextBuilder {
    private Map<String, String> uriForPrefix;
    private Map<String, Set<String>> prefixesForUri;

    /**
     * Creates a new instance.
     */
    public NamespaceContextBuilder() {
        this(null);
    }

    /**
     * Creates a new instance with the specified default namespace uri.
     *
     * @param defaultNsUri the default namespace uri
     */
    public NamespaceContextBuilder(String defaultNsUri) {
        uriForPrefix = new HashMap<>();
        prefixesForUri = new HashMap<>();
        add(XMLConstants.DEFAULT_NS_PREFIX, defaultNsUri == null ? XMLConstants.NULL_NS_URI : defaultNsUri);
    }

    /**
     * Adds a new namespace binding with the specified prefix and uri.
     *
     * @param prefix the namespace prefix
     * @param uri    the namespace uri
     * @return returns this builder
     */
    public NamespaceContextBuilder add(String prefix, String uri) {
        if (uriForPrefix.put(prefix, uri) != null) {
            throw new IllegalArgumentException("Prefix already bound: " + prefix);
        }
        ;
        Set<String> prefixes = prefixesForUri.get(uri);
        if (prefixes == null) {
            prefixesForUri.put(uri, new HashSet<>(Arrays.asList(new String[]{prefix})));
        } else {
            prefixes.add(prefix);
        }
        return this;
    }

    /**
     * Creates a new namespace context with the currently configured settings.
     *
     * @return returns a new namepsace context
     */
    public NamespaceContext build() {
        return new NamespaceContextImpl(this);
    }

    private static class NamespaceContextImpl implements NamespaceContext {
        private final Map<String, String> uriForPrefix;
        private final Map<String, Set<String>> prefixesForUri;

        private NamespaceContextImpl(NamespaceContextBuilder builder) {
            this.uriForPrefix = new HashMap<>(builder.uriForPrefix);
            this.prefixesForUri = new HashMap<>(builder.prefixesForUri);
        }

        @Override
        public String getNamespaceURI(String prefix) {
            throwExceptionIfNull(prefix);
            switch (prefix) {
                case XMLConstants.XML_NS_PREFIX:
                    return XMLConstants.XML_NS_URI;
                case XMLConstants.XMLNS_ATTRIBUTE:
                    return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                default:
                    return withDefault(uriForPrefix.get(prefix), XMLConstants.NULL_NS_URI);
            }
        }

        @Override
        public String getPrefix(String namespaceURI) {
            throwExceptionIfNull(namespaceURI);
            switch (namespaceURI) {
                case XMLConstants.XML_NS_URI:
                    return XMLConstants.XML_NS_PREFIX;
                case XMLConstants.XMLNS_ATTRIBUTE_NS_URI:
                    return XMLConstants.XMLNS_ATTRIBUTE;
                default:
                    return getFirstMatchOrNull(prefixesForUri.get(namespaceURI));
            }
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            throwExceptionIfNull(namespaceURI);
            switch (namespaceURI) {
                case XMLConstants.XML_NS_URI:
                    return toIterator(XMLConstants.XML_NS_PREFIX);
                case XMLConstants.XMLNS_ATTRIBUTE_NS_URI:
                    return toIterator(XMLConstants.XMLNS_ATTRIBUTE);
                default:
                    return toUnmodifiableIterator(prefixesForUri.get(namespaceURI));
            }
        }

        private static void throwExceptionIfNull(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Null not allowed.");
            }
        }

        private static <T> T withDefault(T value, T def) {
            return (value == null) ? def : value;
        }

        private static <T> T getFirstMatchOrNull(Set<T> set) {
            if (set != null && !set.isEmpty()) {
                return set.iterator().next();
            } else {
                return null;
            }
        }

        private static <T> Iterator<T> toUnmodifiableIterator(Set<T> set) {
            if (set != null && !set.isEmpty()) {
                return Collections.unmodifiableSet(set).iterator();
            } else {
                return Collections.emptyListIterator();
            }
        }

        @SafeVarargs
        private static <T> Iterator<T> toIterator(T... value) {
            return Arrays.asList(value).iterator();
        }

    }
}
