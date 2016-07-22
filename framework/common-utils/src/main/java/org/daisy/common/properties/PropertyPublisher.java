package org.daisy.common.properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyPublisher {

	private PropertyTracker tracker;
	private static Logger logger = LoggerFactory.getLogger(PropertyPublisher.class.getName());

	/**
	 * Constructs a new instance.
	 *
	 * @param tracker The tracker for this instance.
	 */
	PropertyPublisher(PropertyTracker tracker) {
		this.tracker = tracker;
	}

	public void publish(Property property){
		if(this.tracker!=null){
			this.tracker.addProperty(property);
		}else{
			logger.warn("Trying to publish a property but the tracker is not set");
		}
	}
	/**
	 * Mainly to give some indepence from OSGI to classes that do not require
	 * OSGI api explicitily. This method resolves the BundleName + and id
	 */
	public void publish(String propertyName,String value,@SuppressWarnings("rawtypes") Class origin){
		Bundle bundle=FrameworkUtil.getBundle(origin);	
		if(bundle==null){
			throw new IllegalStateException("Bundle not found for "+origin.getCanonicalName());
		}
		Property prop = new Property.Builder().withPropertyName(propertyName).withValue(value).withBundleId(bundle.getBundleId()).withBundleName(bundle.getSymbolicName()).build();
		this.publish(prop);
			
	}
	/**
	 * Mainly to give some indepence from OSGI to classes that do not require
	 * OSGI api explicitily. This method resolves the BundleName + and id
	 */
	public void unpublish(String propertyName,@SuppressWarnings("rawtypes") Class origin){
		if (this.tracker==null){
			logger.warn("Trying to unpublish a property but the tracker is not set");
			return;
		}

		Bundle bundle=FrameworkUtil.getBundle(origin);	

		if(bundle==null){
			throw new IllegalStateException("Bundle not found for "+origin.getCanonicalName());
		}

		Property prop=this.tracker.getProperty(propertyName,bundle.getSymbolicName());

		if(prop==null){
			throw new IllegalStateException("Property not found for name:"+propertyName+" bundle "+bundle.getSymbolicName());
		}

		this.unpublish(prop);
			
	}
	public void unpublish(Property property){
		if(this.tracker!=null){
			this.tracker.deleteProperty(property);
		}else{
			logger.warn("Trying to unpublish a property but the tracker is not set");
		}
	}
	/**
	 * Sets the tracker for this instance.
	 *
	 * @param tracker The tracker.
	 */
	public void setTracker(PropertyTracker tracker) {
		this.tracker = tracker;
	}

	/**
	 * Gets the tracker for this instance.
	 *
	 * @return The tracker.
	 */
	public PropertyTracker getTracker() {
		return this.tracker;
	}

}
