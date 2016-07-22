package org.daisy.common.fuzzy;

import java.util.LinkedList;
import java.util.List;


/**
 * A fuzzy variable is just a group of fuzzy sets
 */
public final class FuzzyVariable {

        /**
         * The list of sets
         */
        final List<FuzzySet> sets=new LinkedList<FuzzySet>();
      
        /**
         * Adds a new fuzzy set to this variable
         * @param set
         * @return
         */
        final public FuzzyVariable add(FuzzySet set){
                this.sets.add(set);
                return this;
        }
        
        /**
         * Returns iterable of this variable sets
         *
         * @return
         */
        final public Iterable<FuzzySet> getSets(){
                return this.sets;
        }

 

                
}


