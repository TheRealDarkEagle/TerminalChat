package v2.server.chatroom;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;

import v2.server.chatroom.model.User;

/*
 * @TODO
 * in und out threaden ->
 * input eigener thread(?) löst einen output thread(?) aus?
 *	teste ob in noch eine zeile hat ===!!!!!
 * disconnecten nachricht geht gerade nciht
 * user commands einfügen -> rename etc kennst ja ne
 */
public class Chatter implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(Chatter.class.getName());

	private static Map<User, PrintWriter> usermap = new HashMap<>();
	private User user;
	private Socket socket;
	private Scanner in;
	private PrintWriter out;

	public Chatter(Socket socket) {
		try {
			LOGGER.info(socket.toString());
			LOGGER.info("User joined... starting process");
			System.out.println(socket);
			this.socket = socket;
			LOGGER.info("in just created");
			in = new Scanner(socket.getInputStream());
			LOGGER.info("out just created");
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (Exception e) {
			LOGGER.info("Ein Fehler occured! ->" + e.getMessage());
		}

	}

	@Override
	public void run() {
		try {
			welcomeUser();
			System.out.println("\"" + user.getName() + "\" [" + socket + "] connected");
			while (true) {
				getMsg();
			}
		} catch (NoSuchElementException e) {
			LOGGER.warning("FEHLER -> " + e.getMessage());
		} catch (NullPointerException nupi) {
			LOGGER.warning("Nuller -> " + nupi.getMessage());
		}
	}

	/*
	 * @TODO beim überprüfen des namens nicht alle spaces löschen!
	 */
	private void welcomeUser() {
		String name = "";
		do {
			if (in.hasNextLine()) {
				name = in.nextLine();
				name = name.replaceAll("[ ]", "");
				if (name.isEmpty()) {
					out.println("Ungültiger Username!\r\n Bitte Benutzernamen eingeben:");
				}
			} else {
				break;
			}
		} while (name.isEmpty() || name.startsWith(" "));
		String username = name;
		this.user = new User(username, System.currentTimeMillis());
		sendClientMsg(user.getName() + " just joined");
		out.println("Hi " + user.getName() + " :) zum leaven \"/quit\" eingeben");
		usermap.put(user, out);
	}

	/*
	 * Sendet an alle vorhanden Clients die übergebene Msg
	 */
	private void sendClientMsg(String s) {
		for (Map.Entry<User, PrintWriter> user : usermap.entrySet()) {
			if (!user.getKey().qualifiedName().equals(this.user.qualifiedName())) {
				user.getValue().println(s);
			}
		}
	}

	/*
	 * Bereitet eine Servermsg vor und gibt diese and sendClientMsg weiter
	 */
	private void serverMsg(String msg) {
		sendClientMsg(msg);
	}

	private void removeUser() {
		usermap.get(this.user).close();
		usermap.remove(this.user);
		System.out.println("\"" + this.user.getName() + "\" [" + socket + "] disconnected");
		serverMsg(user.getName() + " has left!");
	}

	private void getMsg() {
		try {
			String s = in.nextLine();
			while (s.startsWith(" ")) {
				s = s.substring(1);
			}
			if (!s.isEmpty()) {
				System.out.println(user.getName() + " : " + s);
				sendClientMsg(user.getName() + " : " + s);
			}
		} catch (NoSuchElementException nsee) {
			// Wenn in.nextLine einen Fehler findet,
			// Remove den diesbezüglichen User
			LOGGER.info("User disconnected -> " + nsee.getMessage());
			removeUser();
			try {
			} catch (NullPointerException e) {
				LOGGER.warning("In -> FEHLER -> nullpointer -> " + e.getMessage());
			}
		} catch (Exception e) {
//			removeUser();
			LOGGER.warning("EXCEPTION ->" + e.getMessage());
		}
	}

}
