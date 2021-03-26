package com.xmlcalabash.runtime;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.util.XProcMessageListenerHelper;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 13, 2008
 * Time: 7:23:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class XGroup extends XCompoundStep {
    public XGroup(XProcRuntime runtime, Step step, XCompoundStep parent) {
          super(runtime, step, parent);
    }
  
    @Override
    public void run() throws SaxonApiException {
        if (!(parent instanceof XTry)) {
            try {
                inScopeOptions = parent.getInScopeOptions();
                XProcMessageListenerHelper.openStep(runtime, this);
            } catch (Throwable e) {
                throw handleException(e);
            }
            try {
                super.run();
            } finally {
                runtime.getMessageListener().closeStep();
            }
        } else
            super.run();
    }
}
