package org.daisy.common.priority.timetracking;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.daisy.common.priority.UpdatablePriorityBlockingQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.base.Function;

//@RunWith(MockitoJUnitRunner.class)
//@SuppressWarnings({"unchecked"})
//public class TimeTrackingTest {

        //private UpdatablePriorityBlockingQueue queue;
        //private TimeFunctionFactory functionFactory = new TimeFunctionFactory() {
                //@Override
                //public Function<Long, Double> getFunction(TimeStats stats) {
                        //return new Function<Long, Double>() {

                                //@Override
                                //public Double apply(Long arg0) {
                                        //return arg0*2.0;
                                //}};
                //}};
        //@Mock private FuzzyRunnable r1;
        //@Mock private FuzzyRunnable r2;
        //@Mock private FuzzyRunnable r3;

       
        //@Before
        //public void setUp(){
                //queue=spy(new UpdatablePriorityBlockingQueue());
                //queue.offer(r1);
                //queue.offer(r2);
                //queue.offer(r3);

        //}
        //@Test 
        //public void trackerNotFull(){
                //TimeTracker tracker=new TimeTracker(3,queue, functionFactory);
                //tracker.executing();
                //tracker.executing();
                //verify(queue,times(0)).update(any(Function.class));
        //}

        //@Test 
        //public void trackerUpdating(){
                //TimeTracker tracker=new TimeTracker(3,queue, functionFactory);
                //tracker.executing();
                //tracker.executing();
                //tracker.executing();
                //verify(queue,times(1)).update(any(Function.class));
        //}

        //@Test 
        //public void trackerAfterUpdatingNotFull(){
                ////given a tracker of size 3 with two already executed tasks
                //TimeTracker tracker=new TimeTracker(3,queue, functionFactory);
                //tracker.executing();
                //tracker.executing();
                //tracker.executing();
                //verify(queue,times(1)).update(any(Function.class));
                //tracker.executing();
                //tracker.executing();
                //verify(queue,times(1)).update(any(Function.class));
                ////
        //}

        //@Test 
        //public void trackerAfterUpdatingFull(){
                ////given a tracker of size 3 with two already executed tasks
                //TimeTracker tracker=new TimeTracker(3,queue, functionFactory);
                //tracker.executing();
                //tracker.executing();
                //tracker.executing();
                //verify(queue,times(1)).update(any(Function.class));
                //tracker.executing();
                //tracker.executing();
                //tracker.executing();
                //verify(queue,times(2)).update(any(Function.class));
                ////
        //}

        //@Test 
        //public void updatedTimes(){
                //when(r1.getTimestamp()).thenReturn(1L);
                //doCallRealMethod().when(r1).setRelativeWaitingTime(any(Function.class));
                //doCallRealMethod().when(r1).getRelativeWaitingTime();
                //when(r2.getTimestamp()).thenReturn(2L);
                //doCallRealMethod().when(r2).setRelativeWaitingTime(any(Function.class));
                //doCallRealMethod().when(r2).getRelativeWaitingTime();
                //when(r3.getTimestamp()).thenReturn(3L);
                //doCallRealMethod().when(r3).setRelativeWaitingTime(any(Function.class));
                //doCallRealMethod().when(r3).getRelativeWaitingTime();

                ////given a tracker 
                //TimeTracker tracker=new TimeTracker(3,queue, functionFactory);

                ////when executing to the maximum size
                //tracker.executing();
                //tracker.executing();
                //tracker.executing();

                ////the relative times have to be updated 
                //verify(r1,times(1)).setRelativeWaitingTime(any(Function.class));
                //Assert.assertEquals("r1 relative time ",2.0,r1.getRelativeWaitingTime(),0.0);

                //verify(r2,times(1)).setRelativeWaitingTime(any(Function.class));
                //Assert.assertEquals("r2 relative time ",4.0,r2.getRelativeWaitingTime(),0.0);

                //verify(r3,times(1)).setRelativeWaitingTime(any(Function.class));
                //Assert.assertEquals("r3 relative time ",6.0,r3.getRelativeWaitingTime(),0.0);
                ////
        //}
        
//}
