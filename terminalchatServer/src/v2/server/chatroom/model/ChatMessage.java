package v2.server.chatroom.model;

import blueprints.Message;

public class ChatMessage extends Message {

	ChatMessage(String msg, String username, long timestamp) {
		super(msg, username, timestamp);
	}

}
