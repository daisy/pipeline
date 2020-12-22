package org.daisy.common.saxon.impl;

import net.sf.saxon.s9api.Processor;

import org.daisy.common.saxon.SaxonConfigurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "saxon-processor",
	service = { Processor.class }
)
public class ProcessorImpl extends Processor {

	public ProcessorImpl() {
		super(false);
	}

	private SaxonConfigurator configurator = null;

	@Reference(
		name = "SaxonConfigurator",
		unbind = "-",
		service = SaxonConfigurator.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setSaxonConfigurator(SaxonConfigurator configurator) {
		this.configurator = configurator;
	}

	@Activate
	public void activate() {
		configurator.configure(this);
	}
}
