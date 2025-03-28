package com.xmlcalabash.runtime;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.*;
import com.xmlcalabash.util.MessageFormatter;
import com.xmlcalabash.util.XProcMessageListenerHelper;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.XPathException;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 13, 2008
 * Time: 4:57:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class XWhen extends XCompoundStep {
    public XWhen(XProcRuntime runtime, Step step, XCompoundStep parent) {
          super(runtime, step, parent);
    }

    public boolean shouldRun() throws SaxonApiException {
        String testExpr = ((When) step).getTest();
        NamespaceBinding nsbinding = new NamespaceBinding(runtime, step.getNode());
        Hashtable<QName,RuntimeValue> globals = parent.getInScopeOptions();

        XdmNode doc = null;
        Vector<XdmNode> defaultCollection = null; {
            Iterator<ReadablePipe> xpathContext = inputs.get("#xpath-context").iterator();
            if (!runtime.getAllowSequenceAsContext()) {
                ReadablePipe reader = xpathContext.next();
                if (xpathContext.hasNext()) {
                    throw XProcException.dynamicError(5, step);
                }
                doc = reader.read();
                if (reader.moreDocuments()) {
                    throw XProcException.dynamicError(5, step);
                }
            } else {
                defaultCollection = new Vector<XdmNode>();
                while (xpathContext.hasNext()) {
                    ReadablePipe reader = xpathContext.next();
                    while (reader.moreDocuments()) {
                        if (doc == null) {
                            doc = reader.read();
                            defaultCollection.add(doc);
                        } else {
                            defaultCollection.add(reader.read());
                        }
                    }
                }
            }
        }

        // Surround testExpr with "boolean()" to force the EBV.
        Vector<XdmItem> results = evaluateXPath(doc,
                                                defaultCollection,
                                                nsbinding.getNamespaceBindings(), "boolean(" + testExpr + ")",
                                                globals);

        if (results.size() != 1) {
            throw new XProcException("Attempt to compute EBV in p:when did not return a singleton!?");
        }

        XdmAtomicValue value = (XdmAtomicValue) results.get(0);
        return value.getBooleanValue();
    }

    protected void copyInputs() throws SaxonApiException {
        for (String port : inputs.keySet()) {
            if (!port.startsWith("|") && !"#xpath-context".equals(port)) {
            String wport = port + "|";
                WritablePipe pipe = outputs.get(wport);
                for (ReadablePipe reader : inputs.get(port)) {
                    while (reader.moreDocuments()) {
                        XdmNode doc = reader.read();
                        pipe.write(doc);
                        logger.trace(MessageFormatter.nodeMessage(step.getNode(),
                                "Compound input copy from " + reader + " to " + pipe));
                    }
                }
            }
        }
    }

    @Override
    protected void doRun() throws SaxonApiException {
        XProcMessageListenerHelper.openStep(runtime, this, BigDecimal.ONE, parent.getInScopeOptions());
        try {
            super.doRun();
        } finally {
            runtime.getMessageListener().closeStep();
        }
    }
}
