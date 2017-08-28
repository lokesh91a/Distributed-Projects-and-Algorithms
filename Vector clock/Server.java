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
	public static class add
	{
		
	}
	public static void main(String args[])
	{
		try
		{
			Scanner scan = new Scanner(System.in);
			VectorClk fileServer = new VectorClk(Integer.parseInt(args[0]));
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.rebind("FileServer", fileServer);
			System.out.println("Server ready: Press Y to start");
			String input = scan.next();
			if(input.equalsIgnoreCase("Y"))
				fileServer.run();
			
		}
		catch (Exception e) {
			System.out.println("I am server class exception");
			e.printStackTrace();
		}				
	}
}
