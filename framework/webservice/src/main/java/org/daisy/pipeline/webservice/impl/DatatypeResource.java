package org.daisy.pipeline.webservice.impl;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class DatatypeResource extends AdminResource {


        private static final Logger logger = LoggerFactory.getLogger(DatatypeResource.class);
        private Optional<DatatypeService> datatype;
        private String idParam;
        @Override
        public void doInit() {
                super.doInit();
                if (!isAuthorized()) {
                        return;
                }
                idParam = (String) getRequestAttributes().get("id");
                datatype= webservice().getDatatypeRegistry().getDatatype(idParam);
        }

        /**
         * Gets the resource.
         *
         * @return the resource
         */
        @Get("xml")
        public Representation getResource() {
                logRequest();
                if (!isAuthorized()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }

                if (!this.datatype.isPresent()) {
                        logger.info("Datatype not found: "+idParam);
                        setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                        return this.getErrorRepresentation("Datatype not found");       
                }

                setStatus(Status.SUCCESS_OK);
                DomRepresentation dom;
                try {
                        dom = new DomRepresentation(MediaType.APPLICATION_XML, datatype
                                        .get().asDocument());
                } catch (Exception e) {

                        setStatus(Status.SERVER_ERROR_INTERNAL);
                        return this.getErrorRepresentation(e.getMessage());       
                }
                logResponse(dom);
                return dom;
        }

}
