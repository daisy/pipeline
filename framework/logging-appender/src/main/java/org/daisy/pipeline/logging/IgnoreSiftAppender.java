package org.daisy.pipeline.logging;
import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.sift.SiftingAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class IgnoreSiftAppender extends SiftingAppender {

	@Override
	protected void append(ILoggingEvent event) {
		if (!isStarted()) {
			return;
		}
		String discriminatingValue = this.getDiscriminator().getDiscriminatingValue(event);
		if( !"default".equals(discriminatingValue)){
			super.append(event);
                        //Force closing the logging file when 
                        //we finish the job
                        if (event.getMarker()==ClassicConstants.FINALIZE_SESSION_MARKER){
                                super.stop();
                        }
		}//else ignore
		
	}
}
