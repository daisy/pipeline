package org.daisy.pipeline.tts.css.calabash.impl;

import javax.xml.transform.URIResolver;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import cz.vutbr.web.css.CSSFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "css-speech-inliner",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}inline-css" }
)
public class InlineCSSProvider implements XProcStepProvider {

	private URIResolver resolver;

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new InlineCSSStep(runtime, step, resolver);
	}

	@Reference(
		name = "URIResolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setUriResolver(URIResolver resolver) {
		this.resolver = resolver;
	}
}
