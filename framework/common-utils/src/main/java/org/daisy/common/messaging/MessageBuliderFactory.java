package org.daisy.common.messaging;

import org.daisy.common.messaging.Message.MessageBuilder;

public class MessageBuliderFactory {

	public MessageBuilder newMessageBuilder(){
			return new Message.MessageBuilder();
	}


}
