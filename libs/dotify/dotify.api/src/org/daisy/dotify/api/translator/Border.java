package org.daisy.dotify.api.translator;

import org.daisy.dotify.api.translator.BorderSpecification.Align;
import org.daisy.dotify.api.translator.BorderSpecification.Style;

public class Border {
	private final BorderSpecification top;
	private final BorderSpecification left;
	private final BorderSpecification right;
	private final BorderSpecification bottom;
	
	public static class Builder {
		private final BuilderView def;
		private final BuilderView top;
		private final BuilderView left;
		private final BuilderView right;
		private final BuilderView bottom;
		
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
		
		public BuilderView getTop() {
			return top;
		}

		public BuilderView getLeft() {
			return left;
		}

		public BuilderView getRight() {
			return right;
		}

		public BuilderView getBottom() {
			return bottom;
		}
		
		public BuilderView getDefault() {
			return def;
		}

		public Border build() {
			return new Border(this);
		}
		
		public class BuilderView {
			private final BorderSpecification.Builder spec;
			private BuilderView(BorderSpecification.Builder spec) {
				this.spec = spec;
			}
			public BuilderView getTop() {
				return top;
			}

			public BuilderView getLeft() {
				return left;
			}

			public BuilderView getRight() {
				return right;
			}

			public BuilderView getBottom() {
				return bottom;
			}
			
			public BuilderView getDefault() {
				return def;
			}
			
			public BuilderView style(Style value) {
				spec.style(value);
				return this;
			}
			
			public BuilderView align(Align value) {
				spec.align(value);
				return this;
			}
			public BuilderView width(Integer value) {
				spec.width(value);
				return this;
			}
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

	public BorderSpecification getTop() {
		return top;
	}

	public BorderSpecification getLeft() {
		return left;
	}

	public BorderSpecification getRight() {
		return right;
	}

	public BorderSpecification getBottom() {
		return bottom;
	}

}
