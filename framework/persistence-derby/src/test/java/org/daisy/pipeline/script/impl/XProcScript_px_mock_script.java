package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "px:mock-script",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=px:mock-script",
		"script.description:String=",
		"script.url:String=/script.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_px_mock_script extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_px_mock_script.class);
	}
}