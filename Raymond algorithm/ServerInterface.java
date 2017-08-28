import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;

/**
 * This is a server interface for the implementation of Ricart Agrawala.
 * 
 * @author Lokesh Agrawal
 *
 */
public interface ServerInterface extends Remote
{
	public void receiveToken(String hostName) throws RemoteException;
	public void receiveRequest(String hostName) throws RemoteException;
}
