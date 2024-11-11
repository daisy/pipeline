package org.daisy.pipeline.modules.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.util.NoSuchElementException;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.daisy.pipeline.modules.ResolutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * ModuleUriResolver resolves uris taking into account the components from the
 * modules loaded.
 */
@Component(
	name = "module-uri-resolver",
	service = {
		URIResolver.class,
		EntityResolver.class
	}
)
public class ModuleUriResolver implements URIResolver, EntityResolver {

	/** The m logger. */
	private static Logger mLogger = LoggerFactory
			.getLogger(ModuleUriResolver.class);

	/** The m registry. */
	private ModuleRegistry mRegistry = null;

	/**
	 * Activate.
	 */
	@Activate
	public void activate() {
		mLogger.trace("Activating module URI resolver");
	}

	/**
	 * Sets the module registry.
	 *
	 * @param reg
	 *            the new module registry
	 */
	@Reference(
		name = "module-registry",
		unbind = "-",
		service = ModuleRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setModuleRegistry(ModuleRegistry reg) {
		mRegistry = reg;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
	 * java.lang.String)
	 */
	@SuppressWarnings(
		"deprecation" // URLDecode.decode is deprecated
	)
	@Override
	public Source resolve(String href, String base) {
		URI uhref = URI.create(href);
		if (uhref.isAbsolute()) {
			return resolveFromModules(uhref);
		} else if (base != null && base.startsWith("jar:file:")) {
			// handle this case also because it is not handled correctly in XMLCalabash
			try {
				return new SAXSource(new InputSource(new URL(new URL(base), href).toString()));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e); // should not happen
			}
		}
		// otherwise let it be handled further down
		return null;
	}
	
	private Source resolveFromModules(URI href) {
		Module mod = mRegistry.getModuleByComponent(href);
		if (mod == null) {
			mLogger.trace("No module found for URI: " + href);
			return null;
		}
		mLogger.trace(href.toString() + " found in module " + mod.getName());
		URI resource; {
			try {
				resource = mod.getComponent(href).getResource();
			} catch (NoSuchElementException|ResolutionException e) {
				throw new IllegalStateException("coding error");
			}
		}
		SAXSource source = new SAXSource(new InputSource(resource.toString()));
		return source;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
		Module mod = mRegistry.getModuleByEntity(publicId);
		// by public id
		if (mod != null) {
			mLogger.trace("Entity " + publicId + " found in module " + mod.getName());
			URI resource; {
				try {
					resource = mod.getEntity(publicId).getResource();
				} catch (NoSuchElementException|ResolutionException e) {
					throw new IllegalStateException("coding error");
				}
			}
			return new InputSource(resource.toString());
		}
		// by systemId
		mLogger.trace("No module found for publicId:" + publicId);
		URI uriSystem=URI.create(systemId);
		mod = mRegistry.getModuleByComponent(uriSystem);
		if (mod != null) {
			return new InputSource(mod.getComponent(uriSystem).getResource().toString());
		} else {
			mLogger.trace("No module found for publicId: " + systemId);
			return null;
		}
	}

}
