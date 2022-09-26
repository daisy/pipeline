/*
 * Pipe.java
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

import java.util.ArrayList;
import java.util.List;

import com.xmlcalabash.core.XProcRunnable;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.util.MessageFormatter;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 *
 * @author ndw
 */
public class Pipe extends ReadOnlyPipe implements ReadablePipe, WritablePipe {

    private ListenableDocumentSequence documents = null;
    private boolean writeSeqOk = false;
    private int size = 0;
    private Step writer = null;

    /* Creates a new instance of Pipe */
    public Pipe(XProcRuntime xproc) {
        this(xproc, new ListenableDocumentSequence(xproc));
    }

    private Pipe(XProcRuntime xproc, ListenableDocumentSequence documents) {
        super(xproc, documents);
        this.documents = documents; // we know this is an empty sequence
        size = 0;
    }

    public void setWriter(Step step) {
        writer = step;
    }

    public void canWriteSequence(boolean sequence) {
        writeSeqOk = sequence;
    }

    public void resetWriter() {
        documents.reset();
        size = 0;
        pos = 0;
    }

    public boolean writeSequence() {
        return writeSeqOk;
    }

    public boolean closed() {
        return documents.closed();
    }

    public void close() {
        documents.close();
    }

    public void write(XdmNode doc) {
        if (writer != null) {
            logger.trace(
                MessageFormatter.nodeMessage(
                    writer.getNode(),
                    writer.getName() + " wrote '" + (doc == null ? "null" : doc.getBaseURI()) + "' to " + this));
        }
        documents.add(doc);
        size++;
        if (size > 1 && !writeSeqOk) {
            dynamicError(7);
        }
    }

    public void onRead(XProcRunnable runnable) {
        if (documents.runOnRead == null) {
            documents.runOnRead = new ArrayList<>();
        }
        documents.runOnRead.add(runnable);
    }

    private static class ListenableDocumentSequence extends DocumentSequence {

        List<XProcRunnable> runOnRead = null;

        ListenableDocumentSequence(XProcRuntime xproc) {
            super(xproc);
        }

        @Override
        void beforeRead() throws SaxonApiException {
            if (runOnRead != null) {
                List<XProcRunnable> r = new ArrayList(runOnRead);
                runOnRead = null;
                for (XProcRunnable rr : r) {
                    rr.run();
                }
                // check if the callbacks registered other callbacks, and if so run them (because we are still about to read)
                if (runOnRead != null) {
                    beforeRead();
                }
                if (runOnRead != null) {
                    r.addAll(runOnRead);
                }
                runOnRead = r;
            }
            // assert that no more documents are written after the pipe has been read
            close();
        }

        @Override
        public void reset() {
            super.reset();
            runOnRead = null;
        }
    }
}
