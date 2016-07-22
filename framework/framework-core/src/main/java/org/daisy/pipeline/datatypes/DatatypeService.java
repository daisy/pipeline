package org.daisy.pipeline.datatypes;

import org.w3c.dom.Document;

public interface DatatypeService {
        
        /**
         * Gets the datatype ID.
         *
         * @return the id
         */
        public String getId();
        public Document asDocument() throws Exception;
        public ValidationResult validate(String content);

}


