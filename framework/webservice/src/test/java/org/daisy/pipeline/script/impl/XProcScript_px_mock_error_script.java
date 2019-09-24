package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "px:mock-error-script",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=px:mock-error-script",
		"script.description:String=",
		"script.url:String=/mock-module/error-script.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_px_mock_error_script extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_px_mock_error_script.class);
	}
}