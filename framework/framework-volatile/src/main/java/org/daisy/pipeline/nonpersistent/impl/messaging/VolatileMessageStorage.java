package org.daisy.pipeline.nonpersistent.impl.messaging;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.properties.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Singleton
 */
@Component(
    name = "volatile-message-storage",
    service = { MessageStorage.class }
)
public class VolatileMessageStorage implements MessageStorage {

	private static final VolatileMessageStorage INSTANCE = new VolatileMessageStorage();
	private static final boolean VOLATILE_DISABLED = "true".equalsIgnoreCase(
		Properties.getProperty("org.daisy.pipeline.persistence"));
	private static final String CACHE_TIMEOUT_PROPERTY="org.daisy.pipeline.messaging.cache";
	private LoadingCache<String, List<Message>> cache;
	private static final Logger logger = LoggerFactory.getLogger(VolatileMessageStorage.class);

	public VolatileMessageStorage() {
		int timeout = Integer.valueOf(Properties.getProperty(
				CACHE_TIMEOUT_PROPERTY, "60"));
		cache = CacheBuilder.newBuilder()
				.removalListener(
						(RemovalNotification<String,List<Message>> n) -> n.getValue().clear())
				.expireAfterAccess(timeout, TimeUnit.SECONDS)
				.build(new CacheLoader<String, List<Message>>() {
					@Override
					public List<Message> load(String id) throws Exception {
						return Lists.newArrayList();
					}
				});
	}

	/**
	 * @throws RuntimeException if volatile storage is disabled through the org.daisy.pipeline.persistence system property.
	 */
	@Activate
	protected void activate() throws RuntimeException {
		if (VOLATILE_DISABLED)
			throw new RuntimeException("Volatile storage is disabled");
	}

	//just for testing don't use in production environments
	static void setTimeOut(int secs){
		logger.warn("Cache timeout was called. This is potentially dangerous outside testing environments!!!!");
		synchronized(INSTANCE){
			INSTANCE.cache = CacheBuilder.newBuilder()
				.removalListener(
						(RemovalNotification<String,List<Message>> n) -> n.getValue().clear())
				.expireAfterAccess(secs, TimeUnit.SECONDS)
				.build(new CacheLoader<String, List<Message>>() {
					@Override
					public List<Message> load(String id) throws Exception {
						return Lists.newArrayList();
					}
				});
		}

	}

	@Override
	public boolean add(Message msg) {
		//msgs less than info are discarded due to 
		//perfomance issues
		if (msg.getLevel().compareTo(Level.INFO)>0){
			return false;
		}
		try {
			this.cache.get(msg.getJobId()).add(msg);
			return true;
		} catch (ExecutionException e) {
			logger.warn("Error while adding message" , e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Message> get(String id){
		try {
			return this.cache.get(id);
		} catch (ExecutionException e) {
			logger.warn("Error while getting messages" , e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean remove(String id){
		this.cache.invalidate(id);
		return true;
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
