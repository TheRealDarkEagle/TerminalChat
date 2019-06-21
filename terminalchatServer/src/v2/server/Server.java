package v2.server;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import v2.server.chatroom.Chatter;

public class Server {

	private static String s = "[SERVER]";

	public static void main(String[] args) {
		System.out.println(s + " is running");
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try (ServerSocket listener = new ServerSocket(31337)) {
			while (true) {
				pool.execute(new Chatter(listener.accept()));
//				pool.execute(Chatter.getInstance(listener.accept()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
