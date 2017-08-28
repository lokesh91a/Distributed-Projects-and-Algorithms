import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class Server
{
	public static void main(String args[])
	{
		try {
			ServerImpl fileServer = new ServerImpl(Integer.parseInt(args[0]));
			fileServer.initServerTable();
			Registry registry = LocateRegistry.createRegistry(Integer.parseInt(args[0]));
			registry.rebind("FileServer", fileServer);
			System.out.println("Server id ready");
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
