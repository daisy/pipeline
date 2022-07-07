/*
 * ReadablePipe.java
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

package com.xmlcalabash.io;

import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.SaxonApiException;
import com.xmlcalabash.model.Step;

/**
 *
 * @author ndw
 */
public interface ReadablePipe {
    public void canReadSequence(boolean sequence);
    public boolean readSequence();
    public XdmNode read() throws SaxonApiException;
    public void setReader(Step step);
    public void setNames(String stepName, String portName);
    public void resetReader();
    public boolean moreDocuments() throws SaxonApiException;
    public int documentCount() throws SaxonApiException;
    public ReadableDocumentSequence documents();
}
