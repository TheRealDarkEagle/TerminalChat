package blueprints;

import java.io.Serializable;

public abstract class Message implements IMessage, Serializable {

	private final String msg;
	private final String username;
	private final long timestamp;

	protected Message(String username, String msg) {
		this.msg = msg;
		this.username = username;
		this.timestamp = System.currentTimeMillis();
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
