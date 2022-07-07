package com.xmlcalabash.runtime;

import com.xmlcalabash.io.ReadableDocumentSequence;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.ReadOnlyPipe;
import com.xmlcalabash.io.Pipe;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.model.Input;
import net.sf.saxon.s9api.XdmNode;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 7, 2008
 * Time: 7:38:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class XInput {
    private XProcRuntime runtime = null;
    private String port = null;
    private XdmNode node = null;
    private boolean sequenceOk = false;
    private boolean isParameters = false;
    private Vector<ReadablePipe> readers = null;
    private Pipe writer = null;
    private boolean writerReturned = false;
    private ReadableDocumentSequence documents = null;

    public XInput(XProcRuntime runtime, Input input) {
        this.runtime = runtime;
        node = input.getNode();
        port = input.getPort();
        sequenceOk = input.getSequence();
        isParameters = input.getParameterInput();
        readers = new Vector<ReadablePipe> ();
    }

    public String getPort() {
        return port;
    }

    public XdmNode getNode() {
        return node;
    }

    public ReadablePipe getReader() {
        if (documents == null) {
            writer = new Pipe(runtime);
            documents = writer.documents();
        }
        ReadablePipe pipe = new ReadOnlyPipe(runtime, documents);
        pipe.canReadSequence(sequenceOk);
        readers.add(pipe);
        return pipe;
    }

    public Pipe getWriter() {
        if (writerReturned) {
            throw new XProcException(node, "Attempt to create two writers for the same input.");
        } else if (writer == null) {
            writer = new Pipe(runtime);
            documents = writer.documents();
        }
        writerReturned = true;
        return writer;
    }

    public boolean getSequence() {
        return sequenceOk;
    }

    public boolean getParameters() {
        return isParameters;
    }
}
