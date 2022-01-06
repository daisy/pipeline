package com.xmlcalabash.runtime;

import com.xmlcalabash.io.*;
import com.xmlcalabash.model.Log;
import com.xmlcalabash.model.Output;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcException;
import net.sf.saxon.s9api.XdmNode;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 7, 2008
 * Time: 8:00:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class XOutput {
    private final XProcRuntime runtime;
    private final String port;
    private final XdmNode node;
    private final boolean sequenceOk;
    private final Pipe writer;
    private boolean writerReturned = false;
    private final Vector<ReadablePipe> readers;

    public XOutput(XProcRuntime runtime, Output output) {
        this.runtime = runtime;
        node = output.getNode();
        port = output.getPort();
        sequenceOk = output.getSequence();
        writer = new Pipe(runtime);
        writer.canWriteSequence(sequenceOk);
        readers = new Vector<ReadablePipe> ();
    }

    public void setLogger(Log log) {
        writer.documents().setLogger(log);
    }

    public XdmNode getNode() {
        return node;
    }

    public String getPort() {
        return port;
    }

    public boolean getSequence() {
        return sequenceOk;
    }

    public ReadablePipe getReader() {
        ReadablePipe pipe = new ReadOnlyPipe(runtime, writer.documents());
        readers.add(pipe);
        return pipe;
    }

    public Pipe getWriter() {
        if (writerReturned) {
            throw new XProcException(node, "Attempt to create two writers for the same output.");
        }
        return writer;
    }
}
