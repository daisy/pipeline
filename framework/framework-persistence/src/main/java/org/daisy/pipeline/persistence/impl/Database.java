package org.daisy.pipeline.persistence.impl;

import java.util.List;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

public class Database {

	private final EntityManagerFactory emf;
	
	public Database(EntityManagerFactory emf) {
		if (emf ==null)
			throw new IllegalArgumentException("entity manager factory was null");
		this.emf = emf;
	}

	public void addObject(Object obj) {
		EntityManager em=this.getEntityManager();
		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();
		em.close();
	}

	public boolean deleteObject(Object obj) {
		if (obj != null) {
			EntityManager em=this.getEntityManager();
			em.getTransaction().begin();
			em.remove(em.merge(obj));
			em.getTransaction().commit();
			em.close();
			return true;
		}
		return false;
	}

	public void updateObject(Object obj) {
		EntityManager em=this.getEntityManager();
		em.getTransaction().begin();
		em.merge(obj);
		em.getTransaction().commit();
		em.close();
	}

	public <T> List<T> runQuery(String queryString, Class<T> clazz) {
		EntityManager em= this.getEntityManager();
		TypedQuery<T> q = em.createQuery(queryString, clazz);
		List<T> res= q.getResultList();
		em.close();
		return res;
	}

	public <T> T getFirst(String queryString, Class<T> clazz) {
		EntityManager em= this.getEntityManager();
		TypedQuery<T> q = em.createQuery(queryString, clazz);
		T res=q.getSingleResult();
		em.close();
		return res;
	}

	public EntityManager getEntityManager(){
		return emf.createEntityManager();
	}

	public Cache getCache(){
		return emf.getCache();
	}

}
