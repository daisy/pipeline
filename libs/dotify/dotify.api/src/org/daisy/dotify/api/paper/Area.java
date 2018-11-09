package org.daisy.dotify.api.paper;

/**
 *
 * @author Bert Frees
 */
public class Area implements Dimensions {

	private final double width, height, offsetX, offsetY;

	/**
	 * Creates a new area with the specified parameters
	 * @param width the width, in mm
	 * @param height the height, in mm
	 * @param offsetX the x-offset, in mm
	 * @param offsetY the y-offset, in mm
	 */
	public Area(double width,
			double height,
			double offsetX,
			double offsetY) {

		this.width = width;
		this.height = height;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	/**
	 * Gets the x-offset.
	 * @return returns the x-offset, in mm
	 */
	public double getOffsetX() {
		return offsetX;
	}

	/**
	 * Gets the y-offset.
	 * @return returns the y-offset, in mm
	 */
	public double getOffsetY() {
		return offsetY;
	}
}
