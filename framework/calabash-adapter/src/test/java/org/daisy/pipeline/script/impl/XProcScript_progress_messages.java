package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "progress-messages",
	immediate = true,
	service = { XProcScriptService.class },
	property = {
		"script.id:String=progress-messages",
		"script.description:String=",
		"script.url:String=/module/progress-messages.xpl",
		"script.version:String=0"
	}
)
public class XProcScript_progress_messages extends XProcScriptService {
	@Activate
	public void activate(Map<?,?> properties) {
		super.activate(properties, XProcScript_progress_messages.class);
	}
}