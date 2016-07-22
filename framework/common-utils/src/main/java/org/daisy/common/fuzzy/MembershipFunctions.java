package org.daisy.common.fuzzy;

import com.google.common.base.Function;

public class MembershipFunctions{

       public static Function<Double,Double> newTrapezoidFunction(double x1, double x2, double x3,double x4){
               return TrapezoidFunction.fromPoints(x1,x2,x3,x4);

       }

       public static Function<Double,Double> newEqualsFunction(final double value){
                return new Function<Double, Double>() {

                        @Override
                        public Double apply(Double otherValue) {
                                return (otherValue==value)? 1.0:0.0;
                        }
               };
       }


       //Protected classes 
       
       /**       
        * A fuzzy set which basically have this shape
        *    --------     1
        *   /        \
        *  /          \
        * /            \  0
        */
       static class TrapezoidFunction implements Function<Double,Double>{
                private double []points;
                
                private TrapezoidFunction(double []points){
                        this.points=points;
                }

                /**
                 * Creates a new trapezoidal memebership function from four points defining the start, end of the first slope, start of the last slope and the ending point.
                 *  x1<=x2<=x3<=x4
                 */
                public static TrapezoidFunction fromPoints(double x1, double x2, double x3,double x4){
                        if (!(x1<=x2 && x2<=x3 && x3<=x4)){
                                throw new IllegalArgumentException(String.format("Fuzzy set boundaries are not x1<=x2<=x3<=x4 (%s, %s, %s, %s)", 
                                                        x1,x2,x3,x4));
                        }
                        return new TrapezoidFunction(new double[]{x1,x2,x3,x4});
                }

                @Override
                /**
                 * Computes the memebership value
                 */
                public Double apply(Double x) {
                        double res=-1;
                        int seg=getSegment(x);
                        switch(seg){
                                case -1: 
                                        res=0;
                                        break;
                                case 0: //(x-x_0)/(x_1-x_0)
                                        res=(x-this.points[seg])/(this.points[seg+1]-this.points[seg]);
                                        if (Double.isNaN(res))
                                                res=1;
                                        break;
                                case 1:
                                        res=1;
                                        break;
                                case 2://(x1-x)/(x_1-x_0)
                                        res=(this.points[seg+1]-x)/(this.points[seg+1]-this.points[seg]);
                                        if (Double.isNaN(res))
                                                res=1;
                                        break;
                        }
                        return new Double(res);
                }
              
                /**
                 * Returns the segment index where this vlaue falls into. -1 is out of the bounds
                 * 
                 */
                int getSegment(double x){
                        if (x<0.0 ){
                                x=0;
                        }else if (x>1.0){
                                x=1;
                        }
                                
                        for (int  i=0;i<this.points.length-1;i++){
                                if(inSegment(i,x)){
                                        return i;
                                }
                        }
                        return -1;
                                
                                
                }
                /**
                 *
                 * Check the segment where x has to be projected into
                 */
                boolean inSegment(int segment,double x){
                        return (x>=this.points[segment] && x<=this.points[segment+1]);
                }

       }
}
