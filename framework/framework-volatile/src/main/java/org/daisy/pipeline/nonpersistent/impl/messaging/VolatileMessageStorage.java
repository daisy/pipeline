package org.daisy.pipeline.nonpersistent.impl.messaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.properties.Properties;
import org.daisy.pipeline.event.MessageStorage;

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
	private Map<String,List<Message>> messages = Collections.synchronizedMap(new HashMap<>());

	public VolatileMessageStorage() {
	}

	/**
	 * @throws RuntimeException if volatile storage is disabled through the org.daisy.pipeline.persistence system property.
	 */
	@Activate
	protected void activate() throws RuntimeException {
		if (VOLATILE_DISABLED)
			throw new RuntimeException("Volatile storage is disabled");
	}

	@Override
	public boolean add(Message msg) {
		//msgs less than info are discarded due to 
		//perfomance issues
		if (msg.getLevel().compareTo(Level.INFO)>0){
			return false;
		}
		List<Message> list = get(msg.getOwnerId());
		list.add(msg);
		return true;
	}

	@Override
	public List<Message> get(String id){
		List<Message> list = messages.get(id);
		if (list == null) {
			list = Lists.newArrayList();
			messages.put(id, list);
		}
		return list;
	}

	@Override
	public boolean remove(String id){
		messages.remove(id);
		return true;
	}

	// for use in tests
	void removeAll(){
		messages.clear();
	}
	/**
	 * @return the instance
	 */
	public static VolatileMessageStorage getInstance() {
		return INSTANCE;
	}
}
