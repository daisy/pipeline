package org.daisy.pipeline.job;

import java.net.URI;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.daisy.pipeline.job.impl.JobResultSetBuilder;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.XProcScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class defines the common behaviour to jobs contexts, the context will mainly differ depending on the mode of 
 * the WS, local or remote. 
 * The subclasses of JobContext MUST define some fine grained behaviour regarding how the job interacts with the fs and 
 * input,output,option redirections.
 */
public abstract class AbstractJobContext implements JobContext{
        private static final Logger logger = LoggerFactory.getLogger(AbstractJobContext.class);
        /** The input. */
        private XProcInput input;

        /** The output. */
        private XProcOutput output;

        /**Script details*/
        private XProcScript script;

        private JobId id;

        private JobBatchId batchId;
        /** monitor */
        private XProcMonitor monitor;

        private URI logFile;

        private URIMapper mapper;  

        private JobResultSet results;
                
        private String niceName;

        private Client client;
        

        public AbstractJobContext(Client client,JobId id,JobBatchId batchId, String niceName,BoundXProcScript boundScript,URIMapper mapper){
                if(boundScript!=null){
                        this.input=boundScript.getInput();
                        this.script=boundScript.getScript();
                        this.output=boundScript.getOutput();            
                }

                this.client=client;
                this.id=id;
                this.batchId=batchId;
                this.niceName=niceName;
                this.mapper=mapper;

                if(id!=null)
                        this.logFile=JobURIUtils.getLogFile(id);
                else
                        this.logFile=URI.create("");

                this.results=new JobResultSet.Builder().build();
                
        }


        @Override
        public XProcInput getInputs() {
                return this.input;
        }

        @Override
        public XProcOutput getOutputs() {
                return this.output;
        }


        @Override
        public URI getLogFile() {
                return this.logFile;
        }

        protected void setLogFile(URI logFile) {
                this.logFile=logFile;
        }


        /**
         * Gets the mapper for this instance.
         *
         * @return The mapper.
         */
        public URIMapper getMapper() {
                return this.mapper;
        }

        /**
         * Sets the mapper for this instance.
         *
         * @param mapper The mapper.
         */
        protected void setMapper(URIMapper mapper) {
                this.mapper = mapper;
        }

        /**
         * Sets the results for this instance.
         *
         * @param results The results.
         */
        protected void setResults(JobResultSet results) {
                this.results = results;
        }


        /**
         * Sets the id for this instance.
         *
         * @param id The id.
         */
        protected void setId(JobId id) {
                this.id = id;
        }

        @Override
        public XProcMonitor getMonitor() {
                return this.monitor;
        }

        /**
         * Sets the input for this instance.
         *
         * @param input The input.
         */
        protected void setInput(XProcInput input) {
                this.input = input;
        }

        /**
         * Sets the output for this instance.
         *
         * @param output The output.
         */
        protected void setOutput(XProcOutput output)
        {
                this.output = output;
        }

        @Override
        public XProcScript getScript() {
                return this.script;
        }

        /**
         * Sets the script for this instance.
         *
         * @param script The script.
         */
        protected void setScript(XProcScript script) {
                this.script = script;
        }

        @Override
        public JobId getId() {
                return this.id;
        }

        @Override
        public JobResultSet getResults() {
                return this.results;
        }

        @Override
        public void writeResult(XProcResult result) {
                result.writeTo(this.output);
                this.results=JobResultSetBuilder.newResultSet(this,this.mapper);
                                
        }

        public void cleanUp(){
                logger.info(String.format( "Deleting context for job %s" ,this.id));
                JobURIUtils.cleanJobBase(this.id);
        }


        @Override
        public String getName() {
                return niceName;
        }

        protected void setName(String name) {
                this.niceName=name;
        }


        @Override
        public Client getClient() {
                return this.client;
        }

        protected void setClient(Client client) {
                this.client=client;
        }

        @Override
        public void setMonitor(XProcMonitor monitor) {
                this.monitor=monitor;
        }

        @Override
        public JobBatchId getBatchId() {
                return this.batchId;
        }
        public void setBatchId(JobBatchId id) {
                this.batchId=id;
        }

}
