package model;

public class User {

	private String name;
	private long timestamp;
	private final int id;
	private boolean admin;

	public User(String name, long currentTimeMillis, int id, boolean admin) {
		this.name = name;
		this.id = id;
		this.timestamp = currentTimeMillis;
		this.admin = admin;
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

	public int getId() {
		return this.id;
	}

	public boolean isAdmin() {
		return this.admin;
	}

}
