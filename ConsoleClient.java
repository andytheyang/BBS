// Usage: java ConsoleClient [hostname] [port number]
import java.io.IOException;
import java.util.Scanner;


public class ConsoleClient extends BoardClient {

	Scanner stdin;
	
	public ConsoleClient(String host, int port) throws IOException {
		super(host, port);
		stdin = new Scanner(System.in);
		// TODO Auto-generated constructor stub
	}

	
	
	@Override
	protected void printMessage(String mes) {
		// TODO Auto-generated method stub
		System.out.println(mes);
	}

	@Override
	protected boolean isRegistering() {
		// TODO Auto-generated method stub
		String response = null;
		do {
			System.out.print("Would you like to register (y/n)? >");
			String temp = stdin.nextLine();
			if (temp.equals("y") || temp.equals("n")) {
				response = temp;
			}
		} while (response == null);
		return response.equals("y");
	}

	@Override
	protected String getUsername() {
		// TODO Auto-generated method stub
		System.out.print("Username >");
		
		
		return stdin.nextLine();
	}

	@Override
	protected String getPassword(boolean register) {
		// TODO Auto-generated method stub
		System.out.print("Password >");
//		return new String(System.console().readPassword());
		return stdin.nextLine();
	}
	
	// Usage: java ConsoleClient [hostname] [port number]
	public static void main(String[] args) throws IOException {
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		BoardClient client = new ConsoleClient(host, port);
		client.start();
		client.close();
	}



	@Override
	protected String getPost() {
		// TODO Auto-generated method stub
		System.out.print(">");
		return stdin.nextLine();
	}

}
