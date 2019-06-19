package v2.server.chatroom.model;

public class User {

	private String name;
	private long timestamp;

	public User(String name, long currentTimeMillis) {
		this.name = name;
		this.timestamp = currentTimeMillis;
	}

	public Long getTimestamp() {
		return this.timestamp;
	}

	public String getName() {
		return this.name;
	}

	public String qualifiedName() {
		return this.name + "~~" + timestamp;
	}

	public void setName(String name) {
		this.name = name;
	}

}
