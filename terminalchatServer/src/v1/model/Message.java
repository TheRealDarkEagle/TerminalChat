package v1.model;

public class Message {

	private String username;
	private String msg;

	public Message(String username, String msg) {
		this.username = username;
		this.msg = msg;
	}

	public String getMsg() {
		return String.format("%s : %s%n", username, msg);
	}

}
