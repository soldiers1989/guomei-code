package com.sunline.ccs.facility;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.ejb.HibernatePersistence;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;

/**
 * 扩展hibernatePersistence ，主要用于 post event 的注入
* @author fanghj
 *
 */
public class HibernatePersistenceExtendedProvider extends HibernatePersistence {

	private PostInsertEventListener[] postInsertEventListeners;
	private PostUpdateEventListener[] postUpdateEventListeners;
	private PostDeleteEventListener[] postDeleteEventListeners;

	@SuppressWarnings("rawtypes")
	@Override
	public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
		Ejb3Configuration cfg = new Ejb3Configuration();
		setupConfiguration(cfg);
		Ejb3Configuration configured = cfg.configure(persistenceUnitName, properties);
		return configured != null ? configured.buildEntityManagerFactory() : null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
		Ejb3Configuration cfg = new Ejb3Configuration();
		setupConfiguration(cfg);
		Ejb3Configuration configured = cfg.configure(info, properties);
		return configured != null ? configured.buildEntityManagerFactory() : null;
	}

	private void setupConfiguration(Ejb3Configuration cfg) {
		cfg.getEventListeners().setPostInsertEventListeners(postInsertEventListeners);
		cfg.getEventListeners().setPostDeleteEventListeners(postDeleteEventListeners);
		cfg.getEventListeners().setPostUpdateEventListeners(postUpdateEventListeners);
	}

	public PostInsertEventListener[] getPostInsertEventListeners() {
		return postInsertEventListeners;
	}

	public void setPostInsertEventListeners(PostInsertEventListener[] postInsertEventListeners) {
		this.postInsertEventListeners = postInsertEventListeners;
	}

	public PostUpdateEventListener[] getPostUpdateEventListeners() {
		return postUpdateEventListeners;
	}

	public void setPostUpdateEventListeners(PostUpdateEventListener[] postUpdateEventListeners) {
		this.postUpdateEventListeners = postUpdateEventListeners;
	}

	public PostDeleteEventListener[] getPostDeleteEventListeners() {
		return postDeleteEventListeners;
	}

	public void setPostDeleteEventListeners(PostDeleteEventListener[] postDeleteEventListeners) {
		this.postDeleteEventListeners = postDeleteEventListeners;
	}

}
