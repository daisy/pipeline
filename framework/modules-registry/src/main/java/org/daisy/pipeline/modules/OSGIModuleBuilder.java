package org.daisy.pipeline.modules;

import java.net.URL;
import java.util.Iterator;

import com.google.common.collect.Iterators;

import org.daisy.pipeline.modules.AbstractModuleBuilder;
import org.daisy.pipeline.modules.ResourceLoader;

import org.osgi.framework.Bundle;

public class OSGIModuleBuilder extends AbstractModuleBuilder<OSGIModuleBuilder> {

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
                                // Paths are assumed to be relative to META-INF
                                if (!path.startsWith("../")) {
                                        throw new RuntimeException("Paths must start with '../' but got '" + path + "'");
                                }
                                path = path.substring(3);
                                URL url = bundle.getResource(path);
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

        @Override
        protected OSGIModuleBuilder self() {
                return this;
        }
}
