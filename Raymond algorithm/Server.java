import java.net.Inet4Address;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This is the server implmentation of Ricart Agrawala.
 * 
 * @author Lokesh Agrawal
 *
 */
public class Server
{
	/**
	 * This program takes 2 inputs
	 * @param args[0] port no.
	 * 		  args[1] process ID
	 */
	public static void main(String args[])
	{
		try
		{
			//It takes the port no. as a run time argument
			Raymond RM = new Raymond(Integer.parseInt(args[0]));
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.rebind("FileServer", RM);
			Thread t1 = new Thread(RM, "receiveRequest");
			t1.start();
			Thread t2 = new Thread(RM, "receiveToken");
			t2.start();
			RM.startRM();
		}
		catch (Exception e) {
			System.out.println("I am server class exception");
			e.printStackTrace();
		}				
	}
}
