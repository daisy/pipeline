package org.daisy.pipeline.job.impl.fuzzy;

import org.daisy.common.fuzzy.InferenceEngine;
import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.pipeline.job.impl.fuzzy.FuzzyPriorityCalculator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Supplier;

@RunWith(MockitoJUnitRunner.class)
public class FuzzyPriorityCalculatorTest   {

        @Mock private InferenceEngine engine;
        @Mock private PrioritizableRunnable runnable;
        Supplier<double[]> supplier;
        FuzzyPriorityCalculator calculator;
        

        @Before
        public void setUp() {
                final double vals[]= new double[]{1.0};
                supplier = new Supplier<double[]>() {

                        @Override
                        public double[] get() {
                                return vals;
                        }

                };
                calculator= new FuzzyPriorityCalculator(engine,supplier,null);
        }

        @Test
        public void calculatePriority(){
                Mockito.when(runnable.isDirty()).thenReturn(true);
                Mockito.when(runnable.getRelativeWaitingTime()).thenReturn(0.5);
                Mockito.when(engine.getScore( Mockito.anyFloat(), Mockito.anyFloat())).thenReturn(123.0);
                double priority=calculator.getPriority(runnable);
                Assert.assertEquals("The priority is right",-123.0,priority,0.0);
                //check that the runnable has been marked as no dirty
                Mockito.verify(runnable, Mockito.times(1)).markDirty(false);
                //check that the crisp values are well built
                Mockito.verify(engine, Mockito.times(1)).getScore(0.5,1.0);
        }

        @Test
        public void calculatePriorityCached(){
                Mockito.when(runnable.isDirty()).thenReturn(true).thenReturn(false);
                Mockito.when(runnable.getRelativeWaitingTime()).thenReturn(0.5);
                Mockito.when(engine.getScore( Mockito.anyFloat(), Mockito.anyFloat())).thenReturn(123.0).thenReturn(0.0);
                //first
                calculator.getPriority(runnable);
                //cached
                double priority=calculator.getPriority(runnable);
                Assert.assertEquals("The cached priority is right",-123.0,priority,0.0);
                //check that the engine was called just once
                Mockito.verify(engine, Mockito.times(1)).getScore(0.5,1.0);
        }

}
