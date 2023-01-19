package org.daisy.common.priority;

public interface Prioritizable<T>  {
       /**
        * Returns the priority.
        */
        public double getPriority();

       /**
        * Returns the timestamp.
        */
        public long getTimestamp();

        public double getRelativeWaitingTime();
        
        public T prioritySource();
}
