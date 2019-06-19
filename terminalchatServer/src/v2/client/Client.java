package v2.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

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

	public static Client getInstance() {
		if (instance == null) {
			instance = new Client("10.176.51.108");
		}
		return instance;
	}

	private String serverAddress;

	private Client(String serverAddress) {
		LOGGER.info("application started");
		this.serverAddress = serverAddress;
		start();
	}

	/*
	 * Stellt die Verbindung zum Server her -> kreiert ReciveMsg und SendMsg-Thread
	 * und startet diese
	 */
	private void start() {
		try {
			this.socket = new Socket(serverAddress, port);
			LOGGER.info(socket.toString());
			System.out.println(socket);
			this.receiveMsgThread = new ReciveMsg(this.socket.getInputStream());
			this.sendMsgThread = new SendMsg(this.socket.getOutputStream());
			this.receiveMsgThread.start();
			this.sendMsgThread.start();
		} catch (ConnectException ce) {
			// Server nicht erreichbar
			LOGGER.warning("Server does not response! " + ce.getMessage());
			reconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Wenn ein ungewollter Verbindungsabbruch entsteht, wird hier der Socket
	 * geschlossen und start() wird angesprochen
	 */
	public void reconnect() {
		LOGGER.info("Tring to reconnect");
		LOGGER.fine("Socket infos: \r\n" + this.socket);
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
			LOGGER.info("Thread got interrupted! " + e.getMessage());
		} catch (NullPointerException nupi) {
			LOGGER.info("Etwas war null!! -> " + nupi.getMessage());
		}

		System.out.println("Socket infos: \r\n" + this.socket);

		start();
	}

	class SendMsg extends Thread {

		PrintWriter writer = null;
		boolean exitThread = false;

		public SendMsg(OutputStream outputStream) {
			writer = new PrintWriter(outputStream, true);
		}

		@Override
		public void run() {
			LOGGER.info("SendMsg startet!");
			BufferedReader br = null;
			try {
				String output;
				br = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("bitte Benutzernamen eingeben!: ");
				while ((!(output = br.readLine()).equals("/quit"))) {
					System.out.println(output);
					writer.println(output);
				}
				recon = false;
				br.close();
				writer.close();
			} catch (SocketException s) {
				LOGGER.info("Server closed! " + s.getMessage());
			} catch (Exception e) {
				LOGGER.warning("Ein SendMsg Fehler ist aufgetreten! " + e.getMessage());
			} finally {
				try {
					writer.close();
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
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
				LOGGER.warning("A Recive error Occured! " + e.getMessage());
			}
		}
	}
}
