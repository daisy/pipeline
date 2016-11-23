package org.daisy.dotify.api.translator;

import org.daisy.dotify.api.translator.BorderSpecification.Align;
import org.daisy.dotify.api.translator.BorderSpecification.Style;

/**
 * Provides a border specification.
 * @author Joel Håkansson
 *
 */
public class Border {
	private final BorderSpecification top;
	private final BorderSpecification left;
	private final BorderSpecification right;
	private final BorderSpecification bottom;
	
	/**
	 * Provides a builder for borders.
	 */
	public static class Builder {
		private final BuilderView def;
		private final BuilderView top;
		private final BuilderView left;
		private final BuilderView right;
		private final BuilderView bottom;
		
		/**
		 * Creates a new builder.
		 */
		public Builder() {
			def = new BuilderView(new BorderSpecification.Builder()
					.align(Align.OUTER)
					.width(BorderSpecification.DEFAULT_WIDTH)
					.style(Style.NONE));
			top = new BuilderView(new BorderSpecification.Builder());
			left = new BuilderView(new BorderSpecification.Builder());
			right = new BuilderView(new BorderSpecification.Builder());
			bottom = new BuilderView(new BorderSpecification.Builder());
		}
		
		/**
		 * Gets the top builder view.
		 * @return returns the top builder view
		 */
		public BuilderView getTop() {
			return top;
		}

		/**
		 * Gets the left builder view.
		 * @return returns the left builder view
		 */
		public BuilderView getLeft() {
			return left;
		}

		/**
		 * Gets the right builder view.
		 * @return returns the right builder view
		 */
		public BuilderView getRight() {
			return right;
		}

		/**
		 * Gets the bottom builder view.
		 * @return returns the bottom builder view
		 */
		public BuilderView getBottom() {
			return bottom;
		}
		
		/**
		 * Gets the default builder view.
		 * @return returns the default builder view
		 */
		public BuilderView getDefault() {
			return def;
		}

		/**
		 * Creates a new border based on the current state
		 * of the builder.
		 * @return returns a new border
		 */
		public Border build() {
			return new Border(this);
		}
		
		/**
		 * Provides a view of the builder that has a 
		 * specific edge selected for configuration.
		 */
		public class BuilderView {
			private final BorderSpecification.Builder spec;
			private BuilderView(BorderSpecification.Builder spec) {
				this.spec = spec;
			}
			/**
			 * Gets the top edge view of the builder.
			 * @return returns the top edge view for configuration
			 */
			public BuilderView getTop() {
				return top;
			}

			/**
			 * Gets the left edge view of the builder.
			 * @return returns the left edge view for configuration
			 */
			public BuilderView getLeft() {
				return left;
			}

			/**
			 * Gets the right edge view of the builder.
			 * @return returns the right edge view for configuration
			 */
			public BuilderView getRight() {
				return right;
			}

			/**
			 * Gets the bottom edge view of the builder.
			 * @return returns the bottom edge view for configuration
			 */
			public BuilderView getBottom() {
				return bottom;
			}
			
			/**
			 * Gets the default edge view of the builder. The default
			 * edge is used whenever a named edge hasn't been configured.
			 * @return returns the default edge view for configuration
			 */
			public BuilderView getDefault() {
				return def;
			}
			
			/**
			 * Sets the border style for the active edge.
			 * @param value the style
			 * @return returns this builder view
			 */
			public BuilderView style(Style value) {
				spec.style(value);
				return this;
			}
			
			/**
			 * Sets the border alignment for the active edge.
			 * @param value the alignment
			 * @return returns this builder view
			 */
			public BuilderView align(Align value) {
				spec.align(value);
				return this;
			}
			/**
			 * Sets the border width for the active edge.
			 * @param value the width
			 * @return returns this builder view
			 */
			public BuilderView width(Integer value) {
				spec.width(value);
				return this;
			}
			/**
			 * Creates a new border using the current state of the builder
			 * @return returns a new border
			 */
			public Border build() {
				return new Border(Builder.this);
			}
		}
	}

	private Border(Builder builder) {
		BorderSpecification def = builder.def.spec.build();
		this.top = builder.top.spec.build(def);
		this.bottom = builder.bottom.spec.build(def);
		this.left = builder.left.spec.build(def);
		this.right = builder.right.spec.build(def);
	}

	/**
	 * Gets the specification for this border's top edge.
	 * @return returns the specification
	 */
	public BorderSpecification getTop() {
		return top;
	}

	/**
	 * Gets the specification for this border's left edge.
	 * @return returns the specification
	 */
	public BorderSpecification getLeft() {
		return left;
	}

	/**
	 * Gets the specification for this border's right edge.
	 * @return returns the specification
	 */
	public BorderSpecification getRight() {
		return right;
	}

	/**
	 * Gets the specification for this border's bottom edge.
	 * @return returns the specification
	 */
	public BorderSpecification getBottom() {
		return bottom;
	}

}
