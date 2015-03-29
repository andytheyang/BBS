import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class BoardViewer {

	private Socket viewer;
	private BufferedReader in;
	private PrintWriter out;
	private Scanner stdin;
	
	private static final String ERR_APPEND = "Error: ";
	
	public BoardViewer(String host, int port) throws IOException {
		viewer = new Socket(host, port);
		in = new BufferedReader(new InputStreamReader(viewer.getInputStream()));
		out = new PrintWriter(viewer.getOutputStream(), true);
		System.out.println("Connection made to " + viewer.getInetAddress());
	}
	
	public void start() throws IOException {
		System.out.println("Viewer started");
		out.println(Protocol.createPacket(Protocol.CLIENT_READONLY));
		String response = in.readLine();
		System.out.println("ID Response: " + response);
		String header = Protocol.getHeader(response);
		if (header.equals(Protocol.STATUS_ACK)) {
			handleSession();
		} else {
			printError(Protocol.getChunks(response)[1]);
		}
		
		
		close();
	}
	
	public void handleSession() throws IOException {
		// TODO finish client-side protocol
		
		while (true) {
			String input = in.readLine();
			String header = Protocol.getHeader(input);
			String chunk = Protocol.getChunks(input)[1];
			if (header.equals(Protocol.NOTIFY)) {
				System.out.println(chunk);
			} else {
				printError(chunk);
			}
		}
	}
	
	public void printError(String err) {
		System.out.println(new StringBuffer(ERR_APPEND).append(err).toString());
	}
	
	public void close() throws IOException {
		out.println(Protocol.createPacket(Protocol.STATUS_END, "Viewer closed connection"));
		in.close();
		out.close();
		viewer.close();
	}
	
	public static void main(String[] args) throws IOException {
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		BoardViewer viewer = new BoardViewer(host, port);
		viewer.start();
		viewer.close();
	}
}
