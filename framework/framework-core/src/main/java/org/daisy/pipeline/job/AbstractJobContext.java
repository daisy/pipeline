package org.daisy.pipeline.job;

import java.io.File;
import java.net.URI;
import java.util.function.Consumer;
import java.util.List;
import java.util.Map;

import org.daisy.common.messaging.MessageBus;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptInput;

public abstract class AbstractJobContext {

        // accessed in DefaultJobBuilder and PersistentJobContext
        protected JobId id;
        protected JobBatchId batchId;
        protected URI logFile;
        protected String niceName;
        protected Client client;
        protected Script script;
        protected JobMonitor monitor;

        // accessed in DefaultJobBuilder, PersistentJobContext and AbstractJob
        protected ScriptInput input;
        protected File resultDir;
        protected JobResultSet results;

        // accessed in DefaultJobBuilder and AbstractJob
        protected MessageBus messageBus;
        protected List<Consumer<Job.Status>> statusListeners;
        protected Map<String,String> properties;

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
                this.resultDir = from.resultDir;
                this.monitor = from.monitor;
                this.messageBus = from.messageBus;
                this.statusListeners = from.statusListeners;
                this.properties = from.properties;
        }

        public URI getLogFile() {
                return logFile;
        }

        public JobMonitor getMonitor() {
                return monitor;
        }

        public Script getScript() {
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
