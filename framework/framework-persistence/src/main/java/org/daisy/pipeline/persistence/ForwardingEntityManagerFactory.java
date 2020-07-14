package org.daisy.pipeline.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

public abstract class ForwardingEntityManagerFactory implements EntityManagerFactory {

	public static final String JAVAX_PERSISTENCE_JDBC_PASSWORD = "javax.persistence.jdbc.password";
	public static final String JAVAX_PERSISTENCE_JDBC_USER = "javax.persistence.jdbc.user";
	public static final String JAVAX_PERSISTENCE_JDBC_URL = "javax.persistence.jdbc.url";
	public static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "javax.persistence.jdbc.driver";
	
	public static final String PU_UNIT = "pipeline-pu";
	
	private Map<String, Object> props = new HashMap<String, Object>();
	private EntityManagerFactory emf;

	public ForwardingEntityManagerFactory(Map<String, Object> props) {
		emf = Persistence.createEntityManagerFactory(PU_UNIT, props);
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
