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
			//It takes the port no. as a run time argument
			ServerImpl fileServer = new ServerImpl(Integer.parseInt(args[0]));
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.rebind("FileServer", fileServer);
			Scanner scan = new Scanner(System.in);
			String hostName = Inet4Address.getLocalHost().getHostName()+".cs.rit.edu";
			String serverBindName = "rmi://" + "glados.cs.rit.edu" + ":" + args[0] + "/FileServer";
			ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
			
			//This loop runs forever which gives for 5 options
			while(true)
			{
				System.out.println("\nYou have following options: \n" + "1.Add this server.\n"
			+ "2.Remove this server.\n" + "3.View details of this server.\n" + 
						"4.Insert file\n" + "5.Search for file\n");
			System.out.println("Enter the option number: ");
			//Enter the option number
			int option = scan.nextInt();
			switch(option)
				{
					case 1: fileInterface.addServer(hostName);
							break;
					case 2: fileServer.removeServer(hostName);
							break;
					case 3: fileServer.view(hostName);
							break;
					case 4: System.out.println("Enter file file name: ");
							fileServer.insertFile(scan.next());
							break;
					case 5: System.out.println("Enter the file name: ");
							ArrayList<String> list = new ArrayList<String>();
							list.add(scan.next());
							list = fileServer.searchFile(list);
							if(list.get(list.size()-1).equals("true"))
								System.out.println("File Found");
							else
								System.out.println("File not found");
							System.out.println("Request trail is: ");
							for(int i=1;i<list.size()-1;i++)
							{
								System.out.println(list.get(i));
							}
              break;
					default:System.out.println("Something is wrong"); 			
				}
			}
		}
		catch (Exception e) {
			System.out.println("I am server class exception");
			e.printStackTrace();
		}				
	}
}
