import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public abstract class BoardClient {
	
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	
	// Error messages and whatnot
	protected static String SERV_ID = "Server says: ";
	protected static String LOGIN_SUCCESS = "Login successful!";
//	protected static String 
	
	private static final String ERR_APPEND = "Error: ";
	
	public BoardClient(String host, int port) throws IOException {
		client = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		out = new PrintWriter(client.getOutputStream(), true);
		System.out.println("Connection made to " + client.getInetAddress());
		
	}
	
	public final void start() throws IOException {
		System.out.println("Client started");
		out.println(Protocol.createPacket(Protocol.CLIENT_READWRITE));
		String response = in.readLine();
		System.out.println("ID Response: " + response);
		String header = Protocol.getHeader(response);
		if (header.equals(Protocol.STATUS_ACK)) {
			printMessage("Welcome to <placeholder>!");
			if (login()) {
				printMessage(LOGIN_SUCCESS);
				handleSession();
			}	
		} else {
			printError(Protocol.getChunks(response)[1]);
		}
		close();
	}
	
	private void handleSession() throws IOException {
		printMessage("/quit to terminate session");
		do {
			String input = getPost();
			if (input.equals("")) {
				continue;
			}
			out.println(Protocol.createPacket(Protocol.ACT_POST, input));
			String response = in.readLine();
			String respHeader = Protocol.getHeader(response);
//			System.out.println("Response: " + respHeader);
			if (respHeader.equals(Protocol.STATUS_ACK)) {
//				printMessage("Message posted successfully");
			} else if (respHeader.equals(Protocol.STATUS_END)) {
				printMessage(Protocol.getChunks(response)[1]);
				return;
			} else {
				printError(Protocol.getChunks(response)[1]);
				return;
			}
		} while (true);
	}
	
	private final boolean login() throws IOException {
		boolean register = isRegistering();
		String username = getUsername();
		String password;
		String header;
		if (register) {
			header = Protocol.ACT_REGISTER;
			password = getPassword(true);
		} else {
			header = Protocol.ACT_LOGIN;
			password = getPassword(false);
		}
		String packet = Protocol.createPacket(header, username, password);
		out.println(packet);
		String response = in.readLine();
		String resHeader = Protocol.getHeader(response);
//		System.out.println("Login response: " + response);
		if (resHeader.equals(Protocol.STATUS_ACK)) {
			return true;
		} else {
			printError(new StringBuffer(SERV_ID).append(Protocol.getChunks(response)[1]).toString());
			return false;
		}
	}
	
	private final void printError(String err) {
		printMessage(new StringBuffer(ERR_APPEND).append(err).toString());
	}
	
	protected abstract void printMessage(String mes);
	
	protected abstract boolean isRegistering();
	
	protected abstract String getUsername();
	
	protected abstract String getPassword(boolean register);
	
	protected abstract String getPost();
	
	public final void close() throws IOException {
		out.println(Protocol.createPacket(Protocol.STATUS_END, "Client closed connection"));
		in.close();
		out.close();
		client.close();
	}
}
