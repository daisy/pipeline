package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "catch-xproc-error",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=catch-xproc-error",
		"script.description:String=",
		"script.url:String=/module/catch-xproc-error.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_catch_xproc_error extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_catch_xproc_error.class);
	}
}