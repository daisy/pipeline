package org.daisy.pipeline.datatypes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class UrlBasedDatatypeService implements DatatypeService{
        
        private static final Logger logger = LoggerFactory.getLogger(UrlBasedDatatypeService.class);
        /** The Constant SCRIPT_URL. */
        public static final String DATATYPE_URL= "data-type.url";


        /** The Constant SCRIPT_ID. */
        public static final String DATATYPE_ID = "data-type.id";

        private String id;
        private URI uri;
        private URIResolver resolver;

        public void activate(Map<?, ?> properties) {
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
                try {
                        uri = new URI(properties.get(DATATYPE_URL).toString());
                } catch (URISyntaxException e) {
                        throw new IllegalArgumentException(DATATYPE_URL
                                        + " property must not be a legal URI");
                }
                id = properties.get(DATATYPE_ID).toString();
                
                logger.debug("Activating"+this.toString());

        }

        public String getId() {
                return this.id;
        }

        /**
         * Gets the datatype uri.
         *
         * @return the id
         */
        public URI getURI() {
                return this.uri;
        }

        public Document asDocument() throws Exception {
                Source src=this.resolver.resolve(this.uri.toString(),"");
                if (src==null){
                        throw new RuntimeException(String.format("Uri not found when resolving datatype %s",this.uri));
                }
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(src.getSystemId());
                return document;
        }

        @Override
        public String toString() {
                // TODO Auto-generated method stub
                return String.format("[DatatypeService #id=%s #uri=%s ]",this.id,this.uri.toString());
        }

        public void setUriResolver(URIResolver resolver) {
                this.resolver=resolver;

        }

        @Override
        public ValidationResult validate(String content) {
                return ValidationResult.notValid("Not implemented");
        }
}
