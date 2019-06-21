package v1.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import v1.chatroom.Chatroom;
import v1.model.User;

/*
 * Nimmt die erste Anfrage eines Clients auf
 * sendet diese an den Register-Server zwecks username
 * wartet dann wieder auf neue anfrage eines Clients
 */
public class Server {

	public static void main(String[] args) throws IOException {
		new Server().getInstance();

	}

	private static Server instance;
	private static int port = 53598;

	private ServerSocket socket;
	private String s = "[SERVER]";
	private Chatroom chat;

	private Server() {
		startServer(Server.port);
		waitForUser();
	}

	private void startServer(int port) {
		System.out.println(s + " starting...");
		try {
			this.socket = new ServerSocket(port);
			this.chat = new Chatroom();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void waitForUser() {
		ExecutorService pool = Executors.newFixedThreadPool(50);
		while (true) {
			System.out.println(s + " warte auf Verbindungsanfrage...");
			try {
				this.chat.joinRoom(new User(this.socket.accept()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

}
