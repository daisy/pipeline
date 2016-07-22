package org.daisy.pipeline.webserviceutils.xml;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.webserviceutils.Routes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DatatypesXmlWriter {
        private Iterable<DatatypeService> datatypes;
        private static final Logger logger = LoggerFactory.getLogger(DatatypesXmlWriter.class);

        /**
         * @param datatypes
         */
        public DatatypesXmlWriter(Iterable<DatatypeService> datatypes) {
                this.datatypes = datatypes;
        }
        
        public Document getXmlDocument(){
                if (this.datatypes== null) {
                        logger.warn("Null datatypes, couldn't create xml");
                        return null;
                }
                return this.buildXml();
        }
        private Document buildXml(){

		String baseUri = new Routes().getBaseUri();
		Document doc = XmlUtils.createDom("datatypes");
		Element datatypesElem= doc.getDocumentElement();
		datatypesElem.setAttribute("href", baseUri + Routes.DATATYPES_ROUTE);
                for (DatatypeService ds : this.datatypes) {
                        Element dsElem=doc.createElementNS(XmlUtils.NS_PIPELINE_DATA,"datatype");
                        dsElem.setAttribute("id",ds.getId());
                        dsElem.setAttribute("href",String.format("%s%s/%s",baseUri,Routes.DATATYPES_ROUTE,ds.getId()));
                        datatypesElem.appendChild(dsElem);
                }

		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.DATATYPES_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}
                return doc;
        }
}
