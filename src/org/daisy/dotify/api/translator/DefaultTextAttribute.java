package org.daisy.dotify.api.translator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides a default implementation of text attributes.
 * @author Joel Håkansson
 */
public class DefaultTextAttribute implements TextAttribute {
	protected final int length;
	private final String identifier;
	private final List<TextAttribute> attributes;

	/**
	 * Provides a builder for a default text attribute
	 */
	public static class Builder {
		private final String identifier;
		private final List<TextAttribute> attributes;

		/**
		 * Creates a new builder
		 */
		public Builder() {
			this(null);
		}

		/**
		 * Creates a new builder with the specified attribute name
		 * @param attribute the attribute name
		 */
		public Builder(String attribute) {
			this.attributes = new ArrayList<>();
			this.identifier = attribute;
		}

		/**
		 * Adds a text attribute to the list of attributes
		 * @param data the attribute
		 * @return returns this builder
		 */
		public Builder add(TextAttribute data) {
			attributes.add(data);
			return this;
		}

		/**
		 * Adds a new attribute with the specified width
		 * @param val the width of the attribute
		 * @return returns this builder
		 */
		public Builder add(int val) {
			attributes.add(new DefaultTextAttribute.Builder().build(val));
			return this;
		}

		/**
		 * Creates a new default text attribute based on the current
		 * state of the builder
		 * @param length the total length of the contained attributes 
		 * @return returns a new default text attribute
		 */
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
		if (s > 0 && s != length) {
			throw new IllegalArgumentException("Text attribute size (" + s + ") does not match specified length (" + length + ").");
		}
	}

	@Override
	public int getWidth() {
		return length;
	}

	@Override
	public boolean hasChildren() {
		return !attributes.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TextAttribute t : attributes) {
			sb.append("\n" + t.toString());
		}
		return this.getClass().getSimpleName() + " [length=" + length + (identifier != null ? ", identifier=" + identifier : "") + ", attributes=" + sb.toString() + "]";
	}

	@Override
	public String getDictionaryIdentifier() {
		return identifier;
	}

	@Override
	public Iterator<TextAttribute> iterator() {
		return attributes.iterator();
	}

}
