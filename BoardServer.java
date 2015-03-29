import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * BoardServer drives the server for both BoardClients and BoardViewers. It utilizes
 * a multi-threaded solution to manage a (theoretically) unlimited number of clients.
 * It features a message backlog, which will be sent to all viewers upon connection.
 * It manages a user/password database (hashed, not plaintext), a login structure,
 * and a message transmission/notification system.
 * @author Andy
 * 
 */
public class BoardServer {
	
	private ServerSocket server;
	private ConcurrentLinkedDeque<Message> history;	// Immediate message history
	private ConcurrentHashMap<String, User> users;	// User database
	private ConcurrentHashMap<String, ClientHandler> online;	// Online users and handlers
	private ConcurrentLinkedDeque<ViewerHandler> viewers;
	
	/**
	 * This variable defines the message backlog, which includes the message itself,
	 * a timestamp, and the user who posted said message.
	 */
	public static final int MESSAGE_CACHE = 10;;
	
	/**
	 * Creates a new BoardServer on a given port
	 * @param port port to initialize on
	 * @throws IOException thrown if no IO
	 */
	public BoardServer(int port) throws IOException {
		server = new ServerSocket(port);
		history = new ConcurrentLinkedDeque<>();
		users = new ConcurrentHashMap<>();
		online = new ConcurrentHashMap<>();
		viewers = new ConcurrentLinkedDeque<>();
		System.out.println("BoardServer created on " + InetAddress.getLocalHost().getHostAddress());
	}
	
	/**
	 * Creates a backup of the current user database (does not include login states)
	 * @param filename name of file to write to
	 * @throws IOException if no IO
	 */
	public void backup(String filename) throws IOException {
//		FileWriter fileOut = new FileWriter()
	}
	
	/**
	 * Restores database data created by backup() method
	 * @param filename file to read from
	 * @throws IOException if no IO
	 */
	public void restore(String filename) throws IOException {
		
	}
	
	/**
	 * Creates and initializes the BoardServer
	 * @throws IOException if no IO
	 */
	public void start() throws IOException {
		System.out.println("BoardServer started");
		while(true) {
			Socket temp = server.accept();
			new PreHandler(temp).start();
//			new ClientHandler(temp).start();
		}
	}
	
	/**
	 * Removes the user with the username specified
	 * @param username username of User to be removed
	 * @throws IOException if no IO
	 */
	protected void removeUser(String username) throws IOException {
		ClientHandler handler = online.get(username);
		handler.logout();
		handler.close();
		users.remove(username);
	}
	
	/**
	 * Internal method for posting a message by a User. Called by BoardHandlers
	 * @param message message content to be posted
	 * @param usr reference to User
	 */
	private synchronized void postMessage(String message, User usr) {
		Message product = new Message(usr.getUsername(), message);
		history.addLast(product);
		while (history.size() >= MESSAGE_CACHE) {
			history.removeFirst();
		}
		notifyViewers(product.toString());
		System.out.println("Message posted: " + product);
	}
	
	private synchronized void notifyViewers(String text) {
		for (ViewerHandler handler : viewers) {
			handler.notifyMessage(text);
		}
	}
	
	// Some utility methods
	public static boolean badPassword(String password) {
		int length = password.length();
		return !(length >= 4 && length <= 18);
	}
	
	private class Message {
		private String source;
		private String content;
		private Date time;
		
		public Message(String source, String content) {
			this.source = source;
			this.content = content;
			time = new Date();
		}

		public String getSource() {
			return source;
		}

		public String getContent() {
			return content;
		}
		
		public Date getTime() {
			return time;
		}
		
		@Override
		public String toString() {
			return new StringBuffer("[").append(time.toString()).append("] ").append(source).append(": ").append(content).toString();
		}
		
	}
	
	private class User {
		private String username;
		private int passHash;
		
		public User(String username, String password) {
			this.username = username;
			passHash = getHash(password);
		}
		
		public String getUsername() {
			return username;
		}
		
		private int getHash(String data) {
			// temporary bullshit hashing algorithm
			return data.hashCode();
		}
		
		protected boolean identity(String username) {
			return username.equalsIgnoreCase(this.username);
		}
		
		protected boolean checkPass(String password) {
			return passHash == getHash(password);
		}
		
		@Override
		public boolean equals(Object other) {
			if (other == null) {
				return false;
			} else if (other == this) {
				return true;
			} else if (other instanceof User) {
				User temp = (User)other;
				return identity(temp.username);
//				return ((temp.username == this.username) && (temp.passHash == this.passHash));
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return username.toLowerCase().hashCode();
		}
	}
	
	private class PreHandler extends Thread {
		private Socket toSocket;
		private BufferedReader in;
		private PrintWriter out;
		
		public PreHandler(Socket sock) throws IOException {
			toSocket = sock;
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		}
		
		public void run() {
			String input;
			try {
				input = in.readLine();
				String header = Protocol.getHeader(input);
				switch(header) {
				case Protocol.CLIENT_READONLY : new ViewerHandler(toSocket).start(); break;
				case Protocol.CLIENT_READWRITE : new ClientHandler(toSocket).start(); break;
				default : out.println(Protocol.createPacket(Protocol.STATUS_ERR, "Unknown client identifier"));
				}
				
				// The handler's will deal with closing the streams
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private class ViewerHandler extends Thread {
		
		private Socket toViewer;
		private BufferedReader in;
		private PrintWriter out;

		public ViewerHandler(Socket viewer) throws IOException {
			viewers.add(this);
			toViewer = viewer;
			in = new BufferedReader(new InputStreamReader(viewer.getInputStream()));
			out = new PrintWriter(viewer.getOutputStream(), true);
		}
		
		public void run() {
			out.println(Protocol.createPacket(Protocol.STATUS_ACK, "ViewerHandler created"));
			handleSession();
		}
		
		private void handleSession() {
			// TODO finish server-side viewer protocol
			synchronized (history) {
				Iterator<Message> i = history.iterator();
				while (i.hasNext()) {
					notifyMessage(i.next().toString());
				}
			}
		}
		
		public synchronized void notifyMessage(String text) {
			try {
				out.println(Protocol.createPacket(Protocol.NOTIFY, text));
			} catch (Exception e) {
				e.printStackTrace();
				close();
			}
		}
		
		public synchronized void close() {
			out.println(Protocol.createPacket(Protocol.STATUS_END, "Server closed connection"));
			out.close();
			viewers.remove(this);
			try {
				in.close();
				toViewer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class ClientHandler extends Thread {
		
		private Socket toClient;
		private BufferedReader in;
		private PrintWriter out;
		private boolean loggedIn = false;
		private User user;
		
		public ClientHandler(Socket sock) throws IOException {
			toClient = sock;
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		}
		
		@Override
		public void run() {
			out.println(Protocol.createPacket(Protocol.STATUS_ACK, "ClientHandler Started"));
			System.out.println("ClientHandler for " + toClient.getInetAddress() + " started");
			try {
				if (handleLogin()) {
					System.out.println("Login successful");
					handleSession();
				} else {
					System.out.println("Login failed");
				}
				logout();
				close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		private boolean postMessage(String message) {
//			
//		}
		
		private void handleSession() throws IOException {
			do {
				String input = in.readLine();
				String header = Protocol.getHeader(input);
				String chunk = Protocol.getChunks(input)[1];
				if (header.equals(Protocol.ACT_POST)) {
					if (chunk.startsWith("/")) {
						// Manage commands
						chunk = chunk.substring(1);
						if (chunk.startsWith("quit")) {
							return;
						}
					} else {
						// Message contained in chunk
						postMessage(chunk, user);
						out.println(Protocol.createPacket(Protocol.STATUS_ACK, "Message Posted"));
					}
				} else {
					return;
				}
			} while (true);
		}
		
		private boolean handleLogin() throws IOException {
			String loginPacket = in.readLine();
			System.out.println("Received login packet: " + loginPacket);
			String header = Protocol.getHeader(loginPacket);
			String[] chunks = Protocol.getChunks(loginPacket);
			String username = chunks[1];
			String password = chunks[2];
			if (badPassword(password)) {
				out.println(Protocol.createPacket(Protocol.STATUS_ERR, "Bad password"));
				return false;
			}
			User key = new User(username, password);
			if (header.equals(Protocol.ACT_LOGIN)) {
				if (users.containsKey(username)) {
					if (users.get(username).checkPass(password)) {
						user = key;
						login();
						return true;
					} else {
						out.println(Protocol.createPacket(Protocol.STATUS_ERR, "Incorrect password"));
						return false;
					}
					
				} else {
					out.println(Protocol.createPacket(Protocol.STATUS_ERR, "User does not exist"));
					return false;
				}
			} else if (header.equals(Protocol.ACT_REGISTER)) {
				if (users.containsValue(key)) {
					loggedIn = false;
					out.println(Protocol.createPacket(Protocol.STATUS_ERR, "User already exists"));
					return false;
				} else {
					System.out.println("Adding new user " + username);
					users.put(username, key);
					System.out.println("User added. " + users.size() + " users in database");
					user = key;
					login();
//					out.println(Protocol.createPacket(Protocol.STATUS_ACK, "Registration and login successful"));
					return true;
				}
			} else {
				loggedIn = false;
				out.println(Protocol.createPacket(Protocol.STATUS_ERR, "Communication error"));
				return false;
			}
		}
		
		private synchronized void login() {
			loggedIn = true;
			online.put(user.getUsername(), this);
			out.println(Protocol.createPacket(Protocol.STATUS_ACK, "Login successful!"));
			notifyViewers(user.getUsername() + " has logged in");
		}
		
		private synchronized void logout() {
			loggedIn = false;
			if (user != null) {
				notifyViewers(user.getUsername() + " has logged out");
				online.remove(user.getUsername());
				user = null;
			}
		}
		
		
		
		public synchronized final void close() throws IOException {
			
			out.println(Protocol.createPacket(Protocol.STATUS_END, "Server closed connection"));
			System.out.println("stat_end sent");
			in.close();
			out.close();
			toClient.close();
			System.out.println("ClientHandler closed");
		}
	}
	
	// Usage: java BoardServer [port number]
	public static void main(String[] args) throws IOException {
		int port = Integer.parseInt(args[0]);
		BoardServer server = new BoardServer(port);
		server.start();
	}
}
