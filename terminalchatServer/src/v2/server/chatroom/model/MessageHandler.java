package v2.server.chatroom.model;

import blueprints.Message;
import model.ChatMessage;
import model.CommandMessage;
import model.InfoMessage;
import model.PrivateMessage;
import model.User;
import v2.server.chatroom.Chatter;

public class MessageHandler {

	private Chatter chatroom;

	public MessageHandler(Chatter chatroom) {
		this.chatroom = chatroom;
	}

	public void handleMessage(Message m, User user) {
		if (m instanceof ChatMessage) {
			chatroom.sendClientMsg(m.getUsername() + "(" + user.getId() + ") : " + m.getMsg());
		} else if (m instanceof PrivateMessage) {
			chatroom.sendPrivateMsg((PrivateMessage) m);
		} else if (m instanceof InfoMessage) {
			chatroom.executeInfo((InfoMessage) m);
		} else if (m instanceof CommandMessage) {
			chatroom.executeCommand(m);
		}
	}

}
