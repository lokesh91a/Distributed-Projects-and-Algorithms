import java.net.Inet4Address;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the server implmentation.
 * 
 * @author Lokesh Agrawal
 *
 */
public class Server
{
	public static void main(String args[])
	{
		try
		{
			Scanner scan = new Scanner(System.in);
			Lamport trans = new Lamport(Integer.parseInt(args[0]));
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.rebind("FileServer", trans);
		
				Thread t1 = new Thread(trans, "sendMoney");
				t1.start();
				Thread t2 = new Thread(trans, "receiveMoney");
				t2.start();
				Thread t3 = new Thread(trans, "sendMarker");
				t3.start();
				Thread t4 = new Thread(trans, "receiveMarker");
				t4.start();
				Thread t5 = new Thread(trans, "startSnapshot");
				t5.start();
		
		System.out.println("Server ready: Press Y to start");
		String input = scan.next();
		trans.start();
		}
		catch (Exception e) 
		{
			System.out.println("I am server class exception");
			e.printStackTrace();
		}
	}
}
