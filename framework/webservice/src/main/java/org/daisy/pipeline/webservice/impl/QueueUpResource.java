package org.daisy.pipeline.webservice.impl;



import org.daisy.pipeline.job.JobQueue;
import org.daisy.pipeline.job.JobId;


public class QueueUpResource extends QueueMoveResource {

        @Override
        public void move(JobQueue queue, JobId id) {
                queue.moveUp(id);

        }

}
