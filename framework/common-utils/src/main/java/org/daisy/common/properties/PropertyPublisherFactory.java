package org.daisy.common.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "property-publisher-factory",
	immediate =true,
	service = { PropertyPublisherFactory.class }
)
public class PropertyPublisherFactory {
	PropertyTracker tracker= null;
	private static String NO_PROPERTY_ERROR="Trying to get a new property publisher but tracker is null";
	private static Logger logger = LoggerFactory.getLogger(PropertyTracker.class.getName());

	@Activate
	public void activate(){
		logger.debug("property publisher factory up");
	}

	@Deactivate
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
	@Reference(
		name = "property-tracker",
		unbind = "-",
		service = PropertyTracker.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setTracker(PropertyTracker tracker){
		this.tracker=tracker;
	}
}
