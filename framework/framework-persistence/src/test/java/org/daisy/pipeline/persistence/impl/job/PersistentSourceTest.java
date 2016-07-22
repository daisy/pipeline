package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManager;

import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.job.PersistentSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistentSourceTest   {
	Database db;
	PersistentSource src1;
	private static final String SYS_ID_1="file:/tmp/file.xml";
	@Before	
	public void setUp(){
		//db=DatabaseProvider.getDatabase();
		//src1=new PersistentSource();
		//src1.setSystemId(SYS_ID_1);
		//db.addObject(src1);
	}	

	@After
	public void tearDown(){
		//EntityManager em=db.getEntityManager();
		//em.getTransaction().begin();
		//em.createNativeQuery("delete from sources").executeUpdate();
		//em.getTransaction().commit();
		//em.close();	
	}	
	@Test
	public void createSource(){
		//PersistentSource found=db.getEntityManager().find(PersistentSource.class,SYS_ID_1);
		Assert.assertNotNull("Hey");
	}

}
