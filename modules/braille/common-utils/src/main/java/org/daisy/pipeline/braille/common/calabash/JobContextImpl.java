package org.daisy.pipeline.braille.common.calabash;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRunnable;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.braille.common.JobContext.AbstractJobContext;

public class JobContextImpl extends AbstractJobContext {
	
	private static final long serialVersionUID = 1L;
	
	private final XProcMessageListener listener;
	private final XProcRunnable step;
	private final XdmNode node;
	
	public JobContextImpl(XProcMessageListener listener) {
		this(listener, null, null);
	}
	
	public JobContextImpl(XProcMessageListener listener, XProcRunnable step, XdmNode node) {
		this.listener = listener;
		this.step = step;
		this.node = node;
	}
	
	@Override
	public String toString() {
		return toStringHelper("o.d.p.b.c.calabash.JobContextImpl").add("listener", listener).toString();
	}
	
	public boolean isTraceEnabled() {
		return true;
	}
	
	public boolean isDebugEnabled() {
		return true;
	}
	
	public boolean isInfoEnabled() {
		return true;
	}
	
	public boolean isWarnEnabled() {
		return true;
	}
	
	public boolean isErrorEnabled() {
		return true;
	}
	
	protected void doTrace(String msg) {
		listener.finer(step, node, msg);
	}
	
	protected void doDebug(String msg) {
		listener.fine(step, node, msg);
	}
	
	protected void doInfo(String msg) {
		listener.info(step, node, msg);
	}
	
	protected void doWarn(String msg) {
		listener.warning(step, node, msg);
	}
	
	protected void doError(String msg) {
		listener.error(step, node, msg, null);
	}
}
