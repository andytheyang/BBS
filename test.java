import java.net.InetAddress;
import java.net.UnknownHostException;


public class test {
	public static void main(String[] args) throws UnknownHostException {
//		Console console = System.console();
//		System.out.print("Enter password >");
//		String password = new String(console.readPassword());
//		System.out.println("just kidding: " + password);
//		System.out.println("Hash: " + password.hashCode());
		InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();
        System.out.println(addr.getHostAddress());
        System.out.println(hostname);

	}
}
