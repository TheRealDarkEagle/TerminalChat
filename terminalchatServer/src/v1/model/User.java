package v1.model;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class User {

	private String username;
	private String ip;
	private Scanner input;
	private PrintWriter output;
	private Socket socket;

	public User(Socket socket) {
		try {
			this.socket = socket;
			this.input = new Scanner(socket.getInputStream());
			this.output = new PrintWriter(socket.getOutputStream(), true);
			setUserInfo();
		} catch (Exception e) {
			System.out.println("Verbindung gescheitert...");
		}

	}

	private void setUserInfo() {
		output.println("Bitte Usernamen eingeben: ");
		this.username = input.nextLine();
		output.println(String.format("%s", "danke " + username + " Sie werden mit dem Chat Verbunden...."));
	}

	public String getUsername() {
		return this.username;
	}

	public String getIp() {
		return this.ip;
	}

	public Scanner getInput() {
		return this.input;
	}

	public PrintWriter getOutput() {
		return this.output;
	}

	public Socket getSocket() {
		return this.socket;
	}

}
