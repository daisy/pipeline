package org.daisy.pipeline.persistence;

import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

public class ForwardingEntityManagerFactory implements EntityManagerFactory {


	public static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "javax.persistence.jdbc.password";
	public static final String JAVAX_PERSISTENCE_JDBC_USER = "javax.persistence.jdbc.user";
	public static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
	public static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "javax.persistence.jdbc.driver";
	
	private EntityManagerFactory emf;

	protected void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

	@Override
	public void close() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		emf.close();

	}

	@Override
	public EntityManager createEntityManager() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.createEntityManager();
	}

	@Override
	public EntityManager createEntityManager(@SuppressWarnings("rawtypes") Map arg0) {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.createEntityManager(arg0);
	}

	@Override
	public Cache getCache() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.getCache();
	}

	@Override
	public CriteriaBuilder getCriteriaBuilder() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.getCriteriaBuilder();
	}

	@Override
	public Metamodel getMetamodel() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.getMetamodel();
	}

	@Override
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.getPersistenceUnitUtil();
	}

	@Override
	public Map<String, Object> getProperties() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.getProperties();
	}

	@Override
	public boolean isOpen() {
		if (emf == null) {
			throw new IllegalStateException(
					"Delegate EntityManagerFactory is null");
		}
		return emf.isOpen();
	}

}
