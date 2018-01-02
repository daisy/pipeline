package org.daisy.pipeline.nonpersistent.impl.messaging;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.properties.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;

/**
 * Singleton
 */
public final class VolatileMessageStorage {

	private static final VolatileMessageStorage INSTANCE = new VolatileMessageStorage();
	private static final  String CACHE_TIMEOUT_PROPERTY="org.daisy.pipeline.messaging.cache";
	private LoadingCache<String, List<Message>> cache;
	private static final Logger logger = LoggerFactory.getLogger(VolatileMessageStorage.class);

	/**
	 *
	 */
	private VolatileMessageStorage() {
		int timeout = Integer.valueOf(Properties.getProperty(
				CACHE_TIMEOUT_PROPERTY, "60"));
		cache = CacheBuilder.newBuilder()
				.expireAfterAccess(timeout, TimeUnit.SECONDS)
				.build(new CacheLoader<String, List<Message>>() {
					@Override
					public List<Message> load(String id) throws Exception {
						return Lists.newArrayList();
					}
				});
	}
	//just for testing don't use in production environments
	static void setTimeOut(int secs){
		logger.warn("Cache timeout was called. This is potentially dangerous outside testing environments!!!!");
		synchronized(INSTANCE){
			INSTANCE.cache = CacheBuilder.newBuilder()
				.expireAfterAccess(secs, TimeUnit.SECONDS)
				.build(new CacheLoader<String, List<Message>>() {
					@Override
					public List<Message> load(String id) throws Exception {
						return Lists.newArrayList();
					}
				});
		}

	}

	public boolean add(Message msg) {
		//msgs less than info are discarded due to 
		//perfomance issues
		if (msg.getLevel().compareTo(Level.INFO)>0){
			return false;
		}
		try {
			this.cache.get(msg.getJobId()).add(msg.getSequence(), msg);
			return true;
		} catch (ExecutionException e) {
			logger.warn("Error while adding message" , e);
			return false;
		}
	}

	public List<Message> get(String id){
		try {
			return this.cache.get(id);
		} catch (ExecutionException e) {
			return Collections.emptyList();
		}
	}

	public void remove(String id){
		this.cache.invalidate(id);
	}

	void removeAll(){
		this.cache.invalidateAll();
		this.cache.cleanUp();
	}
	/**
	 * @return the instance
	 */
	public static VolatileMessageStorage getInstance() {
		return INSTANCE;
	}
}
