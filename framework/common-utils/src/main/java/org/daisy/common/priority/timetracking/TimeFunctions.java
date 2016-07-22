package org.daisy.common.priority.timetracking;
/**
 * Utility class to conglomerate the different normalising functions.
 */
public final class TimeFunctions {


        /**
         * Returns a new factory that produces {@link LinearTimeNormalizer} objects.
         * @return
         */
        public static TimeFunctionFactory newLinearTimeFunctionFactory(){
                return new LinearTimeNormalizer();
        }
}
