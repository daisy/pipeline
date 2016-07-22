package org.daisy.common.fuzzy;

import org.daisy.common.fuzzy.FuzzySet;
import org.daisy.common.fuzzy.FuzzyVariable;
import org.daisy.common.fuzzy.InferenceEngine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.primitives.Doubles;

public class InferenceEngineTest{

        FuzzyVariable f1;
        FuzzyVariable f2;
        FuzzyVariable f3;

        @Before
        public void setUp(){
                //Fuzzy Variable 1 
                f1= new FuzzyVariable();
                f1.add(new FuzzySet("high",100,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x>0.6? 1:0.0;
                                        }
                }));

                f1.add(new FuzzySet("medium",50,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x>=0.3&&x<=0.6? 1:0.0;
                                        }
                }));

                f1.add(new FuzzySet("low",10,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x<0.3? 1:0.0;
                                        }
                }));
                
                f2= new FuzzyVariable();
                f2.add(new FuzzySet("high",80,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x>0.3? 1:0.0;
                                        }
                }));

                f2.add(new FuzzySet("medium",60,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x>=0.1&&x<=0.3? 1:0.0;
                                        }
                }));

                f2.add(new FuzzySet("low",5,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x<0.1? 1:0.0;
                                        }
                }));
                //not really a fuzzy variable but just for testing, the membership is never 1
                f3= new FuzzyVariable();
                f3.add(new FuzzySet("high",100,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x>0.6? 0.9:0.0;
                                        }
                }));

                f3.add(new FuzzySet("medium",50,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x>=0.3&&x<=0.6? 0.5:0.0;
                                        }
                }));
                f3.add(new FuzzySet("low",10,new Function<Double,Double>(){
                                        @Override
                                        public Double apply(Double x) {
                                                return x<0.3? 0.1:0.0;
                                        }
                }));
        }

        @Test (expected=IllegalArgumentException.class)
        public void crispCountsError(){
                InferenceEngine eng = new InferenceEngine();
                eng.add(f1).add(f2);
                eng.getScore(new double[]{1,2,3});
        }
        @Test
        public void scoreFromVariable(){
                InferenceEngine eng = new InferenceEngine();
                eng.add(f3);

                double res=0;
                res= eng.getScore(0.1);
                //numerator low -> 10*0.1 + 50*0.0 +100*0.0 =1.0
                //denominator low 0.1 
                //res = 0.1
                Assert.assertEquals("only low ", 10.0, res,0.0);
                
                res= eng.getScore(0.5);
                //numerator medium -> 10*0.0 + 50*0.5 +100*0.0 =25
                //denominator medium 0.5 
                //res = 50 
                Assert.assertEquals("only med", 50, res,0.0);
                res= eng.getScore(1);
                //numerator high-> 10*0.0 + 50*0.0 +100*0.9 =90
                //denominator medium 0.9 
                //res = 100 
                Assert.assertEquals("only med", 100, res,0.0);

        }
        @Test 
        public void simpleScore(){
                InferenceEngine eng = new InferenceEngine();
                eng.add(f1).add(f2);
                //example 1 x_1=0.5 x_2=0.5
                //rules fired f1_med f2_high
                //=> sum(1.0*50 + 1.0*80)/sum(1,1) = 65
                double res= eng.getScore(0.5,0.5);
                Assert.assertEquals("test f1 mid f2 high is 65",65.0,res,0.00);

                //rules fired f1_low f2_medium
                //=> sum(1.0*10 + 1.0*60)/sum(1,1) = 35 
                res= eng.getScore(0.2,0.1);
                Assert.assertEquals("test f1 low f2 medium is 35",35.0,res,0.00);

                //rules fired f1_high f2_low
                //=> sum(1.0*100 + 1.0*5)/sum(1,1) = 52.5 
                res= eng.getScore(0.8,0.05);
                Assert.assertEquals("test f1 low f2 medium is 52.5",52.5,res,0.00);
        }
        
}
