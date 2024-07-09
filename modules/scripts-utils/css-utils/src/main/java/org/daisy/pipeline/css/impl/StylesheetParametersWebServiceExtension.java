package org.daisy.pipeline.css.impl;

import javax.xml.transform.URIResolver;

import org.daisy.pipeline.css.UserAgentStylesheetRegistry;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.webservice.restlet.WebServiceExtension;

import org.restlet.routing.Router;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "stylesheet-parameters-service-extension",
	service = { WebServiceExtension.class }
)
public class StylesheetParametersWebServiceExtension implements WebServiceExtension {

	static final String STYLESHEET_PARAMETERS_ROUTE = "/stylesheet-parameters";

	private URIResolver uriResolver;

	@Reference(
		name = "URIResolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setURIResolver(URIResolver resolver) {
		uriResolver = resolver;
	}

	private DatatypeRegistry datatypeRegistry;

	@Reference(
		name = "DatatypeRegistry",
		unbind = "-",
		service = DatatypeRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setDatatypeRegistry(DatatypeRegistry registry) {
		datatypeRegistry = registry;
	}

	private UserAgentStylesheetRegistry userAgentStylesheetRegistry;

	@Reference(
		name = "UserAgentStylesheetRegistry",
		unbind = "-",
		service = UserAgentStylesheetRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setUserAgentStylesheetRegistry(UserAgentStylesheetRegistry registry) {
		userAgentStylesheetRegistry = registry;
	}

	public void attachTo(Router router) {
		router.getContext().getAttributes().put(StylesheetParametersResource.URI_RESOLVER_KEY, uriResolver);
		router.getContext().getAttributes().put(StylesheetParametersResource.DATATYPE_REGISTRY_KEY, datatypeRegistry);
		router.getContext().getAttributes().put(StylesheetParametersResource.USER_AGENT_STYLESHEET_REGISTRY_KEY, userAgentStylesheetRegistry);
		router.attach(STYLESHEET_PARAMETERS_ROUTE, StylesheetParametersResource.class);
	}
}
