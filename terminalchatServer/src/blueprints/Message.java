package blueprints;

import java.io.Serializable;

public abstract class Message implements IMessage, Serializable {

	private final String msg;
	private final String username;
	private final long timestamp;

	protected Message(String msg, String username, long timestamp) {
		this.msg = msg;
		this.username = username;
		this.timestamp = timestamp;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getMsg() {
		return this.msg;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

}
