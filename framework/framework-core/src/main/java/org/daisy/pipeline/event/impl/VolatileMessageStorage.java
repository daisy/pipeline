package org.daisy.pipeline.event.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.event.MessageStorage;

public class VolatileMessageStorage implements MessageStorage {

	private Map<String,List<Message>> messages = Collections.synchronizedMap(new HashMap<>());

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
}
