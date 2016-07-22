package org.daisy.common.priority;

import java.util.Comparator;


/**
 * Comparator for PrioritizedRunnables based on their priorities
 */
public final class PrioritizableComparator implements
                Comparator<PrioritizableRunnable<?>> {

       
        @Override
        public int compare(PrioritizableRunnable<?> arg0, PrioritizableRunnable<?> arg1) {
                return Double.compare(arg0.getPriority(),arg1.getPriority());
        }
        
}
