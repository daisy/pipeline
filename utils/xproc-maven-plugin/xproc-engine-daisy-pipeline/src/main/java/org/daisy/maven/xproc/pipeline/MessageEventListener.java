package org.daisy.maven.xproc.pipeline;

import com.google.common.eventbus.Subscribe;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.event.EventBusProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component
public class MessageEventListener {
	
	@Reference(
		name = "EventBusProvider",
		unbind = "-",
		service = EventBusProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEventBusProvider(EventBusProvider provider) {
		provider.get().register(this);
	}
	
	@Subscribe
	public synchronized void handleMessage(Message message) {
		System.out.printf("[%s] %s\n", message.getLevel(), message.getText());
	}
}
