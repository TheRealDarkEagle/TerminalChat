package v2.server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import v2.server.chatroom.Chatter;

public class Server {

	private static String s = "[SERVER]";
	private static Set<PrintWriter> writers = new HashSet<>();

	public static void main(String[] args) {
		System.out.println(s + " is running");
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try (ServerSocket listener = new ServerSocket(31337)) {
			while (true) {
				pool.execute(new Chatter(listener.accept()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
