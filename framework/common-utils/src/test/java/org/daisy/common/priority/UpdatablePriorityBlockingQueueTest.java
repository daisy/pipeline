package org.daisy.common.priority;

import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;

import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.UpdatablePriorityBlockingQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class UpdatablePriorityBlockingQueueTest{
       @Mock private PrioritizableRunnable  task1; 
       @Mock private PrioritizableRunnable  task2; 
       @Mock private PrioritizableRunnable  task3; 

       @Before
       public void setUp(){
       }

       @Test
       public void checkOrder(){
               when(task1.getPriority()).thenReturn(-5.0);
               when(task2.getPriority()).thenReturn(-10.0);
               when(task3.getPriority()).thenReturn(-1.0);
              
               UpdatablePriorityBlockingQueue queue = 
                       new UpdatablePriorityBlockingQueue(); 
               queue.offer(task1);
               queue.offer(task2);
               queue.offer(task3);

               Assert.assertEquals("First is task 2",task2,queue.poll());
               Assert.assertEquals("Second is task 1",task1,queue.poll());
               Assert.assertEquals("Third is task 3",task3,queue.poll());
       }

       @Test
       public void swap(){
               when(task1.getPriority()).thenReturn(-5.0);
               when(task2.getPriority()).thenReturn(-10.0);
               when(task3.getPriority()).thenReturn(-1.0);

               UpdatablePriorityBlockingQueue queue = 
                       new UpdatablePriorityBlockingQueue(); 
               queue.offer(task1);
               queue.offer(task2);
               queue.offer(task3);
               queue.swap(task1,task2);
               Assert.assertArrayEquals("Order is 1,2,3",new PrioritizableRunnable[]{task1,task2,task3},queue.asOrderedCollection().toArray());
               queue.swap(task2,task3);
               Assert.assertArrayEquals("Order is 1,3,2",new PrioritizableRunnable[]{task1,task3,task2},queue.asOrderedCollection().toArray());
               queue.swap(task1,task3);
               Assert.assertArrayEquals("Order is 3,1,2",new PrioritizableRunnable[]{task3,task1,task2},queue.asOrderedCollection().toArray());
               queue.swap(task2,task3);
               Assert.assertArrayEquals("Order is 3,1,2",new PrioritizableRunnable[]{task2,task1,task3},queue.asOrderedCollection().toArray());
       }

       @Test
       public void swapAndBack(){
               when(task1.getPriority()).thenReturn(-5.0);
               when(task2.getPriority()).thenReturn(-10.0);

               UpdatablePriorityBlockingQueue queue = 
                       new UpdatablePriorityBlockingQueue(); 
               queue.offer(task1);
               queue.offer(task2);
               queue.swap(task1,task2);
               queue.swap(task1,task2);

               PrioritizableRunnable first=(PrioritizableRunnable)queue.poll();
               PrioritizableRunnable second=(PrioritizableRunnable)queue.poll();
               Assert.assertEquals("First is task 2",task2,first);
               Assert.assertEquals("Second is task 1",task1,second);
       }

       @Test
       public void swapNonExistent(){
               when(task1.getPriority()).thenReturn(-5.0);
               when(task2.getPriority()).thenReturn(-10.0);

               UpdatablePriorityBlockingQueue queue = 
                       new UpdatablePriorityBlockingQueue(); 
               queue.offer(task1);
               queue.offer(task2);
               queue.swap(task1,task3);

               Assert.assertEquals("First is task 2",task2,queue.poll());
               Assert.assertEquals("Second is task 1",task1,queue.poll());
           
               queue.offer(task1);
               queue.offer(task2);
               queue.swap(task3,task2);

               Assert.assertEquals("First is task 2",task2,queue.poll());
               Assert.assertEquals("Second is task 1",task1,queue.poll());

               Mockito.verify(task3, Mockito.times(0)).getPriority();
       }

       //@Test
       //public void swapNoNestingImpersonation(){
               //when(task1.getPriority()).thenReturn(-5.0);
               //when(task2.getPriority()).thenReturn(-10.0);
               //when(task3.getPriority()).thenReturn(-1.0);

               //UpdatablePriorityBlockingQueue queue = 
                       //new UpdatablePriorityBlockingQueue(); 
               //queue.offer(task1);
               //queue.offer(task2);
               //queue.offer(task3);
               //queue.swap(task1,task2);

               //ForwardingPrioritableRunnable first=((ForwardingPrioritableRunnable)queue.poll());
               //Assert.assertEquals("First is task 1",task1,first.getDelegate());
               //Assert.assertEquals("Second is task 2",task2,((ForwardingPrioritableRunnable)queue.poll()).getDelegate());
               //Assert.assertEquals("Third is task 3",task3,queue.poll());

               //queue.offer(first);
               //queue.offer(task2);
               //queue.offer(task3);
               //queue.swap(task2,first);

               //Assert.assertEquals("First is task 1",task1,((ForwardingPrioritableRunnable)queue.poll()).getDelegate());
               //Assert.assertEquals("Second is task 2",task2,((ForwardingPrioritableRunnable)queue.poll()).getDelegate());
               //Assert.assertEquals("Third is task 3",task3,queue.poll());
           
       //}

       @Test
       public void asCollection(){
               when(task1.getPriority()).thenReturn(-5.0);
               when(task2.getPriority()).thenReturn(-10.0);
               when(task3.getPriority()).thenReturn(-1.0);
              
               UpdatablePriorityBlockingQueue queue = 
                       new UpdatablePriorityBlockingQueue(); 
               queue.offer(task1);
               queue.offer(task2);
               queue.offer(task3);
               Collection<PrioritizableRunnable> col=queue.asOrderedCollection();

               Assert.assertEquals("First is task 2",task2,col.toArray()[0]);
               Assert.assertEquals("Second is task 1",task1,col.toArray()[1]);
               Assert.assertEquals("Third is task 3",task3,col.toArray()[2]);
       }
}

