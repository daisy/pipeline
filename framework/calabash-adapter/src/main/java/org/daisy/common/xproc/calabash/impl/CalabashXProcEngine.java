package org.daisy.common.xproc.calabash.impl;

import java.net.URI;

import javax.xml.transform.URIResolver;

import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcResult;
import org.daisy.common.xproc.calabash.XProcConfigurationFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.EntityResolver;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

//TODO check thread safety
/**
 * Calabash xproc engine wrapper
 */
@Component(
	name = "calabash-xproc-engine",
	service = { XProcEngine.class }
)
public class CalabashXProcEngine implements XProcEngine {

	private static final Logger logger = LoggerFactory
			.getLogger(CalabashXProcEngine.class);

	private URIResolver uriResolver = null;
	private EntityResolver entityResolver = null;
	private XProcConfigurationFactory configFactory = null;

	/**
	 * Instantiates a new calabash x proc engine.
	 */
	public CalabashXProcEngine() {

	}

	/**
	 * Activate (to be used by OSGI)
	 */
	@Activate
	public void activate(){
		logger.trace("Activating XProc Engine");
	}

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.XProcEngine#load(java.net.URI)
	 */
	@Override
	public XProcPipeline load(URI uri) {
		if (configFactory == null) {
			throw new IllegalStateException(
					"Calabash configuration factory unavailable");
		}

		return new CalabashXProcPipeline(uri, configFactory, uriResolver, entityResolver);
	}

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.XProcEngine#getInfo(java.net.URI)
	 */
	@Override
	public XProcPipelineInfo getInfo(URI uri) {
		return load(uri).getInfo();
	}

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.XProcEngine#run(java.net.URI, org.daisy.common.xproc.XProcInput)
	 */
	@Override
	public XProcResult run(URI uri, XProcInput data) throws XProcErrorException {
		return load(uri).run(data);
	}

	/**
	 * Sets the configuration factory to this engine
	 *
	 * @param configFactory the new configuration factory
	 */
	@Reference(
		name = "calabash-config-factory",
		unbind = "-",
		service = XProcConfigurationFactory.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setConfigurationFactory(XProcConfigurationFactory configFactory) {
		this.configFactory = configFactory;
	}

	/**
	 * Sets the entity resolver to this engine
	 *
	 * @param entityResolver the new entity resolver
	 */
	@Reference(
		name = "entity-resolver",
		unbind = "-",
		service = EntityResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * Sets the uri resolver to this engine, the uri resolver is thought to be able to handle component uris
	 *
	 * @param uriResolver the new uri resolver
	 */
	@Reference(
		name = "uri-resolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}
}
