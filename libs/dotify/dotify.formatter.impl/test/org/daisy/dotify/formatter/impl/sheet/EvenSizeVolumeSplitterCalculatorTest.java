package org.daisy.dotify.formatter.impl.sheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class EvenSizeVolumeSplitterCalculatorTest {

	@Test
	public void breakpoints() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, 49);
		for (int j=1; j<i; j++) {
			if (j==48 || j==96 || j==144 || j==192 || j==240 || j==288 || j==336 || j==384 || j==432) {
				assertTrue("Assert that sheet is a break point: " + j, ssd.isBreakpoint(j));
			} else {
				assertTrue("Assert that sheet is not a break point: " + j, !ssd.isBreakpoint(j));
			}
		}
	}
	
	@Test
	public void breakpoints_variable() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, new SplitterLimit() {
			@Override
			public int getSplitterLimit(int volume) {
				return (volume % 2 == 1?50:48);
			}
		});
		for (int j=1; j<i; j++) {
			if (j==49 || j==96 || j==145 || j==192 || j==241 || j==288 || j==337|| j==384 || j==433) {
				assertTrue("Assert that sheet is a break point: " + j, ssd.isBreakpoint(j));
			} else {
				assertTrue("Assert that sheet is not a break point: " + j, !ssd.isBreakpoint(j));
			}
		}
	}
	
	@Test
	public void breakpointsWithOffset() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, 49, 1);
		for (int j=1; j<i; j++) {
			if (j==44 || j==88 || j==132 || j==176 || j==220 || j==264 || j==307 || j==350 || j==393 || j==436) {
				assertTrue("Assert that sheet is a break point: " + j, ssd.isBreakpoint(j));
			} else {
				assertTrue("Assert that sheet is not a break point: " + j, !ssd.isBreakpoint(j));
			}
		}
	}
	
	@Test
	public void breakpointsWithOffset_2() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, 49, 2);
		for (int j=1; j<i; j++) {
			if (j==40 || j==80 || j==120 || j==160 || j==200 || j==240 || j==280 || j==320 || j==360 || j==400 || j==440) {
				assertTrue("Assert that sheet is a break point: " + j, ssd.isBreakpoint(j));
			} else {
				assertTrue("Assert that sheet is not a break point: " + j, !ssd.isBreakpoint(j));
			}
		}
	}
	
	@Test
	public void volumeNumber() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, 49);
		int vol = 1;
		for (int j=1; j<i; j++) {
			if (j==48 || j==96 || j==144 || j==192 || j==240 || j==288 || j==336 || j==384 || j==432) {
				vol++;
			}
			assertEquals("Assert that sheet "+ j +" is in the right volume.", vol, ssd.getVolumeForSheet(j));
		}
	}
	
	@Test
	public void volumeCount() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, 49);
		assertTrue("Assert that number of volumes is correct: " + ssd.getVolumeCount(), ssd.getVolumeCount()==10);
	}
	
	@Test
	public void volumeCountWithOffset() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, 49, 1);
		assertTrue("Assert that number of volumes is correct: " + ssd.getVolumeCount(), ssd.getVolumeCount()==11);
	}
	
	@Test
	public void volumeCountWithOffset_2() {
		int i = 479;
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(i, 49, 2);
		assertTrue("Assert that number of volumes is correct: " + ssd.getVolumeCount(), ssd.getVolumeCount()==12);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void sheetZeroBreakpoint() {
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(479, 49);
		ssd.isBreakpoint(0);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void sheetLimitBreakpoint() {
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(479, 49);
		ssd.isBreakpoint(480);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void sheetZeroVolumeNumber() {
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(479, 49);
		ssd.getVolumeForSheet(0);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void sheetLimitVolumeNumber() {
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(479, 49);
		ssd.getVolumeForSheet(480);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void negativeSheet() {
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(479, 49);
		ssd.isBreakpoint(-1);
	}
	
	@Test (expected=IndexOutOfBoundsException.class)
	public void negativeSheetVolumeNumber() {
		EvenSizeVolumeSplitterCalculator ssd = new EvenSizeVolumeSplitterCalculator(479, 49);
		ssd.getVolumeForSheet(-1);
	}


}