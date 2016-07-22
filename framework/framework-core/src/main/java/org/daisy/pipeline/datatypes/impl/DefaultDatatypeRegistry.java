package org.daisy.pipeline.datatypes.impl;

import java.util.Map;

import javax.xml.transform.URIResolver;

import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.script.impl.DefaultScriptRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public class DefaultDatatypeRegistry implements DatatypeRegistry {

        private static final Logger logger = LoggerFactory.getLogger(DefaultScriptRegistry.class);
        private Map<String,DatatypeService> registry=Maps.newHashMap();
        private URIResolver resolver;

        /**
         * Activate (OSGI).
         */
        public void activate(){
                logger.debug("Activating datatype registry");
        }

        @Override
        public Optional<DatatypeService> getDatatype(String id) {
                return Optional.fromNullable(this.registry.get(id));
        }

        @Override
        public Iterable<DatatypeService> getDatatypes() {
                return this.registry.values();
        }

        @Override
        public void register(DatatypeService service) {
                logger.debug("Registering "+service.toString());

                this.registry.put(service.getId(),service);
        }

        @Override
        public void unregister(DatatypeService service) {
                this.registry.remove(service);
        }

}
