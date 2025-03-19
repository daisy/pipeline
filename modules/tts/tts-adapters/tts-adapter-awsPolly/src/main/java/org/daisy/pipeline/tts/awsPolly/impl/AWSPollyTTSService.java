package org.daisy.pipeline.tts.awsPolly.impl;

import java.util.Map;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;


/**
 *
 * @author mmartida
 */

@Component(
	name = "polly-tts-service",
	service = { TTSService.class }
)
public class AWSPollyTTSService extends AbstractTTSService{
    
    @Activate
	protected void loadSSMLadapter() {
    	super.loadSSMLadapter("/transform-ssml.xsl", AWSPollyTTSService.class);
    	// added for tests not run in a jar
    	if (mXSLTresource == null) {
    		mXSLTresource = AWSPollyTTSService.class.getClassLoader().getResource("/transform-ssml.xsl");
    	}
    }

	@Override
    public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		String priority = params.get("org.daisy.pipeline.tts.awsPolly.priority");
		int intPriority = 2;
		if (priority != null) {
			try { intPriority = Integer.valueOf(priority); } catch (NumberFormatException e) { }
		} return new AWSPollyEngine(this, intPriority);
    }
    
    @Override
	public String getName() {
		return "polly";
	}

	@Override
	public String getVersion() {
		return "cli";
	}
}
