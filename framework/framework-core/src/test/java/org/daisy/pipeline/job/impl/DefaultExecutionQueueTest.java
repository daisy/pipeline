package org.daisy.pipeline.job.impl;

import java.util.Collection;
import java.util.List;

import org.daisy.common.priority.Prioritizable;
import org.daisy.common.priority.PrioritizableRunnable;
import org.daisy.common.priority.PriorityThreadPoolExecutor;
import org.daisy.common.priority.UpdatablePriorityBlockingQueue;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.impl.DefaultJobQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class DefaultExecutionQueueTest {

        @Mock PriorityThreadPoolExecutor<Job> pool;
        @Mock UpdatablePriorityBlockingQueue<Job> queue;
        @Mock PrioritizableRunnable<Job> pj1; 
        @Mock PrioritizableRunnable<Job> pj2; 
        @Mock PrioritizableRunnable<Job> pj3; 
        @Mock PrioritizableRunnable<Job> pj4; 

        List<PrioritizableRunnable<Job>> runnables; 
        Collection<PrioritizableRunnable<Job>> pJobs; 
        List <JobId> ids; 
        DefaultJobQueue exQ;

        @Before
        public void setUp() {
                runnables=Lists.newLinkedList();
                runnables.add(pj1);
                runnables.add(pj2);
                runnables.add(pj3);
                runnables.add(pj4);

                pJobs=Lists.newLinkedList();
                pJobs.add(pj1);
                pJobs.add(pj2);
                pJobs.add(pj3);
                pJobs.add(pj4);

                ids=Lists.newLinkedList();
                for (PrioritizableRunnable<Job> pj : pJobs){
                        JobId id=JobIdFactory.newId();
                        ids.add(id);
                        Mockito.when(pj.prioritySource()).thenReturn(Mockito.mock(Job.class));
                        Mockito.when(pj.prioritySource().getId()).thenReturn(id);
                }

                exQ=new DefaultJobQueue(this.pool);
                Mockito.when(pool.getUpdatableQueue()).thenReturn(queue);
                Mockito.when(queue.asCollection()).thenReturn(runnables);
                Mockito.when(queue.asOrderedCollection()).thenReturn(runnables);
                
        }
        @Test
        public void find() {
                Optional<? extends Prioritizable<Job>> res=exQ.find(ids.get(2));
                Assert.assertTrue("We found the job",res.isPresent());
        }

        @Test
        public void findNext() {
                Prioritizable<Job> reference=exQ.find(ids.get(2)).get();

                Optional<? extends Prioritizable<Job>> next=exQ.findNext(reference);
                Assert.assertEquals("We got the correct next job",pj4.prioritySource().getId(),next.get().prioritySource().getId());

                next=exQ.findNext(next.get());
                Assert.assertFalse("There is no next after the last",next.isPresent());
        }

        @Test
        public void findPrevious() {
                Prioritizable<Job> reference=exQ.find(ids.get(1)).get();

                Optional<? extends Prioritizable<Job>> previous=exQ.findPrevious(reference);
                Assert.assertEquals("We got the correct previous job",pj1.prioritySource().getId(),previous.get().prioritySource().getId());

                previous=exQ.findPrevious(previous.get());
                Assert.assertFalse("There is no previous before the first",previous.isPresent());
        }

        @Test
        @SuppressWarnings({"unchecked"})
        public void moveUp() {
                //no effect after moving up a id that doesnt exsist
                exQ.moveUp(JobIdFactory.newId());
                Mockito.verify(queue, Mockito.times(0)).swap( Mockito.any(PrioritizableRunnable.class),Mockito.any(PrioritizableRunnable.class));
                //the first shouldent be moved
                exQ.moveUp(this.ids.get(0));
                Mockito.verify(queue, Mockito.times(0)).swap( Mockito.any(PrioritizableRunnable.class),Mockito.any(PrioritizableRunnable.class));
                //otherwise move it up
                exQ.moveUp(this.ids.get(1));
                Mockito.verify(queue, Mockito.times(1)).swap(this.runnables.get(1),this.runnables.get(0));
        }

        @Test
        @SuppressWarnings({"unchecked"})
        public void moveDown() {
                //no effect after moving up a id that doesnt exsist
                exQ.moveUp(JobIdFactory.newId());
                //the first shouldent be moved
                exQ.moveDown(this.ids.get(3));
                Mockito.verify(queue, Mockito.times(0)).swap( Mockito.any(PrioritizableRunnable.class),Mockito.any(PrioritizableRunnable.class));
                //otherwise move it up
                exQ.moveDown(this.ids.get(2));
                Mockito.verify(queue, Mockito.times(1)).swap(this.runnables.get(2),this.runnables.get(3));
        }

        @Test
        public void moveUpDown() {
                exQ.moveDown(this.ids.get(2));
                Mockito.verify(queue, Mockito.times(1)).swap(this.runnables.get(2),this.runnables.get(3));
                //otherwise move it up
                exQ.moveUp(this.ids.get(3));
                Mockito.verify(queue, Mockito.times(1)).swap(this.runnables.get(3),this.runnables.get(2));
        }

        @Test
        public void cancel() {
                exQ.moveUp(JobIdFactory.newId());
                Mockito.verify(pool, Mockito.times(0)).remove(this.runnables.get(3));

                exQ.cancel(this.ids.get(3));
                Mockito.verify(pool, Mockito.times(1)).remove(this.runnables.get(3));
        }
}
