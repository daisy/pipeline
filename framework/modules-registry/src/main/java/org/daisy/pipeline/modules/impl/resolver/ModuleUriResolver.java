package org.daisy.pipeline.modules.impl.resolver;

import java.io.IOException;
import java.net.URI;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.daisy.pipeline.modules.Module;
import org.daisy.pipeline.modules.ModuleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * ModuleUriResolver resolves uris taking into account the components from the
 * modules loaded.
 */
public class ModuleUriResolver implements URIResolver, EntityResolver {

	/** The m logger. */
	private static Logger mLogger = LoggerFactory
			.getLogger(ModuleUriResolver.class);

	/** The m registry. */
	private ModuleRegistry mRegistry = null;

	/**
	 * Activate.
	 */
	public void activate() {
		mLogger.trace("Activating module URI resolver");
	}

	/**
	 * Sets the module registry.
	 *
	 * @param reg
	 *            the new module registry
	 */
	public void setModuleRegistry(ModuleRegistry reg) {
		mRegistry = reg;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.xml.transform.URIResolver#resolve(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Source resolve(String href, String base) {
		// System.out.println("Resolving:"+href);
		URI uhref = URI.create(href);
		Module mod = mRegistry.getModuleByComponent(uhref);

		if (mod == null) {
			mLogger.trace("No module found for uri:" + href);
			return null;
		}
		URI resource = mod.getComponent(uhref).getResource();
		if (resource == null) {
			mLogger.trace("No resource found in module " + mod.getName()
					+ " for uri :" + href);
			return null;
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
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		InputSource src=null;
		Module mod = mRegistry.getModuleByEntity(publicId);
		//by public id
		if (mod != null &&mod.getEntity(publicId)!=null && mod.getEntity(publicId).getResource()!=null) {
			src=new InputSource(mod.getEntity(publicId).getResource().toString());

		}else{
			//by systemId
			mLogger.trace("No module found for publicId:" + publicId);
			URI uriSystem=URI.create(systemId);
			mod = mRegistry.getModuleByComponent(uriSystem);
			if (mod != null) {
				src=new InputSource(mod.getComponent(uriSystem).getResource().toString());
			}else{
				mLogger.trace("No module found for uri:" + systemId);
			}
		}
		return src;
	}

}
