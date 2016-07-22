package org.daisy.common.priority;

import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.PriorityCalculator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;

@RunWith(MockitoJUnitRunner.class)
public class PrioritizableRunnableTest {
        @Mock Runnable runnable;
        @Mock
        PriorityCalculator calculator;
        private PrioritizableRunnable pr;

        @Before
        public void setUp() {
                this.pr = new PrioritizableRunnable(runnable,calculator);
                
        }
       
        @Test
        public void run(){
                pr.run();
                Mockito.verify(runnable,Mockito.times(1)).run();
        }

        @Test
        public void getPriority(){
                pr.getPriority();
                Mockito.verify(calculator,Mockito.times(1)).getPriority(pr);
        }

        @Test
        public void setRelativeWaitingTime(){
                Function<Long,Double> func= new Function<Long,Double> (){

                                        @Override
                                        public Double apply(Long arg0) {
                                                return 33.0;
                                        }

                };
                pr.setRelativeWaitingTime(func);
                Assert.assertEquals("The relative waiting time is set correctly",33.0,this.pr.getRelativeWaitingTime(),0.0);
                Assert.assertTrue("And the object is filthy",this.pr.isDirty());
        }
}
