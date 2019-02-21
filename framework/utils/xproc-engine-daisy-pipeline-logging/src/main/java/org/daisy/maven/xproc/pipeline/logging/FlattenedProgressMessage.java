package org.daisy.maven.xproc.pipeline.logging;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FlattenedProgressMessage {
	
	final String text;
	final int sequence;
	final Integer parent;
	final int depth;
	final BigDecimal currentTotalProgress;
	
	public FlattenedProgressMessage(String text, int sequence, Integer parent, int depth, BigDecimal currentTotalProgress) {
		this.text = text;
		this.sequence = sequence;
		this.parent = parent;
		this.depth = depth;
		this.currentTotalProgress = currentTotalProgress;
	}
	
	String getText() {
		return text;
	}
	
	int getSequence() {
		return sequence;
	}
	
	Integer getParent() {
		return parent;
	}
	
	int getDepth() {
		return depth;
	}
	
	BigDecimal getProgress() {
		return currentTotalProgress;
	}
	
	@Override
	public String toString() {
		String s = "" + sequence + ": ";
		if (text != null)
			s += (" " + text);
		if (currentTotalProgress != null) {
			s += " (";
			s += currentTotalProgress.multiply(BigDecimal.TEN).multiply(BigDecimal.TEN)
				.setScale(0, RoundingMode.HALF_UP)
				.toPlainString();
			s += "%)";
		}
		return s;
	}
}
