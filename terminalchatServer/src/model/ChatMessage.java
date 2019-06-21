package model;

import java.io.Serializable;

import blueprints.Message;

public class ChatMessage extends Message implements Serializable {

	public ChatMessage(String username, String msg) {
		super(username, msg);
	}

}
