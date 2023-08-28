package org.daisy.pipeline.webservice.restlet.impl;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.xml.DatatypesXmlWriter;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatatypesResource extends AuthenticatedResource {

        private static final Logger logger = LoggerFactory.getLogger(DatatypeResource.class);
        @Override
        public void doInit() {
                super.doInit();
                if (!isAuthenticated()) {
                        return;
                }
        }

        /**
         * Gets the resource.
         *
         * @return the resource
         */
        @Get("xml")
        public Representation getResource() {
                logRequest();
                maybeEnableCORS();
                if (!isAuthenticated()) {
                        setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                        return null;
                }

                setStatus(Status.SUCCESS_OK);
                DomRepresentation dom;
                Iterable<DatatypeService> datatypes = getDatatypeRegistry().getDatatypes();
                try {
                        dom = new DomRepresentation(MediaType.APPLICATION_XML,
                                                    new DatatypesXmlWriter(datatypes,
                                                                           getRequest().getRootRef().toString())
                                                        .getXmlDocument());
                } catch (Exception e) {

                        setStatus(Status.SERVER_ERROR_INTERNAL);
                        return this.getErrorRepresentation(e.getMessage());       
                }
                logResponse(dom);
                return dom;
        }

}