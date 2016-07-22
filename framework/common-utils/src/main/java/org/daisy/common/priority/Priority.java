package org.daisy.common.priority;

/**
 * Possible priorities associated to a task
 */
public enum Priority{
                LOW,
                MEDIUM,
                HIGH;
                //for efficiency 
                private static final int size = Priority.values().length;
                /*
                 * Normalized numerical value for this piority
                 */
                public double asDouble(){
                        return this.ordinal()/(double)(size-1);
                }
}
