package org.daisy.pipeline.datatypes;

import java.net.URL;
import java.util.Map;

import org.daisy.common.file.URLs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

public class UrlBasedDatatypeService extends XMLBasedDatatypeService {

        private static final Logger logger = LoggerFactory.getLogger(UrlBasedDatatypeService.class);

        public static final String DATATYPE_URL= "data-type.url";
        public static final String DATATYPE_ID = "data-type.id";

        private URL url;

        public UrlBasedDatatypeService() {
                super();
        }

        public void activate(Map<?, ?> properties, Class<?> context) {
                if (properties.get(DATATYPE_ID) == null
                                || properties.get(DATATYPE_ID).toString().isEmpty()) {
                        throw new IllegalArgumentException(DATATYPE_ID
                                        + " property must not be empty");
                }
                if (properties.get(DATATYPE_URL) == null
                                || properties.get(DATATYPE_URL).toString().isEmpty()) {
                        throw new IllegalArgumentException(DATATYPE_URL
                                        + " property must not be empty");
                }
                String path = properties.get(DATATYPE_URL).toString();
                url = URLs.getResourceFromJAR(path, context);
                if (url == null)
                        throw new IllegalArgumentException("Resource at location " + path + " could not be found");
                String id = properties.get(DATATYPE_ID).toString();
                this.id = () -> id;
                logger.debug("Activating" + this.toString());
        }

        @Override
        public Document readDocument() throws Exception {
                return readDocument(url);
        }

        @Override
        public String toString() {
                return String.format("[DatatypeService #id=%s #url=%s ]", getId(), url.toString());
        }
}
