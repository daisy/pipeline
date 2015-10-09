package org.daisy.dotify.common.text;

interface StepForward<T extends SplitPointUnit> {
	void addUnit(T unit);
	boolean overflows(T buffer);
}