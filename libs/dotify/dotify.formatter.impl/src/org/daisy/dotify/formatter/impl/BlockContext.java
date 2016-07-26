package org.daisy.dotify.formatter.impl;

class BlockContext {
	private final int flowWidth;
	private final CrossReferenceHandler refs;
	private final DefaultContext context;
	private final FormatterContext fcontext;

	public BlockContext(int flowWidth, CrossReferenceHandler refs, DefaultContext context, FormatterContext fcontext) {
		this.flowWidth = flowWidth;
		this.refs = refs;
		this.context = context;
		this.fcontext = fcontext;
	}

	public int getFlowWidth() {
		return flowWidth;
	}

	public CrossReferenceHandler getRefs() {
		return refs;
	}

	public DefaultContext getContext() {
		return context;
	}

	public FormatterContext getFcontext() {
		return fcontext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((context == null) ? 0 : context.hashCode());
		result = prime * result + ((fcontext == null) ? 0 : fcontext.hashCode());
		result = prime * result + flowWidth;
		result = prime * result + ((refs == null) ? 0 : refs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		BlockContext other = (BlockContext) obj;
		if (context == null) {
			if (other.context != null) {
				return false;
			}
		} else if (!context.equals(other.context)) {
			return false;
		}
		if (fcontext == null) {
			if (other.fcontext != null) {
				return false;
			}
		} else if (!fcontext.equals(other.fcontext)) {
			return false;
		}
		if (flowWidth != other.flowWidth) {
			return false;
		}
		if (refs == null) {
			if (other.refs != null) {
				return false;
			}
		} else if (!refs.equals(other.refs)) {
			return false;
		}
		return true;
	}
	
	//this.flowWidth!=flowWidth || this.refs != refs || !context.equals(this.context) || this.fcontext != fcontext

}
