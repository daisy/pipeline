package org.daisy.pipeline.job;

import java.util.Properties;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.Message.MessageBuilder;
import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.job.impl.JobUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;

/**
 * The Class Job defines the execution unit.
 */
public class Job implements RuntimeConfigurator.EventBusable{

        private static final Logger logger = LoggerFactory
                        .getLogger(Job.class);


        public static class  JobBuilder{
                protected JobContext ctxt;
                protected EventBus bus;
                protected Priority priority=Priority.MEDIUM;
                private Job job;

                public JobBuilder withPriority(Priority priority){
                        this.priority=priority;
                        return this;
                }

                public JobBuilder withContext(JobContext ctxt){
                        this.ctxt=ctxt;
                        return this;
                }

                public JobBuilder withEventBus(EventBus bus){
                        this.bus=bus;
                        return this;
                }
                //available for subclasses to override
                protected Job initJob(){
                        Job job = new Job(this.ctxt,this.bus,this.priority);
                        return job;
                }
                public final Job build(){
                        this.job=this.initJob();
                        this.job.changeStatus(Status.IDLE);
                        return job;
                }
                
                /**
                 * Builds the job allowing a preprocessing before the status is 
                 * broadcasted
                 * @param initialiser
                 * @return
                 */
                public final Job build(Function<Job,Job> initialiser){
                        this.job= initialiser.apply(this.initJob());
                        this.job.changeStatus(Status.IDLE);
                        return job;
                }
                public final void initialise(){
                }
        }


        /**
         * The Enum Status.
         */


        public static enum Status {

                /** The IDLE. */
                IDLE,
                /** The RUNNING. */
                RUNNING,
                /** The DONE. */
                DONE,
                /** The ERROR. */
                ERROR,
                /** The VALIDATION_FAIL */
                VALIDATION_FAIL
        }


        /** The status. */
        private volatile Status status = Status.IDLE;
        private Priority priority;
        protected JobContext ctxt;
        private EventBus eventBus;

        protected Job(JobContext ctxt,EventBus eventBus,Priority priority) {
                this.ctxt=ctxt;
                this.eventBus=eventBus;
                this.priority=priority;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public JobId getId() {
                return this.ctxt.getId();
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        public Status getStatus() {
                synchronized(this.status){
                        return status;
                }
        }

        protected void setStatus(Status status){
                synchronized(this.status){
                        this.status=status;
                }
        }

        /**
         * @return the priority
         */
        public Priority getPriority() {
                return priority;
        }

        /**
         * @return the priority
         */
        protected void setPriority(Priority priority) {
                this.priority=priority;
        }

        /**
         * @param eventBus the eventBus to set
         */
        public void setEventBus(EventBus eventBus) {
                this.eventBus = eventBus;
        }

        /**
         * Gets the ctxt for this instance.
         *
         * @return The ctxt.
         */
        public JobContext getContext() {
                return this.ctxt;
        }

        /**
         * Gets the ctxt for this instance.
         *
         * @return The ctxt.
         */
        protected void setContext(JobContext ctxt) {
                this.ctxt=ctxt;
        }

        /**
         * Gets the x proc output.
         *
         * @return the x proc output
         */
        final XProcResult getXProcOutput() {
                return null;
        }

        private synchronized final void changeStatus(Status to){
                logger.info(String.format("Changing job status to: %s",to));
                this.status=to;
                this.onStatusChanged(to);
                System.out.println("CHANGING STATUS IN THE DB BEFORE POSTING IT!");
                if (this.eventBus!=null)
                        this.eventBus.post(new StatusMessage.Builder().withJobId(this.getId()).withStatus(this.status).build());
                else
                        logger.warn("I couldnt broadcast my change of status because"+((this.ctxt==null)? " the context ": " event bus ") + "is null");
        }
        private final void broadcastError(String text){
                Message msg= new MessageBuilder()
                        .withJobId(this.getId().toString())
                        .withLevel(Level.ERROR)
                        .withText(text)
                        .withSequence(1)
                        .build();
                if (this.eventBus!=null)
                        this.eventBus.post(msg);
                else
                        logger.warn("I couldnt broadcast an error "+((this.ctxt==null)? " the context ": " event bus ") + "is null");
        }

        /**
         * Runs the job using the XProcEngine as script loader.
         *
         * @param engine the engine
         */
        public synchronized final void run(XProcEngine engine) {
                changeStatus(Status.RUNNING);
                XProcPipeline pipeline = null;
                try{
                        pipeline = engine.load(this.ctxt.getScript().getXProcPipelineInfo().getURI());
                        Properties props=new Properties();
                        props.setProperty("JOB_ID", this.ctxt.getId().toString());
                        XProcResult results = pipeline.run(this.ctxt.getInputs(),this.ctxt.getMonitor(),props);
                        this.ctxt.writeResult(results);
                        //if the validation fails set the job status
                        if (!this.checkValid()){
                                changeStatus(Status.VALIDATION_FAIL);
                        }else{
                                changeStatus( Status.DONE );
                        }
                }catch(Exception e){
                        changeStatus( Status.ERROR);
                        broadcastError(e.getMessage());
                        logger.error("job finished with error state",e);
		} catch (OutOfMemoryError e) {//this one needs it's own catch!
                        changeStatus( Status.ERROR);
                        broadcastError(e.getMessage());
                        logger.error("job consumed all heap space",e);
                }

        }

        protected void onStatusChanged(Status newStatus){
                //for subclasses
        }
        //checks if the internal validations are ok
        private boolean checkValid(){
                return JobUtils.checkValidPort(this.getContext().getResults());
        }

        @Override
        public boolean equals(Object object) {
                return (object instanceof Job)   && 
                        this.getId().equals(((Job) object).getId());
        }
        
}
