
package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.TypedQuery;

import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.persistence.impl.Database;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IdFilterTest   {
        AbstractJob job;
        AbstractJob job2;
        Database db;
        @Before
        public void setUp(){
		db=DatabaseProvider.getDatabase();

		job = new PersistentJob(db, Mocks.buildJob(), null);
		job2 = new PersistentJob(db, Mocks.buildJob(), null);

        }
	@After
	public void tearDown(){
		db.deleteObject(job);
		db.deleteObject(job.getContext().getClient());
		db.deleteObject(job2);
		db.deleteObject(job2.getContext().getClient());
        }


        @Test
        public void getSelect(){
                QueryDecorator<PersistentJob> dec=new IdFilter(db.getEntityManager(),job2.getId());
                TypedQuery<PersistentJob> q=dec.getQuery(PersistentJob.class); 
                AbstractJob fromDb = q.getSingleResult();
                Assert.assertEquals("Finds the appropriate job",fromDb.getId(),job2.getId());
        }

        @Test
        public void getSelectIsUnique(){
                QueryDecorator<PersistentJob> dec=new IdFilter(db.getEntityManager(),job2.getId());
                TypedQuery<PersistentJob> q=dec.getQuery(PersistentJob.class); 
                Assert.assertEquals("Only one result",1,q.getResultList().size());
        }
        
}
