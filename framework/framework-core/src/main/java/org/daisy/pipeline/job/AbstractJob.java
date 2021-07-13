package org.daisy.pipeline.job;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcPipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Job defines the execution unit.
 */
public abstract class AbstractJob implements Job {

        private static final Logger logger = LoggerFactory.getLogger(Job.class);

        private volatile Status status = Status.IDLE;
        protected Priority priority;
        protected AbstractJobContext ctxt;

        protected AbstractJob(AbstractJobContext ctxt, Priority priority) {
                this.ctxt = ctxt;
                this.priority = priority != null ? priority : Priority.MEDIUM;
        }

        @Override
        public Status getStatus() {
                return status;
        }

        protected synchronized void setStatus(Status status) {
                this.status = status;
        }

        @Override
        public Priority getPriority() {
                return priority;
        }

        @Override
        public AbstractJobContext getContext() {
                return this.ctxt;
        }

        protected synchronized final void changeStatus(Status to){
                logger.info(String.format("Changing job status to: %s",to));
                this.status=to;
                this.onStatusChanged(to);
                ctxt.changeStatus(this.status);
        }

        @Override
        public synchronized final void run(XProcEngine engine) {
                changeStatus(Status.RUNNING);
                XProcPipeline pipeline = null;
                if (ctxt.messageBus == null)
                    // This means we've tried to execute a PersistentJob that was read from the
                    // database. Should not happen.
                    throw new RuntimeException();
                try{
                        pipeline = engine.load(this.ctxt.getScript().getXProcPipelineInfo().getURI());
                        if (ctxt.collectResults(pipeline.run(ctxt.input, () -> ctxt.messageBus, null)))
                                changeStatus(Status.SUCCESS);
                        else
                                changeStatus(Status.FAIL);
                } catch (Throwable e) {
                        changeStatus( Status.ERROR);
                        ctxt.messageBus.append(new MessageBuilder()
                                               .withLevel(Level.ERROR)
                                               .withText(e.getMessage() + " (Please see detailed log for more info.)"))
                                  .close();
                        if (e instanceof XProcErrorException) {
                                logger.error("job finished with error state\n" + e.toString());
                                logger.debug("job finished with error state", e);
                        } else
                                logger.error("job finished with error state", e);
		} catch (OutOfMemoryError e) {//this one needs it's own catch!
                        changeStatus( Status.ERROR);
                        ctxt.messageBus.append(new MessageBuilder()
                                               .withLevel(Level.ERROR)
                                               .withText(e.getMessage()))
                                  .close();
                        logger.error("job consumed all heap space",e);
                }

        }

        protected void onStatusChanged(Status newStatus){
                //for subclasses
        }

        @Override
        public boolean equals(Object object) {
                return (object instanceof Job)   && 
                        this.getId().equals(((Job) object).getId());
        }
        
}
