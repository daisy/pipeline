package org.daisy.common.priority;

import com.google.common.base.Function;

public class ForwardingPrioritableRunnable<T> extends PrioritizableRunnable<T> {

        private PrioritizableRunnable<T> delegate;

        public ForwardingPrioritableRunnable(PrioritizableRunnable<T> delegate) {
                super(null,null);
                this.delegate = delegate;
        }

        @Override
        public T prioritySource() {
                return this.delegate.prioritySource();
        }

                @Override
        public void run() {
                this.delegate.run();
        }

        @Override
        public PriorityCalculator<T> getPriorityCalculator() {
                return this.delegate.getPriorityCalculator();
        }

        @Override
        public long getTimestamp() {
                return this.delegate.getTimestamp();
        }

        @Override
        public synchronized double getRelativeWaitingTime() {
                return this.delegate.getRelativeWaitingTime();
        }

        @Override
        public synchronized void markDirty(boolean dirty) {
                this.delegate.markDirty(dirty);
        }

        @Override
        public synchronized boolean isDirty() {
                return this.delegate.isDirty();
        }

        @Override
        public synchronized double getPriority() {
                return this.delegate.getPriority();
        }

        @Override
        public void setRelativeWaitingTime(Function<Long, Double> normalizer) {
                this.delegate.setRelativeWaitingTime(normalizer);
        }

        
        /**
         * @return the delegate
         */
        public PrioritizableRunnable<T> getDelegate() {
                return delegate;
        }


}
