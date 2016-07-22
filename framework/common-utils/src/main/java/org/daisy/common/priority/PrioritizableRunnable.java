package org.daisy.common.priority;

import com.google.common.base.Function;

/**
 * A runnable that holds an associated priority. This priority may be computed somehow but it must allow 
 * to force it.
 * <br/>
 * This object also has a timestamp and relative waiting time.
 *
 */
public class PrioritizableRunnable<T> implements Prioritizable<T>,Runnable{

        /**
         * Instant when this runnable was created.
         */
        final long timestamp;

        /**
         * Indicates if the relative timestamp has been 
         * recently updated.
         */
        protected boolean dirty=true;
        /**
         * realtive waiting time within a given context.
         */
        protected double relativeWaitingTime;

        private Runnable runnable;

        private PriorityCalculator<T> priorityCalculator;

        /**
         * Creats the object and timestamps it.
         * {@inheritDoc}
         * @see Object#PrioritizedRunnable()
         */
        public PrioritizableRunnable(Runnable runnable,PriorityCalculator<T> calculator){
                this.timestamp=System.currentTimeMillis();
                this.runnable=runnable;
                this.priorityCalculator=calculator;
        }

        @Override
        public void run() {
                this.runnable.run();

        }


        public PriorityCalculator<T> getPriorityCalculator(){
                return this.priorityCalculator;
        }

        /** 
         * Forces the priority of this task to be a certain value
         */
//        public abstract double forcePriority(double priority);

        /**
         * @return the timestamp
         */
        @Override
        public long getTimestamp() {
                return timestamp;
        }

        /**
         * Gets the relative waiting time
         * @return the relativeWaitingTime
         */
        public double getRelativeWaitingTime() {
                return this.relativeWaitingTime;
        }

        /**
         *  marks this runnable as dirty so the priority should be recalculated 
         *  if necessary.
         */
        public void markDirty(boolean dirty) {
                this.dirty = dirty;
        }

        public boolean isDirty() {
                return this.dirty;
        }

        /**
         * Uses the normaliser to set the relative waiting time of this 
         * runnable and marks it as dirty
         */
        public void setRelativeWaitingTime(Function<Long,Double> normalizer){
                this.relativeWaitingTime=normalizer.apply(this.getTimestamp());
                this.markDirty(true);
        }

        @Override
        public double getPriority() {
                return this.priorityCalculator.getPriority(this);
        }

        @Override
        public T prioritySource() {
                return this.priorityCalculator.prioritySource();
        }

}
