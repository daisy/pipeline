package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.daisy.pipeline.persistence.impl.Database;

public class DatabaseProvider {
	private static EntityManagerFactory entityManagerFactory=Persistence.createEntityManagerFactory("pipeline-pu-test");

	public static Database getDatabase(){
		return new Database(entityManagerFactory);
	}
	
	public static EntityManagerFactory getEMF() {
		return entityManagerFactory;
	}

}
