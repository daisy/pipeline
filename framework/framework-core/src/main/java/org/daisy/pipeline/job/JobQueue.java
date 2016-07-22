package org.daisy.pipeline.job;

import java.util.Collection;

import org.daisy.common.priority.Prioritizable;

public interface JobQueue {
        public void moveUp(JobId id);

        public void moveDown(JobId id);

        public void cancel(JobId id);

        public Collection<? extends Prioritizable<Job>> asCollection();
}
