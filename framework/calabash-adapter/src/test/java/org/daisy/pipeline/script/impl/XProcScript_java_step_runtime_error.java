package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "java-step-runtime-error",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=java-step-runtime-error",
		"script.description:String=",
		"script.url:String=/module/java-step-runtime-error.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_java_step_runtime_error extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_java_step_runtime_error.class);
	}
}