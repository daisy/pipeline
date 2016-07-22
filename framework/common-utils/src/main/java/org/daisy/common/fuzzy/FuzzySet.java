package org.daisy.common.fuzzy;

import com.google.common.base.Function;

/**
* A fuzzy set is defined by a name which describes the intensity (high, low , very cold, hot, etc.), a weight and a memebership function, the memebership function should define behaviuors in the range (0,1) and return values from (0,1). 
*
*/
public class FuzzySet {
        /**
         * Set's name
         */
        final private String name;
        /**
         * The weight to be applied  
         */
        final private double weight;
        /**
         * Function that measures the membership of a given x
         */
        final private Function<Double, Double> membership;

        /**
         * @param name
         * @param weight
         * @param membership
         */
        public FuzzySet(String name, double weight,
                        Function<Double, Double> membership) {
                this.name = name;
                this.weight = weight;
                this.membership = membership;
        }

        /**
         * @return the name
         */
        final public String getName() {
                return name;
        }

        /**
         * @return the weight
         */
        final public double getWeight() {
                return weight;
        }

        /**
         * @return the membership function
         */
        final public Function<Double, Double> getMembership() {
                return membership;
        }

}
