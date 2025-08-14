package org.daisy.common.xproc.calabash.impl;

import java.net.URI;

import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcResult;
import org.daisy.common.xproc.calabash.XProcRuntimeFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.xml.sax.EntityResolver;

//TODO check thread safety
/**
 * Calabash xproc engine wrapper
 */
@Component(
	name = "calabash-xproc-engine",
	service = { XProcEngine.class }
)
public class CalabashXProcEngine implements XProcEngine {

	private static final Logger logger = LoggerFactory.getLogger(CalabashXProcEngine.class);

	/**
	 * Activate (to be used by OSGI)
	 */
	@Activate
	public void activate(){
		logger.trace("Activating XProc Engine");
	}

	@Override
	public XProcPipeline load(URI uri) {
		if (runtimeFactory == null)
			throw new IllegalStateException("Calabash runtime factory unavailable");
		return new CalabashXProcPipeline(uri, runtimeFactory, entityResolver);
	}

	@Override
	public XProcPipelineInfo getInfo(URI uri) {
		return load(uri).getInfo();
	}

	@Override
	public XProcResult run(URI uri, XProcInput data) throws XProcErrorException {
		return load(uri).run(data);
	}

	@Reference(
		name = "XProcRuntimeFactory",
		unbind = "-",
		service = XProcRuntimeFactory.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setRuntimeFactory(XProcRuntimeFactory factory) {
		this.runtimeFactory = factory;
	}

	private XProcRuntimeFactory runtimeFactory = null;

	@Reference(
		name = "EntityResolver",
		unbind = "-",
		service = EntityResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	private EntityResolver entityResolver = null;

}
