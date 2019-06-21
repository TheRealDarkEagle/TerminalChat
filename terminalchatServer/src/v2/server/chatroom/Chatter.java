package v2.server.chatroom;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import blueprints.Message;
import model.InfoMessage;
import model.PrivateMessage;
import model.User;
import utils.Commands;
import v2.server.chatroom.model.MessageHandler;

/*
 * @TODO
 * disconnecten nachricht geht gerade nciht
 * afk modus implementieren -> wenn user afk geht wird er in einer afk map gespeichert
 * diese erhält dann solange dieser user afk ist die nachrichten.
 * Diese werden beim "nicht mehr afk sein" dann an den user ausgespielt
 * info geht an alles user heraus, wenn user afk geht oder wiederkommt
 *
 * vllt auch eine automatische afk setzung? wenn user länger als x zeit keine nachricht gesendet hat?
 * wobei dieses eine schlechte idee ist, da ein user auch eine zeit lang einfach nicht schreiben möchte(?)
 */
public class Chatter implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(Chatter.class.getName());
	private static Map<User, PrintWriter> usermap = new HashMap<>();
	private static int id = 0;
	private static boolean needAdmin = true;
	private Commands commands;
	private User user;
	private Socket socket;
	private ObjectInputStream objectreciver;
	private PrintWriter out;
	private MessageHandler handler;

	public Chatter(Socket socket) {
		handler = new MessageHandler(this);
		commands = new Commands();
		LOGGER.setLevel(Level.WARNING);
		try {
			LOGGER.info(socket.toString());
			LOGGER.info("User joined... starting process");
			System.out.println(socket);
			this.socket = socket;
			LOGGER.info("in just created");
			this.objectreciver = new ObjectInputStream(socket.getInputStream());
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
				try {
					deriveMsg((Message) objectreciver.readObject());
				} catch (ClassNotFoundException e) {
					LOGGER.warning("CLASS NOT FOUND EXPECTION -> " + e.getMessage());
				}
			}
		} catch (NoSuchElementException e) {
			LOGGER.warning("FEHLER -> " + e.getMessage());
		} catch (NullPointerException nupi) {
			LOGGER.warning("Nuller -> " + nupi.getMessage());
		} catch (IOException ioe) {
			LOGGER.warning("Client disconnected!! -> " + ioe.getMessage());
			removeUser(this.user);
		}
	}

	private void welcomeUser() throws IOException {
		String username = "";
		try {
			Message m = (Message) objectreciver.readObject();
			username = m.getUsername();
		} catch (ClassNotFoundException e) {
			LOGGER.warning("CLASS NOT FOUND!!!! -> " + e.getMessage());
		}
		this.user = new User(username, System.currentTimeMillis(), ++id, this.needAdmin);
		sendClientMsg(user.getName() + " just joined");
		out.println("Hi " + user.getName() + " :) zum leaven \"/quit\" eingeben");
		selfMsg("Für eine Liste der Kommandos bitte \"/#com\" eingeben!");
		usermap.put(user, out);
		needAdmin = false;
	}

	/*
	 * Sendet an alle vorhanden Clients die übergebene Msg
	 */
	public void sendClientMsg(String s) {
		for (Map.Entry<User, PrintWriter> userentry : usermap.entrySet()) {
			if (!userentry.getKey().qualifiedName().equals(this.user.qualifiedName())) {
				userentry.getValue().println(s);
			}
		}
	}

	/*
	 * Bereitet eine Servermsg vor und gibt diese and sendClientMsg weiter
	 */
	private void serverMsg(String msg) {
		sendClientMsg("[SERVER] " + msg);
	}

	/*
	 * Nimmt user aus der Liste
	 */
	private void removeUser(User invalidUser) {
		try {
			usermap.get(invalidUser).close();
		} catch (NullPointerException ex) {
			LOGGER.warning("Kann verbindung nicht schließen, da schon null -> " + ex.getMessage());
		}
		usermap.remove(invalidUser);
		System.out.println("\"" + invalidUser.getName() + "\" [" + socket + "] disconnected");
		serverMsg(invalidUser.getName() + " has left!");
	}

	/*
	 * Hier wird die Message geprüft, welche Art sie wirklich ist ->
	 * Chat/Command/Info
	 */
	private void deriveMsg(Message m) {
		handler.handleMessage(m, this.user);
	}

	/*
	 * @TODO AFK kommando einfügen
	 */
	public void executeCommand(Message m) {

		String msg = (m.getMsg().contains(" ")) ? m.getMsg().substring(0, m.getMsg().indexOf(' ')) : m.getMsg();
		switch (msg) {
		case "/#com":
			sendCommands();
			break;
		case "/#time":
			selfMsg(String.valueOf(new Date()));
			break;
		case "/#userlist":
			for (Map.Entry<User, PrintWriter> usernames : usermap.entrySet()) {
				selfMsg(usernames.getKey().getName() + " \twhisperID->  " + usernames.getKey().getId());
			}
			break;
		case "/#kick":
			if (this.user.isAdmin()) {
				kickUser(Integer.parseInt(m.getMsg().substring(m.getMsg().indexOf(' ') + 1, m.getMsg().length())));
			}
			break;
		case "/#afk":
			selfMsg("Kommand in progress... coming soon©");
			break;
		case "/#name":
			String newUsername = msg.substring(msg.indexOf(' ') + 1, msg.length());
			this.user.setName(newUsername);
			selfMsg("Dein Name wurde geändert -> " + this.user.getName());
			break;
		default:
			selfMsg("[SERVER] Kommando nicht bekannt - Sry Dude!");
			break;
		}
	}

	private void sendCommands() {
		for (Map.Entry<String, String> commandlist : commands.getCommands(this.user.isAdmin()).entrySet()) {
			if (commandlist.getKey().length() < 9) {
				selfMsg(String.format("%s \t\t\t-> %s", commandlist.getKey(), commandlist.getValue()));
			} else if (commandlist.getKey().length() < 15) {
				selfMsg(String.format("%s \t\t-> %s", commandlist.getKey(), commandlist.getValue()));
			} else {
				selfMsg(String.format("%s \t-> %s", commandlist.getKey(), commandlist.getValue()));
			}
		}
	}

	/*
	 * Sendet eine Private Nachricht
	 */
	public void sendPrivateMsg(PrivateMessage m) {
		for (Map.Entry<User, PrintWriter> pm : usermap.entrySet()) {
			if (pm.getKey().getId() == m.getDestination()) {
				pm.getValue().println("[" + m.getUsername() + "(" + this.user.getId() + ")] -> " + m.getMsg());
				return;
			}
		}
		this.out.print("UserID nicht bekannt!");
	}

	public void executeInfo(InfoMessage m) {
		if (m.getMsg().contains("nameChange:")) {
			changeUsername(m.getMsg().substring(m.getMsg().indexOf(':') + 1));
		}
	}

	private void changeUsername(String newname) {
		newname = newname.trim();
		String oldUsername = this.user.getName();
		usermap.remove(this.user);
		PrintWriter newOut = this.out;
		User test = new User(newname, this.user.getTimestamp(), this.user.getId(), this.user.isAdmin());
		this.user.setName(newname);
		usermap.put(test, newOut);
		selfMsg("Name wurde geändert ->" + newname);
		serverMsg("[INFO] " + oldUsername + " -> " + newname);

	}

	private void selfMsg(String msg) {
		this.out.println(msg);
	}

	private void kickUser(int id) {
		for (Map.Entry<User, PrintWriter> volk : usermap.entrySet()) {
			if (volk.getKey().getId() == id) {
				removeUser(volk.getKey());
				break;
			}
		}
	}
}
