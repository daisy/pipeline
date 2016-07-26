package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.Stack;

import org.daisy.dotify.common.text.StringTools;

class Margin extends Stack<MarginComponent> {
	enum Type {
		LEFT(false),
		RIGHT(true)
		;

		private final boolean reverse;
		private Type(boolean reverse) {
			this.reverse = reverse;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9008010676189297081L;

	private final Type t;

	Margin(Type t) {
		this.t = t;
	}
	
	MarginProperties buildMargin(char spaceCharacter) {
		return buildMarginProperties(spaceCharacter, false);
	}
	
	MarginProperties buildMarginParent(char spaceCharacter) {
		return buildMarginProperties(spaceCharacter, true);
	}

	private MarginProperties buildMarginProperties(char spaceCharacter, boolean parent) {
		boolean isSpace = true;
		ArrayList<String> inp = new ArrayList<>();
		int j = 0;
		for (MarginComponent c : this) {
			inp.add(StringTools.fill(spaceCharacter, c.getOuterOffset()));
			if (!parent || j<size()-1) {
				inp.add(c.getBorder());
				isSpace &= isSpace(spaceCharacter, c.getBorder());
			}
			if (!parent || j<size()-1) {
				inp.add(StringTools.fill(spaceCharacter, c.getInnerOffset()));
			}
			j++;
		}
		StringBuilder sb = new StringBuilder();
		if (t.reverse) {
			for (int i = inp.size()-1; i>=0; i--) {
				sb.append(inp.get(i));
			}
		} else {
			for (String s : inp) {
				sb.append(s);
			}
		}
		return new MarginProperties(sb.toString(), isSpace);
	}
	
	private boolean isSpace(char spaceCharacter, String ret) {
		for (int i = 0; i<ret.length(); i++) {
			if (ret.charAt(i)!=spaceCharacter) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Margin other = (Margin) obj;
		if (t != other.t) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Margin [t=" + t + ", elements=" + super.toString() + "]";
	}
	
}
