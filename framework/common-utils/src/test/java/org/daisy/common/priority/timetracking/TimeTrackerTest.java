package org.daisy.common.priority.timetracking;

import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.UpdatablePriorityBlockingQueue;
import org.daisy.common.priority.timetracking.TimeFunctions;
import org.daisy.common.priority.timetracking.TimeTracker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TimeTrackerTest {
        @Mock PrioritizableRunnable r1;
        @Mock PrioritizableRunnable r2;
        @Mock PrioritizableRunnable r3;
       
        @Test
        public void maximum(){
                TimeTracker tracker = Mockito.spy(new TimeTracker(3,new UpdatablePriorityBlockingQueue(),TimeFunctions.newLinearTimeFunctionFactory())); 
                Mockito.doNothing().when(tracker).update(Mockito.any(long[].class));
                tracker.executing();
                tracker.executing();
                tracker.executing();
                Mockito.verify(tracker,Mockito.times(1)).update(Mockito.any(long[].class));
        }

        @Test
        public void noUpdate(){
                TimeTracker tracker = Mockito.spy(new TimeTracker(3,new UpdatablePriorityBlockingQueue(),TimeFunctions.newLinearTimeFunctionFactory())); 
                //Mockito.doNothing().when(tracker).update(Mockito.any(long[].class));
                tracker.executing();
                tracker.executing();
                Mockito.verify(tracker,Mockito.times(0)).update(Mockito.any(long[].class));
        }
}
