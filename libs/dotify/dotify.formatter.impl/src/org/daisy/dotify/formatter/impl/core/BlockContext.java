package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.Space;

public class BlockContext extends DefaultContext {
	private final int flowWidth;
	private final FormatterContext fcontext;
	
	public static class Builder extends DefaultContext.Builder {
		private int flowWidth = 0;
		private FormatterContext fcontext = null;
		
		public Builder(BlockContext base) {
			super(base);
			this.flowWidth = base.flowWidth;
			this.fcontext = base.fcontext;
		}

		protected Builder(DefaultContext base) {
			super(base);
		}

		public Builder flowWidth(int value) {
			this.flowWidth = value;
			return this;
		}
		
		public Builder formatterContext(FormatterContext value) {
			this.fcontext = value;
			return this;
		}

		@Override
		public Builder currentVolume(Integer value) {
			super.currentVolume(value);
			return this;
		}

		@Override
		public Builder currentPage(Integer value) {
			super.currentPage(value);
			return this;
		}

		@Override
		public Builder metaVolume(Integer value) {
			super.metaVolume(value);
			return this;
		}

		@Override
		public Builder metaPage(Integer value) {
			super.metaPage(value);
			return this;
		}

		@Override
		public Builder space(Space value) {
			super.space(value);
			return this;
		}

		public BlockContext build() {
			return new BlockContext(this);
		}
	}
	
	protected BlockContext(Builder builder) {
		super(builder);
		this.flowWidth = builder.flowWidth;
		this.fcontext = builder.fcontext;
	}

	public static BlockContext.Builder from(DefaultContext base) {
		return new BlockContext.Builder(base);
	}

	public static BlockContext.Builder from(BlockContext base) {
		return new BlockContext.Builder(base);
	}

	public int getFlowWidth() {
		return flowWidth;
	}

	public FormatterContext getFcontext() {
		return fcontext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fcontext == null) ? 0 : fcontext.hashCode());
		result = prime * result + flowWidth;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlockContext other = (BlockContext) obj;
		if (fcontext == null) {
			if (other.fcontext != null)
				return false;
		} else if (!fcontext.equals(other.fcontext))
			return false;
		if (flowWidth != other.flowWidth)
			return false;
		return true;
	}

}
