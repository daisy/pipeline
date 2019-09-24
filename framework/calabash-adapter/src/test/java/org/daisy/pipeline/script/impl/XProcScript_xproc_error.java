package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "xproc-error",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=xproc-error",
		"script.description:String=",
		"script.url:String=/module/xproc-error.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_xproc_error extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_xproc_error.class);
	}
}