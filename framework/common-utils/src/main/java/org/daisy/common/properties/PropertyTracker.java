package org.daisy.common.properties;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.collect.ImmutableList;

/*
 * This class is not intended to provide configuration values, 
 * It's a mechanism to control which properties are set in different 
 * bundles along the framework
 * 
 */
public class PropertyTracker {
	//Properties are indexed using propery name and bundle name, there shouldnt be clashes 
	// but just in case
	private HashMap<String,HashMap<String,Property>> propertiesIndex = new HashMap<String,HashMap<String,Property>>();
	//most of the times the property will be accessed using list
	private LinkedList<Property> properties= new LinkedList<Property>();
	private static Logger logger = LoggerFactory.getLogger(PropertyTracker.class.getName());

	public void activate(){
		logger.debug("Property tracker up");
	}

	public void deactivate(){
		logger.debug("Property tracker down");
	}

	public Collection<Property> getProperties(){
		return ImmutableList.copyOf(this.properties);
	}

	public Collection<Property> getProperties(String propertyName){
		if( this.propertiesIndex.containsKey(propertyName)){
			return ImmutableList.copyOf(this.propertiesIndex.get(propertyName).values());
		}else{
			logger.warn(String.format("Trying to get a property with no values %s=>null empty list returned",propertyName));
			return ImmutableList.copyOf(new LinkedList<Property>());
		}
	}
	public Property getProperty(String propertyName,String bundleName){
		if ( this.propertiesIndex.containsKey(propertyName) &&
				this.propertiesIndex.get(propertyName).containsKey(bundleName)){

			return this.propertiesIndex.get(propertyName).get(bundleName);
		}else{
			logger.warn(String.format("Trying to access to a non exisiting property: %s=>%s=>null",propertyName,bundleName));
			return null;
		}
	}

	public void addProperty(Property prop){
		if (!this.propertiesIndex.containsKey(prop.getPropertyName())) {
			this.propertiesIndex.put(prop.getPropertyName(),new HashMap<String,Property>());	
		}	
		this.propertiesIndex.get(prop.getPropertyName()).put(prop.getBundleName(),prop);
		this.properties.add(prop);
		logger.debug(String.format("property tracked: %s",prop.toString()));
	}

	public void deleteProperty (Property prop){
		String propName=prop.getPropertyName();
		String bundle=prop.getBundleName();

		if (this.propertiesIndex.containsKey(propName)){
			if (this.propertiesIndex.get(propName).containsKey(bundle)) {
				this.propertiesIndex.get(propName).remove(bundle);
				//more than one property under that name
				if (this.propertiesIndex.get(propName).size()==0){
					this.propertiesIndex.remove(propName);
				}
				this.properties.remove(prop);	
				logger.debug("Property untracked "+prop);
			}else{
				logger.warn(String.format( "Trying to delete a property (%s) from a non-existent bundle name %s",propName,bundle));
			}
		}else
			logger.warn(String.format( "Trying to delete a non-existent property name %s",propName ));

	}
}
