package blueprints;

public interface IMessage {

	String getUsername();

	String getMsg();

	long getTimestamp();

	static boolean isCommandMsg(String msg) {
		return (msg.startsWith("%%")) ? true : false;

	}
}
