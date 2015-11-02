package org.daisy.dotify.common.layout;

interface StepForward<T extends SplitPointUnit> {
	void addUnit(T unit);
	boolean overflows(T buffer);
}