import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ClientInterface extends Remote
{
	public void fileNotFoundMessage(String message) throws RemoteException;
	public void writeFileToClient(byte buffer[], PacketDetails packet) throws RemoteException;

}
