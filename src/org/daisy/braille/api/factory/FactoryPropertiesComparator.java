package org.daisy.braille.api.factory;

import java.util.Comparator;

/**
 * Provides a comparator for factory properties.
 * 
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * 
 * @author Joel Håkansson
 *
 */
public class FactoryPropertiesComparator implements Comparator<FactoryProperties> {
	private Order order;
	private By by;
	
	public enum Order {
		UP,
		DOWN
	}
	
	public enum By {
		DISPLAY_NAME,
		IDENTIFIER,
		DESCRIPTION
	}
	
	public FactoryPropertiesComparator() {
		this(Order.UP, By.DISPLAY_NAME);
	}
	
	private FactoryPropertiesComparator(Order order, By by) {
		this.order = order;
		this.by = by;
	}

	public FactoryPropertiesComparator order(Order order) {
		this.order = order;
		return this;
	}
	public FactoryPropertiesComparator by(By by) {
		this.by = by;
		return this;
	}

	@Override
	public int compare(FactoryProperties arg0, FactoryProperties arg1) {
		switch (by) {
			case DESCRIPTION:
				return (order==Order.UP?1:-1)*arg0.getDescription().compareTo(arg1.getDescription());
			case IDENTIFIER:
				return (order==Order.UP?1:-1)*arg0.getIdentifier().compareTo(arg1.getIdentifier());
			case DISPLAY_NAME: default:
				return (order==Order.UP?1:-1)*arg0.getDisplayName().compareTo(arg1.getDisplayName());
		}
	}

}
