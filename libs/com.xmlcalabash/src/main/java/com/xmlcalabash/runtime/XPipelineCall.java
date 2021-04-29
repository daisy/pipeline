package com.xmlcalabash.runtime;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.model.*;
import com.xmlcalabash.util.XProcMessageListenerHelper;

import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 8, 2008
 * Time: 5:25:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class XPipelineCall extends XAtomicStep {
    private DeclareStep decl = null;

    public XPipelineCall(XProcRuntime runtime, Step step, XCompoundStep parent) {
        super(runtime, step, parent);
    }

    public void setDeclaration(DeclareStep decl) {
        this.decl = decl;
    }

    public XCompoundStep getParent() {
        return parent;
    }


    public void run() throws SaxonApiException {
        logger.trace("Running " + step.getType());

        decl.setup();

        if (runtime.getError() != null) {
            throw runtime.getError().copy();
        }

        XRootStep root = new XRootStep(runtime);
        XPipeline newstep = new XPipeline(runtime, decl, root, getLocation());

        newstep.instantiate(decl);

        // Calculate all the options
        inScopeOptions = parent.getInScopeOptions();

        HashSet<QName> pipeOpts = new HashSet<QName> ();
        for (QName name : newstep.step.getOptions()) {
            pipeOpts.add(name);
        }

        for (QName name : step.getOptions()) {
            Option option = step.getOption(name);
            RuntimeValue value = computeValue(option);
            setOption(name, value);

            if (pipeOpts.contains(name)) {
                newstep.passOption(name, value);
            }

            inScopeOptions.put(name, value);
        }

        for (QName name : step.getParameters()) {
            Parameter param = step.getParameter(name);
            RuntimeValue value = computeValue(param);

            String port = param.getPort();
            if (port == null) {
                newstep.setParameter(name, value);
            } else {
                newstep.setParameter(port, name, value);
            }
        }

        for (String port : inputs.keySet()) {
            if (!port.startsWith("|")) {
                newstep.inputs.put(port, inputs.get(port));
            }
        }

        for (String port : outputs.keySet()) {
            if (!port.endsWith("|")) {
                newstep.outputs.put(port, outputs.get(port));
            }
        }

        runtime.start(this);
        XProcMessageListenerHelper.openStep(runtime, this);

        // temporarily clear the list of in scope XSLT functions as we're gonna invoke another step
        // (with a new scope).
        List<FunctionLibrary> inscopeXsltFunctions
            = new ArrayList<FunctionLibrary>(runtime.getConfiguration().inscopeXsltFunctions);
        runtime.getConfiguration().inscopeXsltFunctions.clear();

        try {
            newstep.run();
        } finally {
            // restore the in scope XSLT functions
            runtime.getConfiguration().inscopeXsltFunctions.addAll(inscopeXsltFunctions);
            for (XdmNode doc : newstep.errors()) {
                reportError(doc);
            }
            runtime.getMessageListener().closeStep();
        }
        runtime.finish(this);

    }
}