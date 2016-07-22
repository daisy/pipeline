package org.daisy.common.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyPublisherFactory {
	PropertyTracker tracker= null;
	private static String NO_PROPERTY_ERROR="Trying to get a new property publisher but tracker is null";
	private static Logger logger = LoggerFactory.getLogger(PropertyTracker.class.getName());

	public void activate(){
		logger.debug("property publisher factory up");
	}

	public void deactivate(){
		logger.debug("property publisher factory down");
	}
	public PropertyPublisher newPropertyPublisher(){
		if ( tracker != null ){
			return new PropertyPublisher(this.tracker);
		}else{
			logger.warn(NO_PROPERTY_ERROR);
			throw new IllegalStateException(NO_PROPERTY_ERROR);
		}
	}

	/**
	 * Sets the tracker for this instance.
	 *
	 * @param tracker The tracker.
	 */
	public void setTracker(PropertyTracker tracker){
		this.tracker=tracker;
	}
}
