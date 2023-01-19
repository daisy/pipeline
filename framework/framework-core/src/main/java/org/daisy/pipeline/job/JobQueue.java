package org.daisy.pipeline.job;

import java.util.Collection;

import org.daisy.common.priority.Priority;
import org.daisy.common.priority.Prioritizable;

public interface JobQueue {

        public void moveUp(JobId id);

        public void moveDown(JobId id);

        public void cancel(JobId id);

        /**
         * @return the (0-based) position in the queue of the specified job. Returns <code>-1</code>
         *         if the job is not in the queue.
         */
        public int getPositionInQueue(JobId id);

        /**
         * @return the job priority as defined at creation time of the job. Note that this is not
         *         the final calculated priority, which is based on the job priority but also on the
         *         client priority, the time spent in the queue, and whether items in the queue have
         *         been swapped. Returns <code>null</code> if the job is not in the queue.
         */
        public Priority getJobPriority(JobId id);

        /**
         * @return the priority of the client associated with the job.Returns <code>null</code> if
         * the job is not in the queue.
         */
        public Priority getClientPriority(JobId id);

        public Collection<? extends Prioritizable<Job>> asCollection();

}
