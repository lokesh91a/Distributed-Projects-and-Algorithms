import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ServerInterface extends Remote
{
	public void sendFileToClient(PacketDetails packet, ClientInterface client) throws RemoteException;
	public void sendFileToChilds(byte[] data, PacketDetails Packet, String child) throws RemoteException;
}
