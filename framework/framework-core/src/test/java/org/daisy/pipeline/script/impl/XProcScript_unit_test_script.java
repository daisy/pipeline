package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "unit-test-script",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=unit-test-script",
		"script.description:String=detail description",
		"script.url:String=/script.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_unit_test_script extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_unit_test_script.class);
	}
}