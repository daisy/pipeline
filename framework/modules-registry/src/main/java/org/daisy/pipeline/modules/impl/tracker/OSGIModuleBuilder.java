package org.daisy.pipeline.modules.impl.tracker;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.modules.Component;
import org.daisy.pipeline.modules.Entity;
import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ResourceLoader;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

public class OSGIModuleBuilder {

        private ResourceLoader loader;
        private String name;
        private String version;
        private String title;
        private final List<Component> components = new ArrayList<Component>();
        private final List<Entity> entities = new ArrayList<Entity>();
        private final Logger mLogger = LoggerFactory.getLogger(getClass());

        public Module build() {
                return new Module(name, version, title, components, entities);
        }

        public OSGIModuleBuilder withName(String name) {
                this.name = name;
                return this;
        }

        public OSGIModuleBuilder withLoader(ResourceLoader loader) {
                this.loader = loader;
                return this;
        }

        public OSGIModuleBuilder withVersion(String version) {
                this.version = version;
                return this;
        }

        public OSGIModuleBuilder withTitle(String title) {
                this.title = title;
                return this;
        }

        public OSGIModuleBuilder withComponents(
                        Collection<? extends Component> components) {
                this.components.addAll(components);
                return this;
        }

        public OSGIModuleBuilder withComponent(URI uri, String path) {
                mLogger.trace("withComponent:" + uri.toString() + ", path: " + path);
                components.add(new Component(uri, path, loader));
                return this;
        }

        public OSGIModuleBuilder withBundle(final Bundle bundle) {
                String title = bundle.getSymbolicName();
                String version = bundle.getVersion().toString();
                String name = bundle.getHeaders().get("Bundle-Name").toString();
                withVersion(version);
                withName(name);
                withTitle(title);
                withLoader(new ResourceLoader() {

                        @Override
                        public URL loadResource(String path) {
                                //catalog is placed on the meta-inf folder, all the paths are relative to it
                                //url getResource or getEntry does not support relative paths then get rid of the starting ../
                                URL url = bundle.getResource(path.replace("../", ""));
                                return url;
                        }

                        @Override
                        public Iterable<URL> loadResources(final String path) {
                                return new Iterable<URL>() {
                                        @Override
                                        public Iterator<URL> iterator() {
                                                return Iterators.forEnumeration(bundle.findEntries(
                                                                path.replace("../", ""), "*", true));
                                        }
                                };
                        }

                });
                return this;
        }

        public OSGIModuleBuilder withCatalog(XmlCatalog catalog) {
                for (Map.Entry<URI, URI> entry : catalog.getSystemIdMappings()
                                .entrySet()) {
                        withComponent(entry.getKey(), entry.getValue().toString());
                }
                for (Map.Entry<URI, URI> entry : catalog.getUriMappings().entrySet()) {
                        withComponent(entry.getKey(), entry.getValue().toString());
                }
                for (Map.Entry<String, URI> entry : catalog.getPublicMappings()
                                .entrySet()) {
                        withEntity(entry.getKey(), entry.getValue().toString());
                }
                for (Map.Entry<URI, URI> rule : catalog.getRewriteUris().entrySet()) {
                        Iterable<URL> entries = this.loader.loadResources(rule.getValue()
                                        .toString());
                        for (URL url : entries) {
                                try {
                                        //get tail of the path i.e. ../static/css/ -> /css/
                                        String path=url.toURI().getPath().toString().replace(rule.getValue().toString().replace("..",""),"");
                                        withComponent(rule.getKey().resolve(URI.create(path)), url.toString());
                                        
                                } catch (URISyntaxException e) {
                                        mLogger.warn("Exception while generating paths");
                                }
                        }
                }
                

                return this;
        }

        public OSGIModuleBuilder withEntities(Collection<? extends Entity> entities) {
                this.entities.addAll(entities);
                return this;
        }
        
        public OSGIModuleBuilder withEntity(String publicId, String path) {
                mLogger.trace("withEntity:" + publicId.toString() + ", path: " + path);
                entities.add(new Entity(publicId, path, loader));
                return this;
        }


}
