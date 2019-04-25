package org.daisy.pipeline.transform.saxon.impl;

import javax.xml.transform.TransformerFactory;

import org.daisy.pipeline.saxon.SaxonConfigurator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "saxon-transformer-factory",
	service = { TransformerFactory.class }
)
public class TransformerFactoryImpl extends net.sf.saxon.TransformerFactoryImpl {
	
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
