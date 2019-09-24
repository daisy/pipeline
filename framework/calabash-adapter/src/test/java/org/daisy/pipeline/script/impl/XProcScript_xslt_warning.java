package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "xslt-warning",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=xslt-warning",
		"script.description:String=",
		"script.url:String=/module/xslt-warning.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_xslt_warning extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_xslt_warning.class);
	}
}