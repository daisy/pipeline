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
	private XPipeline pipeline = null;
	private HashSet<QName> pipeOpts = null;

    public XPipelineCall(XProcRuntime runtime, Step step, XCompoundStep parent) {
        super(runtime, step, parent);
        decl = step.getDeclaration();
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

        if (pipeline == null) {
            XRootStep root = new XRootStep(runtime);
            pipeline = new XPipeline(runtime, decl, root, getLocation());
            pipeline.instantiate(decl);
            pipeOpts = new HashSet<QName>();
            for (QName name : pipeline.step.getOptions()) {
                pipeOpts.add(name);
            }
            for (String port : inputs.keySet()) {
                if (!port.startsWith("|")) {
                    pipeline.inputs.put(port, inputs.get(port));
                }
            }
            for (String port : outputs.keySet()) {
                if (!port.endsWith("|")) {
                    pipeline.outputs.put(port, outputs.get(port));
                }
            }
        } else {
            pipeline.reset();
        }

        // Calculate all the options
        inScopeOptions = parent.getInScopeOptions();

        for (QName name : step.getOptions()) {
            Option option = step.getOption(name);
            RuntimeValue value = computeValue(option);
            setOption(name, value);

            if (pipeOpts.contains(name)) {
                pipeline.passOption(name, value);
            }

            inScopeOptions.put(name, value);
        }

        for (QName name : step.getParameters()) {
            Parameter param = step.getParameter(name);
            RuntimeValue value = computeValue(param);

            String port = param.getPort();
            if (port == null) {
                pipeline.setParameter(name, value);
            } else {
                pipeline.setParameter(port, name, value);
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
            pipeline.run();
        } finally {
            // restore the in scope XSLT functions
            runtime.getConfiguration().inscopeXsltFunctions.addAll(inscopeXsltFunctions);
            for (XdmNode doc : pipeline.errors()) {
                reportError(doc);
            }
            runtime.getMessageListener().closeStep();
        }
        runtime.finish(this);

    }
}