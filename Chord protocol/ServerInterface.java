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
	public void addServer(String serverName) throws RemoteException;
	public void removeServer(String serverName) throws RemoteException;
	public void setSuccessor(String hostName) throws RemoteException;
	public void setPredecessor(String hostName) throws RemoteException;
	public void setMax(int idSpace) throws RemoteException;
	public void setMin(int idSpace) throws RemoteException;
	public String getSuccessor() throws RemoteException;
	public String getPredecessor() throws RemoteException;
	public int getMax() throws RemoteException;
	public int getMin() throws RemoteException;
	public void view(String serverName) throws RemoteException;
	public void insertFile(String serverName) throws RemoteException;
	public ArrayList<String> searchFile(ArrayList<String> requestTrail) throws RemoteException;
	public byte[] readFile(String fileName) throws RemoteException;
	public void getFile(String fileName) throws RemoteException;
	public void writeFile(byte[] buffer, String fileName) throws RemoteException;
}
