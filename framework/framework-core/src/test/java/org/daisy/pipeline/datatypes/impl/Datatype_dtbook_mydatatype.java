package org.daisy.pipeline.datatypes.impl;

import java.util.Map;
import javax.xml.transform.URIResolver;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.UrlBasedDatatypeService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "dtbook:mydatatype",
	immediate = true,
	service = { DatatypeService.class },
	property = {
		"data-type.id:String=dtbook:mydatatype",
		"data-type.url:String=/datatype.xml"
	}
)
public class Datatype_dtbook_mydatatype extends UrlBasedDatatypeService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, Datatype_dtbook_mydatatype.class);
	}
}