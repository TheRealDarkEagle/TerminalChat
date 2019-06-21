package model;

import java.io.Serializable;

import blueprints.Message;

public class CommandMessage extends Message implements Serializable {

	public CommandMessage(String username, String msg) {
		super(username, msg);
	}

}
