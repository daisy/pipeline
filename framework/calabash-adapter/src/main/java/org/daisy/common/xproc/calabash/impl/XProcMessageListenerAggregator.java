package org.daisy.common.xproc.calabash.impl;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRunnable;

// TODO: Auto-generated Javadoc
/**
 * Aggregates a collection of {@link XProcMessageListener} rerouting the events listened
 */
public class XProcMessageListenerAggregator implements XProcMessageListener{

	/** The m listeners. */
	List<XProcMessageListener> mListeners = new LinkedList<XProcMessageListener>();


	/**
	 * Adds a new listener
	 *
	 * @param listener the listener
	 */
	public void add(XProcMessageListener listener){
		mListeners.add(listener);
	}




	/**
	 * Removes the given listener
	 *
	 * @param listener the listener
	 */
	public void remove(XProcMessageListener listener){
		mListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#error(java.lang.Throwable)
	 */
	@Override
	public void error(Throwable throwable) {
		for(XProcMessageListener l:mListeners){
			l.error(throwable);
		}

	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#error(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String, net.sf.saxon.s9api.QName)
	 */
	@Override
	public void error(XProcRunnable runnable, XdmNode xnode, String str, QName qname) {
		for(XProcMessageListener l:mListeners){
			l.error(runnable,xnode,str,qname);
		}

	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#fine(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void fine(XProcRunnable arg0, XdmNode arg1, String arg2) {
		for(XProcMessageListener l:mListeners){
			l.fine(arg0,arg1,arg2);
		}

	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#finer(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void finer(XProcRunnable arg0, XdmNode arg1, String arg2) {
		for(XProcMessageListener l:mListeners){
			l.finer(arg0,arg1,arg2);
		}
	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#finest(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void finest(XProcRunnable arg0, XdmNode arg1, String arg2) {
		for(XProcMessageListener l:mListeners){
			l.finest(arg0,arg1,arg2);
		}

	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#info(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void info(XProcRunnable arg0, XdmNode arg1, String arg2) {
		for(XProcMessageListener l:mListeners){
			l.info(arg0,arg1,arg2);
		}
	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#warning(com.xmlcalabash.core.XProcRunnable, net.sf.saxon.s9api.XdmNode, java.lang.String)
	 */
	@Override
	public void warning(XProcRunnable arg0, XdmNode arg1, String arg2) {
		for(XProcMessageListener l:mListeners){
			l.warning(arg0,arg1,arg2);
		}

	}

	/* (non-Javadoc)
	 * @see com.xmlcalabash.core.XProcMessageListener#warning(java.lang.Throwable)
	 */
	@Override
	public void warning(Throwable throwable) {
		for(XProcMessageListener l:mListeners){
			l.warning(throwable);
		}

	}

	@Override
	public void openStep(XProcRunnable step, XdmNode node, String message, String level, BigDecimal portion) {
		for (XProcMessageListener l : mListeners){
			l.openStep(step, node, message, level, portion);
		}
	}

	@Override
	public void closeStep() {
		for (XProcMessageListener l : mListeners){
			l.closeStep();
		}
	}
}
