package org.daisy.pipeline.ocr.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.ocr.OCRService;
import org.daisy.pipeline.webservice.restlet.WebServiceExtension;

import org.restlet.routing.Router;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
 name = "ocr-services-web-service-extension",
	service = { WebServiceExtension.class }
)
public class OCRServicesWebServiceExtension implements WebServiceExtension {

	static final String OCR_SERVICES_ROUTE = "/ocr/services";

	private List<OCRService> services = new ArrayList<>();

	@Reference(
		name = "OCRService",
		unbind = "-",
		service = OCRService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void setTTSRegistry(OCRService service) {
		services.add(service);
	}

	public void attachTo(Router router) {
		router.getContext().getAttributes().put(OCRServicesResource.OCR_SERVICES_KEY, services);
		router.attach(OCR_SERVICES_ROUTE, OCRServicesResource.class);
	}
}
