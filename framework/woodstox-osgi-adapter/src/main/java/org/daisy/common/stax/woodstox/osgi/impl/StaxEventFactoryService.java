package org.daisy.common.stax.woodstox.osgi.impl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;

import org.codehaus.stax2.ri.Stax2EventFactoryImpl;

import org.osgi.service.component.annotations.Component;

/**
 * A factory for creating StaxEventFactoryService objects.
 */
@Component(
    name = "stax-event-factory",
    service = { XMLEventFactory.class },
    servicefactory = true,
    property = {
        "org.codehaus.stax2.implName:String=woodstox",
        "org.codehaus.stax2.implVersion:String=4.1"
    }
)
// FIXME: it would have been easier to extend com.ctc.wstx.stax.WstxEventFactory, but it is final
public class StaxEventFactoryService extends Stax2EventFactoryImpl {

	@Override
	protected QName createQName(String nsURI, String localName) {
		return new QName(nsURI, localName);
	}

	@Override
	protected QName createQName(String nsURI, String localName, String prefix) {
		return new QName(nsURI, localName, prefix);
	}
}
