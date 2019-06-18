package org.daisy.dotify.api.translator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Provides a default implementation of attributes with context.
 * @author Joel Håkansson
 */
public class DefaultAttributeWithContext implements AttributeWithContext {
	protected final int length;
	private final Optional<String> name;
	private final List<AttributeWithContext> attributes;

	/**
	 * Provides a builder for a default text attribute
	 */
	public static class Builder {
		private final String name;
		private final List<AttributeWithContext> attributes;

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
			this.name = attribute;
		}

		/**
		 * Adds a text attribute to the list of attributes
		 * @param data the attribute
		 * @return this builder
		 */
		public Builder add(AttributeWithContext data) {
			attributes.add(data);
			return this;
		}

		/**
		 * Adds a new attribute with the specified width
		 * @param val the width of the attribute
		 * @return this builder
		 */
		public Builder add(int val) {
			attributes.add(new DefaultAttributeWithContext.Builder().build(val));
			return this;
		}

		/**
		 * Creates a new default text attribute based on the current
		 * state of the builder
		 * @param length the total length of the contained attributes 
		 * @return a new default text attribute
		 */
		public DefaultAttributeWithContext build(int length) {
			return new DefaultAttributeWithContext(length, this);
		}
	}

	protected DefaultAttributeWithContext(int length, Builder builder) {
		this.length = length;
		this.name = Optional.ofNullable(builder.name);
		this.attributes = builder.attributes;
		int s = 0;
		for (AttributeWithContext a : builder.attributes) {
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
		for (AttributeWithContext t : attributes) {
			sb.append('\n').append(t.toString());
		}
		return this.getClass().getSimpleName() + " [length=" + length + (name != null ? ", name=" + name : "") + ", attributes=" + sb.toString() + "]";
	}

	@Override
	public Optional<String> getName() {
		return name;
	}

	@Override
	public Iterator<AttributeWithContext> iterator() {
		return attributes.iterator();
	}

}
