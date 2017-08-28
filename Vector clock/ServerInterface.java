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
	public void receiveMoney(int time1, int time2, int time3) throws RemoteException;	
}
