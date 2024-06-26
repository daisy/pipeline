/*
 * DeclareStep.java
 *
 * Copyright 2008 Mark Logic Corporation.
 * Portions Copyright 2007 Sun Microsystems, Inc.
 * All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * https://xproc.dev.java.net/public/CDDL+GPL.html or
 * docs/CDDL+GPL.txt in the distribution. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at docs/CDDL+GPL.txt.
 */

package com.xmlcalabash.model;

import com.xmlcalabash.core.XProcData;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.QName;
import com.xmlcalabash.core.XProcRuntime;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import org.slf4j.Logger;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import org.slf4j.LoggerFactory;

public class DeclareStep extends CompoundStep implements DeclarationScope {
    protected boolean psviRequired = false;
    protected String xpathVersion = "2.0";
    private QName declaredType = null;
    private boolean atomic = true;
    protected Hashtable<QName, DeclareStep> declaredSteps = new Hashtable<QName, DeclareStep> ();
    private List<PipelineLibrary> importedLibs = new ArrayList<>();
    private List<XdmNode> xsltFunctionImports = new ArrayList<>();
    private DeclarationScope parentScope = null;
    private Vector<XdmNode> rest = null;
    private HashSet<String> excludedInlineNamespaces = null;
    private URI sourceImport = null;

    /* Creates a new instance of DeclareStep */
    public DeclareStep(XProcRuntime xproc, XdmNode node, String name) {
        super(xproc, node, XProcConstants.p_declare_step, name);
    }

    protected void setXmlContent(Vector<XdmNode> nodes) {
        rest = nodes;
    }

    protected Vector<XdmNode> getXmlContent() {
        return rest;
    }

    public void setPsviRequired(boolean psvi) {
        psviRequired = psvi;
    }

    public void setXPathVersion(String version) {
        xpathVersion = version;
    }

    public void setDeclaredType(QName type) {
        declaredType = type;
    }

    public void setExcludeInlineNamespaces(HashSet<String> uris) {
        excludedInlineNamespaces = uris;
    }

    public HashSet<String> getExcludeInlineNamespaces() {
        return excludedInlineNamespaces;
    }

    public void setAtomic(boolean isAtomic) {
        atomic = isAtomic;
    }

    public boolean isAtomic() {
        return atomic;
    }

    public boolean isPipeline() {
        return !atomic;
    }

    public QName getDeclaredType() {
        return declaredType;
    }

    public void setParentScope(DeclarationScope decls) {
        parentScope = decls;
    }

    public void declareStep(QName type, DeclareStep step) {
        DeclareStep d = getDeclaration(type);
        if (d != null) {
            if (!d.equals(step))
                throw new XProcException(step, "Duplicate step type: " + type);
        } else {
            declaredSteps.put(type, step);
        }
    }

    public void addImport(PipelineLibrary lib) {
        importedLibs.add(lib);
    }

    public void addXsltFunctionImport(XdmNode lib) {
        xsltFunctionImports.add(lib);
    }

    public void setSourceImport(URI href) {
        sourceImport = href;
    }

    public URI getSourceImport() {
        return sourceImport;
    }

    public DeclareStep getDeclaration() {
        return getDeclaration(declaredType);
    }

    public DeclareStep getDeclaration(QName type) {
        DeclareStep decl = null;
        if (parentScope != null)
            try {
                decl = parentScope.getDeclaration(type);
            } catch (XProcException e) {
                if (XProcConstants.staticError(44).equals(e.getErrorCode())) {
                    // step was not found
                    // throw same exception but with more precise location info
                    throw XProcException.staticError(44, node, "Unexpected step name: " + type);
                } else
                    throw e;
            }
        for (PipelineLibrary lib : importedLibs) {
            DeclareStep d = lib.getDeclaration(type);
            if (d != null) {
                if (decl == null)
                    decl = d;
                else if (!decl.equals(d))
                    throw new XProcException(d, "Duplicate step type: " + type);
            }
        }
        {
            DeclareStep d = declaredSteps.get(type);
            if (d != null) {
                if (decl == null)
                    decl = d;
                else if (!decl.equals(d))
                    throw new XProcException(d, "Duplicate step type: " + type);
            }
        }
        return decl;
    }
    
    public Set<QName> getInScopeTypes() {
        Set<QName> decls = new HashSet<>();
        decls.addAll(declaredSteps.keySet());
        if (parentScope != null)
            decls.addAll(parentScope.getInScopeTypes());
        for (PipelineLibrary lib : importedLibs)
            decls.addAll(lib.getInScopeTypes());
        return decls;
    }

    public List<XdmNode> getXsltFunctionImports() {
        return xsltFunctionImports;
    }

    private void setupEnvironment() {
        setEnvironment(new Environment(this));
    }

    protected void patchEnvironment(Environment env) {
        if (atomic) {
            //nop;
        } else {
            // See if there's exactly one "ordinary" input
            int count = 0;
            Input defin = null;
            boolean foundPrimary = false;
            for (Input input : inputs) {
                if (!input.getPort().startsWith("|") && !input.getParameterInput()) {
                    count++;
                    foundPrimary |= input.getPrimary();

                    if (!input.getPrimary() && input.getPrimarySet()) {
                        // nop; if the port is explicitly marked primary=false, it can't count
                    } else {
                        if (defin == null || input.getPrimary()) {
                            defin = input;
                        }
                    }
                }
            }

            if (count == 1 || foundPrimary) {
                env.setDefaultReadablePort(defin);
            }
        }
    }

    private boolean setup = false;
    public void setup() {
        if (setup) return;
        setup = true;

        XProcRuntime runtime = this.runtime;
        DeclareStep decl = this;
        boolean debug = runtime.getDebug();

        if (decl.psviRequired && !runtime.getPSVISupported()) {
            throw XProcException.dynamicError(22);
        }

        if (debug) {
            logger.trace("=====================================================================================");
            logger.trace("Before augment:");
            decl.dump();
        }

        boolean seenPrimaryDocument = false;
        boolean seenPrimaryParameter = false;
        for (Input input : decl.inputs()) {
            if (!input.getPort().startsWith("|") && input.getPrimary()) {
                if (seenPrimaryDocument && !input.getParameterInput()) {
                    error(XProcException.staticError(30, "At most one primary document input port is allowed"));
                }
                if (seenPrimaryParameter && input.getParameterInput()) {
                    error(XProcException.staticError(30, "At most one primary parameter input port is allowed"));
                }

                if (input.getParameterInput()) {
                    seenPrimaryParameter = true;
                } else {
                    seenPrimaryDocument = true;
                }
            }
        }

        boolean seenPrimary = false;
        for (Output output : decl.outputs()) {
            if (!output.getPort().endsWith("|") && output.getPrimary()) {
                if (seenPrimary) {
                    error(XProcException.staticError(30, "At most one primary output port is allowed"));
                }
                seenPrimary = true;
            }
        }

        if (debug) {
            logger.trace("After binding pipeline inputs and outputs:");
            decl.dump();
        }

        if (subpipeline.size() == 0) {
            error(XProcException.staticError(100, "Declared step has no subpipeline, but is not known.")); // FIXME!
            return;
        }

        decl.augment();

        if (debug) {
            logger.trace("After augment:");
            decl.dump();
        }

        decl.setupEnvironment();

        if (!decl.valid()) {
            if (debug) {
                decl.dump();
            }
            return;
        }

        if (debug) {
            logger.trace("After valid:");
            decl.dump();
        }

        if (!decl.orderSteps()) {
            if (debug) {
                decl.dump();
            }
            return;
        }

        if (debug) {
            logger.trace("After ordering:");
            decl.dump();
        }

        HashSet<QName> vars = new HashSet<QName> ();
        checkDuplicateVars(vars);

        // Are all the primary outputs bound?
        if (!checkOutputBindings()) {
            if (debug) {
                decl.dump();
            }
            return;
        }
    }

    protected boolean checkOutputBindings() {
        HashSet<Output> uboutputs = new HashSet<Output> ();

        for (Step substep : subpipeline) {
            for (Output output : substep.outputs()) {
                if (output.getBinding().size() == 0
                        && !output.getPort().endsWith("|") && !output.getPort().startsWith("#")) {
                    uboutputs.add(output);
                }
            }
        }

        for (Input input : inputs()) {
            for (Binding binding : input.bindings) {
                if (binding.getBindingType() == Binding.PIPE_NAME_BINDING) {
                    PipeNameBinding b = (PipeNameBinding) binding;
                    Output output = env.readablePort(b.getStep(), b.getPort());
                    if (uboutputs.contains(output)) {
                        uboutputs.remove(output);
                    } else {
                        // Doesn't matter. Must be legit but doesn't help us.
                    }
                }
            }
        }

        for (Option option : options()) {
            for (Binding binding : option.bindings) {
                if (binding.getBindingType() == Binding.PIPE_NAME_BINDING) {
                    PipeNameBinding b = (PipeNameBinding) binding;
                    Output output = env.readablePort(b.getStep(), b.getPort());
                    if (uboutputs.contains(output)) {
                        uboutputs.remove(output);
                    } else {
                        // Doesn't matter. Must be legit but doesn't help us.
                    }
                }
            }
        }

        for (Parameter param : parameters()) {
            for (Binding binding : param.bindings) {
                if (binding.getBindingType() == Binding.PIPE_NAME_BINDING) {
                    PipeNameBinding b = (PipeNameBinding) binding;
                    Output output = env.readablePort(b.getStep(), b.getPort());
                    if (uboutputs.contains(output)) {
                        uboutputs.remove(output);
                    } else {
                        // Doesn't matter. Must be legit but doesn't help us.
                    }
                }
            }
        }

        for (Step substep : subpipeline) {
            substep.checkForBindings(uboutputs);
        }

        boolean valid = true;
        Iterator<Output> outputIter = uboutputs.iterator();
        while (outputIter.hasNext()) {
            Output output = outputIter.next();
            if (output.getPrimary()) {
                error(new XProcException(new QName("", "ERR"), "Unbound primary output: " + output));
                valid = false;
            }
        }

        return valid;
    }

    protected boolean checkBinding(Input input) {
        boolean valid = true;

        // Note: it's ok for there to be no input bindings on a declare-step; the
        // bindings come from the caller

        if (input.getBinding().size() == 0) {
            Port port = null;

            if ("#xpath-context".equals(input.getPort())) {
                if (this instanceof When) {
                    // Manufacture the right port
                    port = new Port(runtime,getNode());
                    port.setStep(parent);
                    port.setPort("#xpath-context");
                } else {
                    port = env.getDefaultReadablePort();
                }
            }

            if ("#iteration-source".equals(input.getPort())
                    || "#viewport-source".equals(input.getPort())) {
                port = env.getParent().getDefaultReadablePort();
            }

            // Check if the declaration has a default binding for this port
            Vector<Binding> declBinding = null;
            // FIXME: is this right?
            if (XProcConstants.p_pipeline.equals(getType())) {
                Step decl = declaration;
                for (Input dinput : decl.inputs()) {
                    if (dinput.getPort().equals(input.getPort())) {
                        declBinding = dinput.getBinding();
                    }
                }
            }

            if (input.getPrimary() && input.getPort().startsWith("|") && subpipeline.size() > 0) {
                // This needs to be bound to the output of the last step.
                Step substep = subpipeline.get(subpipeline.size()-1);
                port = substep.getDefaultOutput();

                if (port == null) {
                    error(
                        XProcException.staticError(
                            5, "Output port '" + input.getPort().substring(1) + "' on " + getStep() + " unbound"));
                    valid = false;
                }
            }

            // FIXME: Is this right? We don't want to steal the root input/output bindings, but
            // we do want to steal all the others, right?
            Output output = null;
            if (input.getPort().startsWith("|") && parent != null) {
                String oport = input.getPort().substring(1);
                output = getOutput(oport);
            }

            if (output != null && output.getBinding().size() > 0) {
                // For |result, we want to copy result's bindings over
                for (Binding binding : output.getBinding()) {
                    input.addBinding(binding);
                }
                output.clearBindings();
            } else if (port == null) {
                if (declBinding != null) {
                    for (Binding binding : declBinding) {
                        input.addBinding(binding);
                    }
                } else if (input.getParameterInput()) {
                    EmptyBinding empty = new EmptyBinding();
                    input.addBinding(empty);
                } else {
                    // FIXME: is this right?
                }
            } else {
                String stepName = port.getStep().getName();
                String portName = port.getPort();

                PipeNameBinding binding = new PipeNameBinding(runtime, node);
                binding.setStep(stepName);
                binding.setPort(portName);

                input.addBinding(binding);
            }
        } else if (input.getParameterInput()) {
            XProcData data = runtime.getXProcData();
            // If depth==0 then we're on a declare step and you aren't allowed to
            // provide default bindings for parameter input ports.
            if (data.getDepth() == 0 && input.getBinding().size() > 0) {
                throw XProcException.staticError(35, input.getNode(), "You must not specify bindings in this context.");
            }
        }

        for (Binding binding : input.getBinding()) {
            if (binding.getBindingType() == Binding.PIPE_NAME_BINDING) {
                PipeNameBinding pipe = (PipeNameBinding) binding;

                // FIXME: This seems like an ugly special case
                Step step = env.visibleStep(pipe.getStep());
                if ((step instanceof Catch && "error".equals(pipe.getPort()))
                        || (step instanceof Choose && "#xpath-context".equals(pipe.getPort()))) { 
                    // then that's ok
                } else {
                    Output output = env.readablePort(pipe.getStep(), pipe.getPort());
                    if (output == null) {
                        error(
                            new XProcException(
                                XProcException.err_E0001, "Unreadable port: " + pipe.getPort() + " on " + pipe.getStep()));
                        valid = false;
                    }
                }
            }
        }

        return valid;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (this.sourceImport == null)
            return false;
        if (!(o instanceof DeclareStep))
            return false;
        DeclareStep that = (DeclareStep)o;
        if (!this.declaredType.equals(that.declaredType))
            return false;
        if (!this.sourceImport.equals(that.sourceImport))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + declaredType.hashCode();
        result = prime * result + ((sourceImport == null) ? 0 : sourceImport.hashCode());
        return result;
    }
}
