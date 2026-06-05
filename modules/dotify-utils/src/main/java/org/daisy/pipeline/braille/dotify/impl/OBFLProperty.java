package org.daisy.pipeline.braille.dotify.impl;

import org.daisy.braille.css.BrailleCSSProperty;

public interface OBFLProperty extends BrailleCSSProperty {

	@Override
	default boolean inherited() {
		return false;
	}

	public enum KeepWithSheets implements OBFLProperty {
		integer(""), INHERIT("inherit"), INITIAL("initial");
		private String text;
		private KeepWithSheets(String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
	}

	// not inherited in OBFL
	public enum LineHeight implements OBFLProperty {
		number(""), percentage(""), INHERIT("inherit"), INITIAL("initial");
		private String text;
		private LineHeight(String text) {
			this.text = text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
		@Override
		public String toString() {
			return text;
		}
	}

	public enum Marker implements OBFLProperty {
		list_values(""), NONE("none"), INHERIT("inherit"), INITIAL("initial");
		private String text;
		private Marker(String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
	}

	public enum ScenarioCost implements OBFLProperty {
		integer(""), evaluate(""), NONE("none"), INHERIT("inherit"), INITIAL("initial");
		private String text;
		private ScenarioCost(String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
	}

	// not inherited in OBFL
	public enum TextAlign implements OBFLProperty {
	    LEFT("left"), RIGHT("right"), CENTER("center"), INHERIT("inherit"), INITIAL("initial");
		private String text;
		private TextAlign(String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
	}

	// not inherited in OBFL
	public enum TextIndent implements OBFLProperty {
		integer(""), INHERIT("inherit"), INITIAL("initial");
		private String text;
		private TextIndent(String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
	}
	
	public enum TocRange implements OBFLProperty {
		DOCUMENT("document"), VOLUME("volume"), INHERIT("inherit"), INITIAL("initial");
		private String text;
		private TocRange(String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
	}

	public enum VerticalAlign implements OBFLProperty {
		BEFORE("before"), CENTER("center"), AFTER("after"), INHERIT("inherit"), INITIAL("initial");
		private final String text;
		private VerticalAlign(String text) {
			this.text = text;
		}
		@Override
		public String toString() {
			return text;
		}
		@Override
		public boolean equalsInherit() {
			return this == INHERIT;
		}
		@Override
		public boolean equalsInitial() {
			return this == INITIAL;
		}
	}

	public enum VolumeBreakInside implements OBFLProperty {
		keep();
		@Override
		public String toString() {
			return "";
		}
		@Override
		public boolean equalsInherit() {
			return false;
		}
		@Override
		public boolean equalsInitial() {
			return false;
		}
	}
}
