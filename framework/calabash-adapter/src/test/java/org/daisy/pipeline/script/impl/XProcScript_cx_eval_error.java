package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "cx-eval-error",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=cx-eval-error",
		"script.description:String=",
		"script.url:String=/module/cx-eval-error.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_cx_eval_error extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_cx_eval_error.class);
	}
}