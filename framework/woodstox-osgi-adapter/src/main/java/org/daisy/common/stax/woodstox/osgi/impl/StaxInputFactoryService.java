package org.daisy.common.stax.woodstox.osgi.impl;

import javax.xml.stream.XMLInputFactory;

import com.ctc.wstx.stax.WstxInputFactory;

import org.osgi.service.component.annotations.Component;

/**
 * A factory for creating StaxInputFactoryService objects.
 */
@Component(
    name = "stax-input-factory",
    service = { XMLInputFactory.class },
    servicefactory = true,
    property = {
        "org.codehaus.stax2.implName:String=woodstox", // Stax2InputFactoryProvider.OSGI_SVC_PROP_IMPL_NAME    -> ReaderConfig.getImplName()
        "org.codehaus.stax2.implVersion:String=4.1"    // Stax2InputFactoryProvider.OSGI_SVC_PROP_IMPL_VERSION -> ReaderConfig.getImplVersion()
    }
)
public class StaxInputFactoryService extends WstxInputFactory {
}
