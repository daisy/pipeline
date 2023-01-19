package org.daisy.pipeline.job;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import org.daisy.common.messaging.MessageBus;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.script.XProcScript;

public abstract class AbstractJobContext {

        protected XProcInput input;
        protected XProcOutput output;
        protected XProcScript script;
        protected JobId id;
        protected JobBatchId batchId;
        protected MessageBus messageBus;
        protected JobMonitor monitor;
        protected URI logFile;
        protected URIMapper resultMapper;
        protected JobResultSet results;
        protected String niceName;
        protected Client client;
        protected List<Consumer<Job.Status>> statusListeners;

        // used by DefaultJobBuilder and PersistentJobContext
        protected AbstractJobContext() {}

        // used by PersistentJobContext
        protected AbstractJobContext(AbstractJobContext from) {
                if (from == null)
                        throw new IllegalArgumentException();
                this.client = from.client;
                this.id = from.id;
                this.batchId = from.batchId;
                this.niceName = from.niceName;
                this.logFile = from.logFile;
                this.results = from.results;
                this.script = from.script;
                this.input = from.input;
                this.output = from.output;
                this.resultMapper = from.resultMapper;
                this.monitor = from.monitor;
                this.messageBus = from.messageBus;
                this.statusListeners = from.statusListeners;
        }

        public URI getLogFile() {
                return logFile;
        }

        public JobMonitor getMonitor() {
                return monitor;
        }

        public XProcScript getScript() {
                return script;
        }

        public JobId getId() {
                return id;
        }

        public JobResultSet getResults() {
                return results;
        }

        public String getName() {
                return niceName;
        }

        public Client getClient() {
                return client;
        }

        public JobBatchId getBatchId() {
                return batchId;
        }
}
