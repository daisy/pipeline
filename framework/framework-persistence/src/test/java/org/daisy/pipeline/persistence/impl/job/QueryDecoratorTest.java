package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.JobBuilder;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.PersistentJob;
import org.daisy.pipeline.persistence.impl.job.PersistentJobContext;
import org.daisy.pipeline.persistence.impl.job.QueryDecorator;
import org.daisy.pipeline.persistence.impl.job.PersistentJob.PersistentJobBuilder;
import org.daisy.pipeline.persistence.impl.job.QueryDecorator.QueryHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class QueryDecoratorTest {
        static class QueryDecoratorImpl extends QueryDecorator<PersistentJob> {
                public QueryDecoratorImpl(EntityManager em) {
                        super(em);
                }


                @Override
                Predicate getPredicate(QueryDecorator<PersistentJob>.QueryHolder holder) {
                        return holder.cb.conjunction();
                }
        }
        @Mock
        CriteriaBuilder cb;

        QueryDecorator<PersistentJob> dec1;
        QueryDecorator<PersistentJob> dec2;
        @Mock Root<PersistentJob> root;
        @Mock CriteriaQuery<PersistentJob> cq;
        @Mock Predicate pred;
        Job job; 
        Database db;
        @Before
        public void setUp(){
		db=DatabaseProvider.getDatabase();
                dec1=Mockito.spy(new QueryDecoratorImpl(db.getEntityManager()));
                dec2=Mockito.spy(new QueryDecoratorImpl(db.getEntityManager()));

		System.setProperty("org.daisy.pipeline.iobase",System.getProperty("java.io.tmpdir"));
		PersistentJobContext.setScriptRegistry(new Mocks.DummyScriptService(Mocks.buildScript()));
		JobBuilder builder= new PersistentJobBuilder(db).withContext(Mocks.buildContext());
		job =(PersistentJob) builder.build();//new PersistentJob(Job.newJob(Mocks.buildContext()),db);

        }
	@After
	public void tearDown(){
		db.deleteObject(job);
		db.deleteObject(job.getContext().getClient());
        }


        @Test
        public void decorate(){
                dec1.setNext(dec2);
                QueryDecorator<PersistentJob>.QueryHolder holder=dec1.holder(cb,root,cq);
                dec1.decorateWhere(holder,pred);
                Mockito.verify(dec1,Mockito.times(1)).getPredicate(holder);
                Mockito.verify(dec2,Mockito.times(1)).getPredicate(holder);
                
        }

        @Test
        @SuppressWarnings({"unchecked"})
        public void getSelect(){
                QueryDecorator<PersistentJob> dec=Mockito.spy(new QueryDecoratorImpl(db.getEntityManager()));
                TypedQuery<PersistentJob> query=dec.getQuery(PersistentJob.class); 
                Mockito.verify(dec,Mockito.times(1)).getPredicate(Mockito.any(QueryHolder.class));
                Assert.assertEquals("Finds the job",1,query.getResultList().size());
        }
        
}
