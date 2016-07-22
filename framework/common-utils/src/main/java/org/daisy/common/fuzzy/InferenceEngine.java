package org.daisy.common.fuzzy;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.primitives.Doubles;



public class InferenceEngine {


        private List<FuzzyVariable> variables= new LinkedList<FuzzyVariable>();
        /**Adds a new FuzzyVariable to the system
         */
        public InferenceEngine add(FuzzyVariable set){
                this.variables.add(set);
                return this;
        }

        /**
         * We need a crisp value (x_i) per fuzzy variable (j) to calculate the memberships then the final score is calculated
         * as x_i:
         * (ΣiΣj weight_j*memebership_j(x_i)) / Σj weith_j where memebership_j(x_i)!=0  
         *
         */
        public double getScore(double ...crispValues){
                Collection<Double> crispCol=Doubles.asList(crispValues);
                //size(values) == size(sets)
                if (crispCol.size()!=this.variables.size()){
                        throw new IllegalArgumentException(String.format("The size of crisp values is different from the ammount of variables %s != %s",crispCol.size(),this.variables.size()));
                }

                double denominator=0.0;
                double numerator=0.0;
                //no zipping in java!
                Iterator<Double> crispIter=crispCol.iterator();
                Iterator<FuzzyVariable> variableIter=this.variables.iterator();
                //crisps and variables have the same size
                double memebership;
                while(crispIter.hasNext()){
                        FuzzyVariable var=variableIter.next();
                        double x=crispIter.next();
                        for (FuzzySet set:var.getSets()){
                                memebership=set.getMembership().apply(x);
                                numerator+=memebership*set.getWeight();
                                denominator+=memebership;
                        }
                }
                return numerator/denominator;

        }

}
