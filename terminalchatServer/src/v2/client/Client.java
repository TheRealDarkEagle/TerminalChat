package v2.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import blueprints.Message;
import model.ChatMessage;
import model.CommandMessage;
import model.InfoMessage;
import model.PrivateMessage;

/*
 * @TODO
 * reconnect einrichten wenn server nicht mehr erreichbar ist
 * timeout einrichten wenn server nicht mehr erreichbar ist
 * ip vllt dynamisch beziehen?
 */
public class Client {

	public static void main(String[] args) {
		Client.getInstance();
	}

	private static final Logger LOGGER = Logger.getLogger(Client.class.getName());

	private static Client instance;
	public boolean recon = true;
	private Socket socket;
	private ReciveMsg receiveMsgThread;
	private SendMsg sendMsgThread;
	private int port = 31337;
	private String username;
	private String serverAddress;

	public static Client getInstance() {
		if (instance == null) {
			instance = new Client("10.176.51.108");
		}
		return instance;
	}

	private Client(String serverAddress) {
		LOGGER.setLevel(Level.WARNING);
		LOGGER.info("application started");
		this.serverAddress = serverAddress;
		this.username = createUsername();
		start();
	}

	private String createUsername() {
		System.out.println("bitte Benutzernamen eingeben!: ");
		String choosenName = "";
		Scanner scanner = new Scanner(System.in);
		do {
			choosenName = scanner.nextLine();
			choosenName = choosenName.replaceAll("[ ]", "");
			if (choosenName.isEmpty()) {
				System.out.println("Ungültiger Username!\r\n Bitte Benutzernamen eingeben:");
			}
		} while (choosenName.isEmpty() || choosenName.startsWith(" "));
		return choosenName;
	}

	/*
	 * Stellt die Verbindung zum Server her -> kreiert ReciveMsg und SendMsg-Thread
	 * und startet diese
	 */
	private void start() {
		try {
			this.socket = new Socket(serverAddress, port);
			LOGGER.info(socket.toString());
			this.receiveMsgThread = new ReciveMsg(this.socket.getInputStream());
			this.sendMsgThread = new SendMsg(this.socket.getOutputStream());
			this.receiveMsgThread.start();
			this.sendMsgThread.start();
		} catch (ConnectException ce) {
			// Server nicht erreichbar
			LOGGER.warning("Server does not response! " + ce.getMessage());
			reconnect();
		} catch (Exception e) {
			LOGGER.warning("Exception beim starten des Programmes! -> " + e.getMessage());
		}
	}

	/*
	 * Wenn ein ungewollter Verbindungsabbruch entsteht, wird hier der Socket
	 * geschlossen und start() wird angesprochen
	 */
	public void reconnect() {
		LOGGER.info("Tring to reconnect");
		LOGGER.fine("Socket infos: \r\n " + this.socket);
		try {
			Thread.sleep(5000);
			this.socket.close();
			receiveMsgThread.in.close();
			sendMsgThread.writer.close();
			receiveMsgThread.exitThread = true;
			sendMsgThread.exitThread = true;
			this.socket.bind(null);
		} catch (IOException e) {
			LOGGER.warning("IO EXECPTION -> " + e.getMessage());
		} catch (InterruptedException e) {
			LOGGER.warning("Thread got interrupted! " + e.getMessage());
			Thread.currentThread().interrupt();
		} catch (NullPointerException nupi) {
			LOGGER.warning("Etwas war null!! -> " + nupi.getMessage());
		}
		System.out.println("Socket infos: \r\n" + this.socket);
		start();
	}

	class SendMsg extends Thread {

		ObjectOutputStream writer = null;
		boolean exitThread = false;

		public SendMsg(OutputStream outputStream) throws IOException {
			writer = new ObjectOutputStream(outputStream);
		}

		@Override
		public void run() {
			LOGGER.info("SendMsg startet!");
			BufferedReader br = null;
			try {
				Message m = new InfoMessage(username, "connected");
				writer.writeObject(m);
				String output;
				br = new BufferedReader(new InputStreamReader(System.in));
				while ((!(output = br.readLine()).equals("/quit"))) {
					sendMsg(createMsg(output));
				}
				recon = false;
				br.close();
				writer.close();
			} catch (SocketException s) {
				LOGGER.info("Server closed! " + s.getMessage());
			} catch (Exception e) {
				LOGGER.warning("Ein SendMsg Fehler ist aufgetreten! " + e.getMessage());
				try {
					writer.reset();
				} catch (IOException e1) {
					LOGGER.warning("IO Exception beim Senden aufgetreten -> " + e1.getMessage());
				}
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.warning("IO Exception im FinallyBlock des Sendens! ->" + e.getMessage());
				}
			}
		}

		private void sendMsg(Message m) {
			if (!m.getMsg().isEmpty()) {
				try {
					writer.writeObject(m);
				} catch (IOException e) {
					LOGGER.warning("ein fehler ist aufgetreten! -> " + e.getMessage());
					this.interrupt();
					try {
						this.finalize();
					} catch (Throwable e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

		/*
		 * Erstellt ein MessageObjekt -> achtet daruf ob es eine ChatMessage war oder
		 * eine CommandMessage!
		 */
		private Message createMsg(String msg) {
			msg = removeStartingSpaces(msg);
			// Kreiere eine personalMessage objekt -> username, msg, destinationUser
			if (msg.startsWith("/#@")) {
				String recivinguser = msg.substring(msg.indexOf('@') + 1, msg.indexOf(' ', msg.indexOf('@' + 1)));
				return new PrivateMessage(recivinguser, msg, username);
			} else if (msg.startsWith("/#name")) {
				username = msg.substring(msg.indexOf(' ') + 1).trim();
				return new InfoMessage(username, "nameChange:" + msg.substring(msg.indexOf(' ')));
			} else {
				return msg.startsWith("/#") ? new CommandMessage(username, msg) : new ChatMessage(username, msg);
			}
		}

		private String removeStartingSpaces(String msg) {
			if (msg.startsWith(" ")) {
				do {
					msg = msg.substring(1);
				} while (msg.startsWith(" "));

			}
			return msg;
		}
	}

	class ReciveMsg extends Thread {

		InputStreamReader in = null;
		boolean exitThread = false;

		public ReciveMsg(InputStream inputStream) throws IOException {
			in = new InputStreamReader(inputStream);
		}

		@Override
		public void run() {
			LOGGER.info("ReciveMsg started!");
			String line;
			BufferedReader br = null;
			try {
				br = new BufferedReader(in);
				while ((line = br.readLine()) != null || !recon || !exitThread) {
					if (line.startsWith("null")) {
						break;
					}
					System.out.println(line);
				}
			} catch (SocketException se) {
				if (recon) {
					LOGGER.warning("Server went Down! " + se.getMessage());
					// Server geht im laufenden Betrieb offline!
					reconnect();
				} else {
					System.out.println("Disconnected from Server!");
				}
			} catch (Exception e) {
				System.out.println("U got kicked From the Server!");
				sendMsgThread.interrupt();
				LOGGER.warning("A Recive error Occured! " + e.getMessage());
			}
		}
	}
}
