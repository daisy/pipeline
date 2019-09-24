package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "java-function-runtime-error",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=java-function-runtime-error",
		"script.description:String=",
		"script.url:String=/module/java-function-runtime-error.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_java_function_runtime_error extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_java_function_runtime_error.class);
	}
}