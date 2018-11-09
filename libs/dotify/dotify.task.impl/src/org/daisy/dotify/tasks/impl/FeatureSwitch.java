package org.daisy.dotify.tasks.impl;

/**
 * Provides a place to put feature switches, so that it is easy to find a list of 
 * features that can be toggled.
 *  
 * @author Joel Håkansson
 */
public enum FeatureSwitch {
	/**
	 * Defines if embossing is enabled or not.
	 */
	ENABLE_EDITING_INSTRUCTIONS("on".equalsIgnoreCase(System.getProperty("org.daisy.dotify.tasks.editing-instructions", "off")));

	private final boolean on;
	FeatureSwitch(boolean on) {
		this.on = on;
	}

	/**
	 * Returns true if the feature is on, false otherwise.
	 * @return true if the feature is on, false otherwise
	 */
	public boolean isOn() {
		return on;
	}
}
