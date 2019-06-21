package model;

import java.io.Serializable;

import blueprints.Message;

public class InfoMessage extends Message implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8060429065851752324L;

	public InfoMessage(String username, String msg) {
		super(username, msg);
	}

}
