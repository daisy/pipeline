package org.daisy.pipeline.persistence.impl.messaging;

import javax.persistence.EntityManagerFactory;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.persistence.impl.Database;

import com.google.common.eventbus.Subscribe;

/**
 * This class receives message events and stores them in memory and gives access
 * to them via the accessor interface. The class that is interested in
 * processing a memoryMessage
 * 
 */
public class PersistentMessageEventListener {

	EventBusProvider eventBusProvider;
	Database datbase;

	public void setEventBusProvider(EventBusProvider eventBusProvider) {
		this.eventBusProvider = eventBusProvider;
		this.eventBusProvider.get().register(this);
	}

	public synchronized void setEntityManagerFactory(EntityManagerFactory emf) {
		this.datbase = new Database(emf);
	}

	@Subscribe
	public synchronized void handleMessage(Message msg) {
		if (datbase != null) {
			datbase.addObject(new PersistentMessage(msg));
		}
	}

}
