package org.daisy.pipeline.job.impl;

import java.util.Collection;

import org.daisy.common.priority.Prioritizable;
import org.daisy.common.priority.PriorityThreadPoolExecutor;
import org.daisy.pipeline.job.Job;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class FilteredJobQueue extends DefaultJobQueue {

        public PriorityThreadPoolExecutor<Job> executor;

        public Predicate<Prioritizable<Job>> filter;

        /**
         * @param queue
         * @param filter
         */
        public FilteredJobQueue(PriorityThreadPoolExecutor<Job> executor,
                        Predicate<Prioritizable<Job>> filter) {
                super(executor);
                this.filter = filter;
        }

        @Override
        public Collection<? extends Prioritizable<Job>> asCollection() {
                return Collections2.filter(super.asCollection(),this.filter);
        }

        //faster
        protected Collection<? extends Prioritizable<Job>> nonOrdered(){
                return Collections2.filter(super.nonOrdered(),this.filter);

        }

        
}
