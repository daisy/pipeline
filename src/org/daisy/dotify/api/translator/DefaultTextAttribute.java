package org.daisy.dotify.api.translator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class DefaultTextAttribute implements TextAttribute {
	protected final int length;
	private final String identifier;
	private final List<TextAttribute> attributes;

	public static class Builder {
		private final String identifier;
		private final List<TextAttribute> attributes;

		public Builder() {
			this(null);
		}

		public Builder(String attribute) {
			this.attributes = new ArrayList<TextAttribute>();
			this.identifier = attribute;
		}

		public Builder add(TextAttribute data) {
			attributes.add(data);
			return this;
		}

		public Builder add(int val) {
			attributes.add(new DefaultTextAttribute.Builder().build(val));
			return this;
		}

		public DefaultTextAttribute build(int length) {
			return new DefaultTextAttribute(length, this);
		}
	}

	protected DefaultTextAttribute(int length, Builder builder) {
		this.length = length;
		this.identifier = builder.identifier;
		this.attributes = builder.attributes;
		int s = 0;
		for (TextAttribute a : builder.attributes) {
			s += a.getWidth();
		}
		if (s > 0 && s != getWidth()) {
			throw new IllegalArgumentException("Text attribute size (" + s + ") does not match specified length (" + length + ").");
		}
	}

	public int getWidth() {
		return length;
	}

	public boolean hasChildren() {
		return attributes.size() > 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TextAttribute t : attributes) {
			sb.append("\n" + t.toString());
		}
		return this.getClass().getSimpleName() + " [length=" + length + (identifier != null ? ", identifier=" + identifier : "") + ", attributes=" + sb.toString() + "]";
	}

	public String getDictionaryIdentifier() {
		return identifier;
	}

	public Iterator<TextAttribute> iterator() {
		return attributes.iterator();
	}

}
