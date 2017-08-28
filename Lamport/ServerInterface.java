import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This is a server interface for the implementation of chord.
 * 
 * @author Lokesh Agrawal
 *
 */
public interface ServerInterface extends Remote
{
	public void receiveMarker(String selfName) throws RemoteException;	
	public void receiveMoney(String selfName, int amount) throws RemoteException;
}
