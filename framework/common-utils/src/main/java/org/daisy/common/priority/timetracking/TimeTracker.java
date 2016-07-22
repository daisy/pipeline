package org.daisy.common.priority.timetracking;

import java.util.Collection;

import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.UpdatablePriorityBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * This class maintains a buffer of the waiting time of last N tasks exectued. Once the buffer is full
 * the {@link UpdatablePriorityBlockingQueue} associated is updated with the new generated {@link TimeStats}.
 * @version
 *
 */
public class TimeTracker<T>{
        /**
         *Buffer possition
         */
        private int counter=0;
        /**
         *Size of the buffer
         */
        final private int frequency;
        /**
         *Execution queue to be updated peridically
         */
        final private UpdatablePriorityBlockingQueue<T> queue;
        /**
         *Factory of normalising functions
         */
        final private TimeFunctionFactory functionFactory ;

        private static final Logger logger = LoggerFactory.getLogger(TimeTracker.class);

        


        /**
         * Creates a new TimeTracker with a buffer of the provided size, that updates the given queue using the functions 
         * provided by the factory.
         * @param size
         * @param queue
         */
        public TimeTracker(int frequency, UpdatablePriorityBlockingQueue<T> queue, TimeFunctionFactory functionFactory) {
                this.frequency= frequency;
                this.queue = queue;
                this.functionFactory=functionFactory;
        }

        /**
         * Stores the waiting time of the given runnable in the buffer
         * if the buffer is full the queue is updated.
         * <br/>
         */
        public synchronized void executing(){
                //update counter 
                this.counter++;
                if( this.counter == this.frequency){
                        //get the waiting times
                        Collection<PrioritizableRunnable<T>> waiting=this.queue.asCollection();
                        long times[] = new long[waiting.size()]; 
                        int i=0;
                        for( PrioritizableRunnable<T> r:waiting){
                                times[i++]=r.getTimestamp(); 
                        }
                        this.update(times);                                 
                        this.counter=0;
                }
        }

        /**
         * Updates the queue 
         */
        void update(long []times){
                logger.debug("Updating queue");

                //new stats
                TimeStats stats= new TimeStats(System.nanoTime(),times);
                //get a new updater function
                final Function<Long,Double> timeUpdater=TimeTracker.this.functionFactory.getFunction(stats);
                //Let the queue do the work
                this.queue.update(new Function<PrioritizableRunnable<T>, Void>() {
                        @Override
                        public Void apply(PrioritizableRunnable<T> runnable) {
                                runnable.setRelativeWaitingTime(timeUpdater);
                                //ugly as hell but you can't intantiate void, go figure.
                                return null;
                        }
                });
        }
}
