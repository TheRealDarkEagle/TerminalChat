package v1.chatroom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import v1.model.Message;
import v1.model.User;

public class Chatroom implements Runnable {

	private static List<User> userlist;
	private List<Message> messagelist;
	private ExecutorService pool;

	public void joinRoom(User user) {
		pool.execute(this);
		for (User s : userlist) {
			s.getOutput().println("[SERVER] " + user.getUsername() + " has just joined :) ");
		}
		userlist.add(user);
		user.getOutput().println("willkommen im Chatroom der Empie :) " + user.getUsername());
	}

	public Chatroom() {
		pool = Executors.newFixedThreadPool(50);
		System.out.println("listen werden erstellt..");
		this.userlist = new ArrayList<User>();
		this.messagelist = new ArrayList<>();
	}

	@Override
	public void run() {
	}

}
