package org.daisy.common.priority.timetracking;

import org.daisy.common.priority.timetracking.TimeStats;

import com.google.common.base.Function;

public interface TimeFunctionFactory  {

        public Function<Long,Double> getFunction(TimeStats stats);
        
}
