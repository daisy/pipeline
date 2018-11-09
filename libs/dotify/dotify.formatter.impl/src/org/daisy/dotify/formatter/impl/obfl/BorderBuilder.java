package org.daisy.dotify.formatter.impl.obfl;

import java.util.HashSet;
import java.util.logging.Logger;

import org.daisy.dotify.api.translator.Border;
import org.daisy.dotify.api.translator.Border.Builder.BuilderView;
import org.daisy.dotify.api.translator.BorderSpecification.Align;
import org.daisy.dotify.api.translator.BorderSpecification.Style;

class BorderBuilder {
	
	private static final String KEY_BORDER = "border";
	private static final String KEY_TOP = "top";
	private static final String KEY_LEFT = "left";
	private static final String KEY_RIGHT = "right";
	private static final String KEY_BOTTOM = "bottom";
	private static final String KEY_STYLE = "style";
	private static final String KEY_WIDTH = "width";
	private static final String KEY_ALIGN = "align";
	private final Border.Builder builder;

	private boolean useBorder = false;

	BorderBuilder() {
		this.builder = new Border.Builder();
	}
	
	void put(String key, Object value) {
		if (key!=null && key.toLowerCase().startsWith(KEY_BORDER)) {
			useBorder = true;
			HashSet<String> set = new HashSet<>();
			for (String s : key.toLowerCase().split("-")) {
				set.add(s);
			}
			set.remove(KEY_BORDER);
			BuilderView b = builder.getDefault();
			if (set.remove(KEY_TOP)) {
				b = builder.getTop();
			} else if (set.remove(KEY_LEFT)) {
				b = builder.getLeft();
			} else if (set.remove(KEY_RIGHT)) {
				b = builder.getRight();
			} else if (set.remove(KEY_BOTTOM)) {
				b = builder.getBottom();
			}
			if (set.size()==1) {
				String s = set.iterator().next();
				set(b, s, value.toString());
			} else {
				//unknown
				Logger.getLogger(this.getClass().getCanonicalName()).warning("Unknown feature: " + key);
			}
		} else {
			Logger.getLogger(this.getClass().getCanonicalName()).warning("Unknown feature: " + key);
		}
	}
	
	private void set(BuilderView b, String key, String value) {
		key = key.toLowerCase();
		value = value.toUpperCase();
		if (key.equals(KEY_STYLE)) {
			b.style(Style.valueOf(value));
		} else if (key.equals(KEY_WIDTH)) {
			try {
				b.width(Integer.parseInt(value));
			} catch (NumberFormatException e) {
				//ignore
				Logger.getLogger(this.getClass().getCanonicalName()).warning("Ignoring unparsable value: " + value);
			}
		} else if (key.equals(KEY_ALIGN)) {
			b.align(Align.valueOf(value));
		} else {
			throw new IllegalArgumentException("Unkown value '" + value + "' for " + key);
		}
	}
	
	Border build() {
		if (useBorder) {
			return builder.build();
		} else {
			return null;
		}
	}



}
