package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "xslt-terminate-error",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=xslt-terminate-error",
		"script.description:String=",
		"script.url:String=/module/xslt-terminate-error.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_xslt_terminate_error extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_xslt_terminate_error.class);
	}
}