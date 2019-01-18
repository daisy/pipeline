/*
 * XProcException.java
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

package com.xmlcalabash.core;

import com.xmlcalabash.util.S9apiUtils;
import com.xmlcalabash.util.URIUtils;
import com.xmlcalabash.model.Step;

import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trace.ContextStackFrame;
import net.sf.saxon.trace.ContextStackIterator;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.ValidationException;

import javax.xml.transform.dom.DOMLocator;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author ndw
 */
public class XProcException extends RuntimeException {
    public static final QName err_E0001 = new QName(XProcConstants.NS_XPROC_ERROR_EX, "XE0001"); // invalid pipeline
    public static final QName err_E0002 = new QName(XProcConstants.NS_XPROC_ERROR_EX, "XE0002"); // invalid configuration

    private QName error = null;
    private Step step = null;
    private XdmNode node = null;

    /* Creates a new instance of XProcException */
    public XProcException() {
        super();
    }

    /* Creates a new instance of XProcException */
    public XProcException(String message) {
        super(message);
    }
    
    /* Creates a new instance of XProcException */
    public XProcException(Step step) {
        super();
        this.step = step;
    }

    /* Creates a new instance of XProcException */
    public XProcException(Step step, String message) {
        super(message);
        this.step = step;
    }

    /* Creates a new instance of XProcException */
    public XProcException(XdmNode node, String message) {
        super(message);
        this.node = node;
    }

    /* Creates a new instance of XProcException */
    public XProcException(String message, Throwable cause) {
        super(message, cause);
    }

    /* Creates a new instance of XProcException */
    public XProcException(Step step, String message, Throwable cause) {
        super(message, cause);
        this.step = step;
    }

    /* Creates a new instance of XProcException */
    public XProcException(Throwable cause) {
        super(cause);
    }

    /* Creates a new instance of XProcException */
    public XProcException(XdmNode node, Throwable cause) {
        super(cause);
        this.node = node;
    }

    /* Creates a new instance of XProcException */
    public XProcException(XdmNode node, String message, Throwable cause) {
        super(message, cause);
        this.node = node;
    }

    public XProcException(QName errorCode) {
    	super(errorCode.getLocalName());
        error = errorCode;
    }

    public XProcException(Step step, QName errorCode) {
    	super(errorCode.getLocalName());
        error = errorCode;
        this.step = step;
    }

    public XProcException(QName errorCode, String message) {
        super(message);
        error = errorCode;
    }

    public XProcException(Step step, QName errorCode, String message) {
        super(message);
        error = errorCode;
        this.step = step;
    }

    public XProcException(QName errorCode, XdmNode node, Throwable cause, String message) {
        super(message,cause);
        error = errorCode;
        this.node = node;
    }

    public XProcException(QName errorCode, XdmNode node, String message) {
        super(message);
        error = errorCode;
        this.node = node;
    }

    public XProcException(Step step, QName errorCode, XdmNode node, String message) {
        super(message);
        error = errorCode;
        this.step = step;
        this.node = node;
    }
    
    public XProcException(QName errorCode, Throwable cause) {
        super("XProc error err:" + errorCode.getLocalName(), cause);
        error = errorCode;
    }

    public static XProcException staticError(int errno) {
        return new XProcException(XProcConstants.staticError(errno));
    }

    public static XProcException staticError(int errno, String message) {
        return new XProcException(XProcConstants.staticError(errno), message);
    }

    public static XProcException staticError(int errno, XdmNode node, String message) {
        return new XProcException(XProcConstants.staticError(errno), node, message);
    }

    public static XProcException staticError(int errno, XdmNode node, Throwable cause, String message) {
        return new XProcException(XProcConstants.staticError(errno), node, cause, message);
    }

    public static XProcException staticError(int errno, Exception except) {
        return new XProcException(XProcConstants.staticError(errno), except);
    }

    public static XProcException dynamicError(int errno) {
        return new XProcException(XProcConstants.dynamicError(errno));
    }

    public static XProcException dynamicError(Step step, int errno) {
        return new XProcException(step, XProcConstants.dynamicError(errno));
    }

    public static XProcException dynamicError(int errno, String message) {
        return new XProcException(XProcConstants.dynamicError(errno), message);
    }

    public static XProcException dynamicError(int errno, XdmNode node, String message) {
        return new XProcException(XProcConstants.dynamicError(errno), node, message);
    }

    public static XProcException dynamicError(int errno, XdmNode node, Exception except, String message) {
        return new XProcException(XProcConstants.dynamicError(errno), node, except, message);
    }

    public static XProcException dynamicError(Step step, int errno, String message) {
        return new XProcException(step, XProcConstants.dynamicError(errno), message);
    }

    public static XProcException dynamicError(int errno, Exception except) {
        return new XProcException(XProcConstants.dynamicError(errno), except);
    }

    public static XProcException stepError(int errno) {
        return new XProcException(XProcConstants.stepError(errno));
    }

    public static XProcException stepError(int errno, String message) {
        return new XProcException(XProcConstants.stepError(errno), message);
    }

    public static XProcException stepError(int errno, Exception except) {
        return new XProcException(XProcConstants.stepError(errno), except);
    }

    /**
     * @param offset Make the locator of this error start "offset" number of frames from
     *               the point where this method is called (a positive number results in
     *               less frames in the locator).
     */
    public static XProcException javaError(Throwable throwable, int offset) {
        StackTraceElement[] base = new RuntimeException().getStackTrace();
        return javaError(throwable, offset, base, 1);
    }
    
    public static XProcException javaError(Throwable throwable, int offset, StackTraceElement[] base, int baseOffset) {
        if (baseOffset < 0)
            throw new IllegalArgumentException();
        final SourceLocator[] location; {
            StackTraceElement[] trace = throwable.getStackTrace();
            int m = trace.length - 1;
            int n = base.length - 1;
            while (m >= 0 && n >= baseOffset && trace[m].equals(base[n])) {
                m--;
                n--;
            }
            if (m >= 0 && n >= baseOffset
                && trace[m].getClassName().equals(base[n].getClassName())
                && Objects.equals(trace[m].getMethodName(), base[n].getMethodName())
                && Objects.equals(trace[m].getFileName(), base[n].getFileName())) {
                m--;
                n--;
            }
            if (n < baseOffset)
                m += (2 - offset);
            else
                m = trace.length;
            location = new SourceLocator[m];
            for (int i = 0; i < m; i++) {
                final StackTraceElement frame = trace[i];
                location[i] = new SourceLocator() {
                    public String getPublicId() {
                        return null;
                    }
                    public String getSystemId() {
                        return frame.getFileName();
                    }
                    public int getLineNumber() {
                        return frame.getLineNumber();
                    }
                    public int getColumnNumber() {
                        return -1;
                    }
                    @Override
                    public String toString() {
                        return frame.toString();
                    }
                };
            }
        }
        Throwable cause = throwable.getCause();
        if (cause != null)
            cause = javaError(cause, offset, base, baseOffset);
        else
            // make the original exception part of the stack trace in order to get some
            // info that might be missing from the XProcException wrapper otherwise
            cause = throwable;
        return new XProcException(throwable.getMessage(), cause) {
            @Override
            public SourceLocator[] getLocator() {
                return location; }};
    }

    public XProcException rebaseOnto(Step step) {
        return rebaseOnto(step, getLocator(step));
    }
    
    public XProcException rebaseOnto(SourceLocator[] base) {
        return rebaseOnto(getStep(), base);
    }
    
    private XProcException rebaseOnto(final Step step, SourceLocator[] base) {
        final QName error = this.getErrorCode();
        final String message = this.getMessage();
        final XdmNode node = this.getNode();
        final SourceLocator[] location; {
            SourceLocator[] l = this.getLocator();
            int originalLength = l.length;
            if (l.length == 1 && l[0] instanceof XMLLocation && getStep() == null && getNode() == null && base.length > 0)
                originalLength = 0;
            location = new SourceLocator[base.length + originalLength];
            int i = 0;
            if (originalLength != 0)
                for (SourceLocator ll : l)
                    location[i++] = ll;
            for (SourceLocator ll : base)
                location[i++] = ll;
        }
        final Throwable cause; {
            Throwable c = this.getCause();
            if (c != null && c instanceof XProcException)
                cause = ((XProcException)c).rebaseOnto(base);
            else
                cause = c;
        }
        return new XProcException() {
            @Override
            public QName getErrorCode() {
                return error;
            }
            @Override
            public String getMessage() {
                return message;
            }
            @Override
            public Step getStep() {
                return step;
            }
            @Override
            public XdmNode getNode() {
                return node;
            }
            @Override
            public SourceLocator[] getLocator() {
                return location;
            }
            @Override
            public Throwable getCause() {
                return cause;
            }
        };
    }

    public QName getErrorCode() {
        return error;
    }

    public Step getStep() {
        return step;
    }

    public XdmNode getNode() {
        return node;
    }

    public SourceLocator[] getLocator() {
        return getLocator(getStep(), getNode());
    }

    private static SourceLocator[] getLocator(Step step) {
        return getLocator(step, null);
    }

    private static SourceLocator[] getLocator(Step step, XdmNode node) {
        List<Step> parents = new ArrayList<Step>(); {
            Step s = step;
            while (s != null) {
                s = s.getParent();
                if (s != null)
                    parents.add(s);
            }
        }
        SourceLocator[] location = new SourceLocator[1 + parents.size()]; {
            if (node == null) {
                if (step != null)
                    node = step.getNode();
            }
            location[0] = new XMLLocation(step, node);
            for (int i = 0; i < parents.size(); i++)
                location[i + 1] = new XMLLocation(parents.get(i).getStep());
        }
        return location;
    }

    public static SourceLocator prettyLocator(final SourceLocator loc, final String instructionName) {
        return new XMLLocationWithInstructionName() {
            public String getPublicId() {
                return loc.getPublicId();
            }
            public String getSystemId() {
                return loc.getSystemId();
            }
            public int getLineNumber() {
                return loc.getLineNumber();
            }
            public int getColumnNumber() {
                return loc.getColumnNumber();
            }
            protected String getInstructionName() {
                return instructionName;
            }
        };
    }

    public static SourceLocator prettyLocator(final String systemId, final int lineNumber, final String instructionName) {
        return new XMLLocationWithInstructionName() {
            public String getPublicId() {
                return null;
            }
            public String getSystemId() {
                return systemId;
            }
            public int getLineNumber() {
                return lineNumber;
            }
            public int getColumnNumber() {
                return -1;
            }
            protected String getInstructionName() {
                return instructionName;
            }
        };
    }
    
    private static class XMLLocation extends XMLLocationWithInstructionName {
        private int line = -1;
        private int col = -1;
        private String systemId = null;
        private QName type;

        public XMLLocation(Step step) {
            this(step, step.getNode());
        }
        
        public XMLLocation(Step step, XdmNode node) {
            if (step != null) {
                type = step.getDeclaredType();
            }
            if (node != null) {
                URI cwd = URIUtils.cwdAsURI();
                systemId = cwd.relativize(node.getBaseURI()).toASCIIString();
                line = node.getLineNumber();
                if (line <= 0) {
                    line = S9apiUtils.getDocumentElement(node).getLineNumber();
                }
                col = node.getColumnNumber();
            }
        }

        public String getPublicId() {
            return null;
        }

        public String getSystemId() {
            return systemId;
        }

        public int getLineNumber() {
            return line;
        }

        public int getColumnNumber() {
            return col;
        }

        protected String getInstructionName() {
            if (type != null)
                return type.getClarkName();
            else
                return null;
        }
    }

    private static abstract class XMLLocationWithInstructionName implements SourceLocator {

        protected abstract String getInstructionName();

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            String instructionName = getInstructionName();
            String fileName = getSystemId();
            if (fileName != null && !"".equals(fileName)) {
                if (fileName.lastIndexOf('/') >= 0)
                    fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                s.append(fileName);
                int line = getLineNumber();
                if (line > 0)
                    s.append(":" + getLineNumber());
            }
            if (instructionName != null && !"".equals(instructionName)) {
                if (s.length() > 0)
                    s.insert(0, instructionName + "(").append(")");
                else
                    s.append(instructionName);
            } else if (s.length() == 0)
                s.append("?");
            return s.toString();
        }
    }

    public static SourceLocator[] getLocator(TransformerException e) {

        // This code is inspired by StandardErrorListener
        List<SourceLocator> frames = new ArrayList<SourceLocator>();
        SourceLocator loc = e.getLocator();
        if (loc == null) {
            TransformerException err = e;
            while (loc == null) {
                if (err.getException() instanceof TransformerException) {
                    err = (TransformerException)err.getException();
                    loc = err.getLocator();
                } else if (err.getCause() instanceof TransformerException) {
                    err = (TransformerException)err.getCause();
                    loc = err.getLocator();
                } else {
                    break;
                }
            }
        }
        if (loc == null)
            loc = ExplicitLocation.UNKNOWN_LOCATION;
        if (loc instanceof XPathParser.NestedLocation)
            loc = ((XPathParser.NestedLocation)loc).getContainingLocation();
        String instructionName = getInstructionName(loc);
        if (instructionName == null && e instanceof TerminationException)
            instructionName = "xsl:message";
        loc = XProcException.prettyLocator(loc, instructionName);
        frames.add(loc);

        // now add more frames based on XPathContext
        if (e instanceof XPathException) {
            XPathException xe = (XPathException)e;
            XPathContext ctxt = xe.getXPathContext();
            if (ctxt != null) {
                Iterator<ContextStackFrame> ff = new ContextStackIterator(xe.getXPathContext());
                while (ff.hasNext()) {
                    ContextStackFrame f = ff.next();
                    instructionName = getInstructionName(f);
                    if (instructionName != null)
                        frames.add(XProcException.prettyLocator(f.getSystemId(), f.getLineNumber(), instructionName));
                }
            }
        }
        return frames.toArray(new SourceLocator[frames.size()]);
    }

    private static String getInstructionName(SourceLocator loc) {
        if (loc instanceof AttributeLocation) {
            return ((AttributeLocation)loc).getElementName().getDisplayName() + "/@"
                + ((AttributeLocation)loc).getAttributeName();
        } else if (loc instanceof DOMLocator) {
            return ((DOMLocator)loc).getOriginatingNode().getNodeName();
        } else if (loc instanceof NodeInfo) {
            return ((NodeInfo)loc).getDisplayName();
        } else if (loc instanceof ValidationException && ((ValidationException)loc).getNode() != null) {
            return (((ValidationException)loc).getNode()).getDisplayName();
        } else if (loc instanceof Instruction) {
            return StandardErrorListener.getInstructionName((Instruction)loc);
        } else if (loc instanceof Actor) {
            return getInstructionName((Actor)loc);
        } else {
            return null;
        }
    }

    private static String getInstructionName(ContextStackFrame frame) {
        if (frame instanceof ContextStackFrame.FunctionCall) {
            StructuredQName name = ((ContextStackFrame.FunctionCall)frame).getFunctionName();
            if (name != null)
                return name.getClarkName() + "()";
        } else if (frame instanceof ContextStackFrame.ApplyTemplates) {
            String name = "xsl:apply-templates";
            Item node = frame.getContextItem();
            if (node instanceof NodeInfo)
                name += " processing " + Navigator.getPath((NodeInfo)node);
            return name;
        } else if (frame instanceof ContextStackFrame.CallTemplate) {
            return "xsl:call-template name=\""
                + ((ContextStackFrame.CallTemplate)frame).getTemplateName().getDisplayName() + "\"";
        } else if (frame instanceof ContextStackFrame.VariableEvaluation) {
            Object container = frame.getContainer();
            if (container instanceof Actor) {
                return getInstructionName((Actor)container);
            } else if (container instanceof TemplateRule) {
                return "xsl:template match=\"" + ((TemplateRule)container).getMatchPattern().toString() + "\"";
            }
        }
        return null;
    }

    private static String getInstructionName(Actor actor) {
        StructuredQName name = actor.getObjectName();
        String objectName = name == null ? "" : name.getDisplayName();
        if (actor instanceof UserFunction) {
            return "function " + objectName + "()";
        } else if (actor instanceof NamedTemplate) {
            return "template name=\"" + objectName + "\"";
        } else if (actor instanceof AttributeSet) {
            return "attribute-set " + objectName;
        } else if (actor instanceof KeyDefinition) {
            return "key " + objectName;
        } else if (actor instanceof GlobalVariable) {
            StructuredQName qName = ((GlobalVariable)actor).getVariableQName();
            if (qName.hasURI(NamespaceConstant.SAXON_GENERATED_VARIABLE)) {
                return "optimizer-created global variable";
            } else {
                return "global variable $" + qName.getDisplayName();
            }
        } else {
            return "procedure " + objectName;
        }
    }
}
