package org.daisy.pipeline.nonpersistent.impl.messaging;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.event.EventBusProvider;

import com.google.common.eventbus.Subscribe;

/**
 * This class receives message events and stores them in memory.  
 */
public class VolatileMessageEventListener {

	private EventBusProvider eventBusProvider;


	public void setEventBusProvider(EventBusProvider eventBusProvider) {
		this.eventBusProvider = eventBusProvider;
		this.eventBusProvider.get().register(this);
	}

	@Subscribe
	public synchronized void handleMessage(Message msg) {
		VolatileMessageStorage.getInstance().add(msg);
	}

}
