package model;

import java.io.Serializable;

public class PrivateMessage extends CommandMessage implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 6262968702593606456L;

	public PrivateMessage(String recivinguser, String msg, String username) {
		super(username, msg);
	}

	@Override
	public String getMsg() {
		String msg = super.getMsg().substring(3, super.getMsg().length());
		msg = msg.substring(msg.indexOf(' ') + 1);
		return msg;
	}

	public int getDestination() {
		return Integer
				.parseInt(super.getMsg().substring(super.getMsg().indexOf("~#~") + 4, super.getMsg().indexOf(' ')));
	}

}
