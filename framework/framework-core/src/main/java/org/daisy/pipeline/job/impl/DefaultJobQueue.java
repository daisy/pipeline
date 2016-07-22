package org.daisy.pipeline.job.impl;


import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.daisy.common.priority.Prioritizable;
import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.PriorityThreadPoolExecutor;
import org.daisy.common.priority.UpdatablePriorityBlockingQueue;
import org.daisy.pipeline.job.JobQueue;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DefaultJobQueue implements JobQueue {

        PriorityThreadPoolExecutor<Job> executor;

        /**
         * @param queue
         */
        public DefaultJobQueue(PriorityThreadPoolExecutor<Job> executor) {
                this.executor= executor;
        }

        @Override
        public void moveUp(JobId id) {
                Optional<? extends Prioritizable<Job>> ided=this.find(id);
                if(!ided.isPresent()){
                        return;
                }
                Optional<? extends Prioritizable<Job>> prev=this.findPrevious(ided.get());
                if(!prev.isPresent()){
                        return;
                }
                this.getQueue()
                        .swap((PrioritizableRunnable<Job>)ided.get(),(PrioritizableRunnable<Job>)prev.get());

        }

        @Override
        public void moveDown(JobId id) {
                Optional<? extends Prioritizable<Job>> ided=this.find(id);
                if(!ided.isPresent()){
                        return;
                }
                Optional<? extends Prioritizable<Job>> next=this.findNext(ided.get());
                if(!next.isPresent()){
                        return;
                }
                this.getQueue()
                        .swap((PrioritizableRunnable<Job>)ided.get(),(PrioritizableRunnable<Job>)next.get());
        }

        @Override
        public void cancel(JobId id) {
                Optional<? extends Prioritizable<Job>> ided=this.find(id);
                if(!ided.isPresent()){
                        return;
                }
                this.executor.remove((PrioritizableRunnable<Job>)ided.get());
        }

        @Override
        public Collection<? extends Prioritizable<Job>> asCollection(){
                return (Collection<? extends Prioritizable<Job>>)
                        this.getQueue().asOrderedCollection();

        }

        //faster
        protected Collection<? extends Prioritizable<Job>> nonOrdered(){
               return this.getQueue().asCollection();

        }

        Optional<? extends Prioritizable<Job>> find(final JobId id){
                return Iterables.tryFind(this.nonOrdered(),
                                new Predicate<Prioritizable<Job>>() {
                                        @Override
                                        public boolean apply(Prioritizable<Job> pJob) {
                                                return pJob.prioritySource().getId().equals(id);
                                        }
                });
        }

        Optional<? extends Prioritizable<Job>> findNext(Prioritizable<Job> job){
                return this.findNext(job.prioritySource().getId(),this.asCollection());
        }

        private Optional<? extends Prioritizable<Job>> findNext(final  JobId id, Collection<? extends Prioritizable<Job>> jobs){
                return Iterables.tryFind(jobs,
                                new Predicate<Prioritizable<Job>>() {
                                        boolean isNext=false; 
                                        @Override
                                        public boolean apply(Prioritizable<Job> pJob) {
                                                if(isNext){
                                                        return true;
                                                }
                                                isNext=pJob.prioritySource().getId().equals(id);
                                                return false;

                                        }
                });
        }

        Optional<? extends Prioritizable<Job>> findPrevious(Prioritizable<Job> job){
                List<Prioritizable<Job>> reverse=Lists.newLinkedList(this.asCollection());
                Collections.reverse(reverse);
                return this.findNext(job.prioritySource().getId(),reverse);
        }

        /**
         * @return the executor
         */
        protected UpdatablePriorityBlockingQueue<Job> getQueue() {
                return executor.getUpdatableQueue();
        }

        
}
