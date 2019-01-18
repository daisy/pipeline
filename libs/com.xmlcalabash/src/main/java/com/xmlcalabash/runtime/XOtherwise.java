package com.xmlcalabash.runtime;

import java.math.BigDecimal;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.util.XProcMessageListenerHelper;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 13, 2008
 * Time: 4:58:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class XOtherwise extends XCompoundStep {
    public XOtherwise(XProcRuntime runtime, Step step, XCompoundStep parent) {
          super(runtime, step, parent);
    }

    @Override
    public void run() throws SaxonApiException {
        try {
            XProcMessageListenerHelper.openStep(runtime, parent, BigDecimal.ONE);
        } catch (Throwable e) {
            throw handleException(e);
        }
        try {
            super.run();
        } finally {
            runtime.getMessageListener().closeStep();
        }
    }
}
