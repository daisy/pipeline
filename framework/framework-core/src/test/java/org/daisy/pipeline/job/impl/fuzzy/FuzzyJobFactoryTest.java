package org.daisy.pipeline.job.impl.fuzzy;

import org.daisy.common.fuzzy.InferenceEngine;
import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.pipeline.job.impl.fuzzy.FuzzyJobFactory;
import org.daisy.pipeline.job.impl.fuzzy.FuzzyPriorityCalculator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Supplier;

@RunWith(MockitoJUnitRunner.class)

public class FuzzyJobFactoryTest   {

        InferenceEngine engine=FuzzyJobFactory.ENGINE;
        FuzzyPriorityCalculator<Object>[][] calculators;
        double scores[][][];
        double []waitingTimes;


        @SuppressWarnings("unchecked")
        @Before
        public void setUp(){

                calculators=new FuzzyPriorityCalculator[3][];
                //generate all the possible options
                for (int i=0; i<3;i++){
                        calculators[i]=new FuzzyPriorityCalculator[3];
                        for (int j=0; j<3;j++){
                                final double[] prios=new double[]{i/2.0,j/2.0};
                                calculators[i][j] = new FuzzyPriorityCalculator<Object>(engine,
                                                new Supplier<double[]>() {

                                                        @Override
                                                        public double[] get() {
                                                                return prios;
                                                        }
                                                },null);
                        }
                }

                waitingTimes=new double[]{0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
                scores = this.getScores(waitingTimes);

        }

        @Test
        public void engineNoStarvation(){
                //very old task, low,low
                double oldLL=scores[0][0][9];
                //very new task, high,high
                double newHH=scores[2][2][0];
                //check that oldLL is bigger than newHH, this assures no starvation
                Assert.assertTrue("old task with low priority more score than new task with hight priority",
                                oldLL>newHH);

        }

        @Test
        public void engineTimeSanity(){

                        for (int j=0; j<3;j++){
                                for (int k=0; k<3;k++){
                                        for (int i=1; i<waitingTimes.length;i++){
                                                Assert.assertTrue("The older, the more prio",scores[j][k][i-1]<=scores[j][k][i]);
                                        }
                                }
                        }

        }

        @Test
        public void enginePrioritySanity(){
                        for (int i=1; i<waitingTimes.length;i++){
                                for (int j=0; j<3;j++){
                                        for (int k=j+1; k<3;k++){
                                                Assert.assertTrue("The higher prio (job), the more prio",scores[j][k-1][i]<=scores[j][k][i]);
                                                Assert.assertTrue("The higher prio (client), the more prio",scores[k-1][j][i]<=scores[k-1][j][i]);
                                        }
                                }
                        }

        }

        @SuppressWarnings({"unchecked"})
        private double[][][] getScores(double times[]){
                double[][][] scores= new double[3][3][times.length];
                PrioritizableRunnable<Object> runnable = Mockito.mock(PrioritizableRunnable.class);
                for (int i=0; i<3;i++){
                        for (int j=0; j<3;j++){
                                for (int k=0;k<times.length;k++){
                                        Mockito.doReturn(times[k]).when(runnable).getRelativeWaitingTime();
                                        Mockito.doReturn(true).when(runnable).isDirty();
                                        //as prios are negative
                                        scores[i][j][k]=-1*calculators[i][j].getPriority(runnable);
                                }
                        }
                }
                return scores;
                        
        }
}
