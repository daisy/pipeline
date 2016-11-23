package org.daisy.dotify.api.formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an marker indicator region. This margin region marks occurrences of
 * markers at the height of the occurrence within the flow using the specified
 * indicators. The first matching indicator in the list of indicators at that
 * row will be printed in the margin region. 
 * 
 * @author Joel Håkansson
 */
public class MarkerIndicatorRegion implements MarginRegion {
	private final int width;
	private final List<MarkerIndicator> indicators;
	
	/**
	 * Creates a new marker indicator region builder
	 */
	public static class Builder {
		private final int width;
		
		private List<MarkerIndicator> indicators  = new ArrayList<>();
		private boolean built = false;
		
		/**
		 * Creates a new builder with the specified width.
		 * @param width the width of the column, in characters
		 */
		public Builder(int width) {
			this.width = width;
		}

		/**
		 * Adds a new marker indicator.
		 * @param name the name of the markers to indicate
		 * @param indicator the string indicating an occurrence
		 * @return a new builder
		 * @throws IllegalStateException if the builder has already transitioned to the built state
		 */
		public Builder addIndicator(String name, String indicator) {
			if (built) {
				throw new IllegalStateException();
			}
			indicators.add(new MarkerIndicator(name, indicator));
			return this;
		}
		
		/**
		 * Builds the builder into a marker indicator region.
		 * @return returns a new marker indicator region
		 * @throws IllegalStateException if the builder has already transitioned to the built state
		 */
		public MarkerIndicatorRegion build() {
			if (built) {
				throw new IllegalStateException();
			}
			built = true;
			return new MarkerIndicatorRegion(this);
		}
	}

	private MarkerIndicatorRegion(Builder builder) {
		this.width = builder.width;
		this.indicators = Collections.unmodifiableList(builder.indicators);
	}
	
	/**
	 * Creates a new builder of the specified width.
	 * @param width the width of the builder, in characters
	 * @return the builder
	 */
	public static Builder ofWidth(int width) {
		return new Builder(width);
	}

	@Override
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the list of indicators.
	 * @return the list of indicators
	 */
	public List<MarkerIndicator> getIndicators() {
		return indicators;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indicators == null) ? 0 : indicators.hashCode());
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MarkerIndicatorRegion other = (MarkerIndicatorRegion) obj;
		if (indicators == null) {
			if (other.indicators != null) {
				return false;
			}
		} else if (!indicators.equals(other.indicators)) {
			return false;
		}
		if (width != other.width) {
			return false;
		}
		return true;
	}

}
