

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * This interface will be used at Client side for creating remote reference of Tracker
 * Create a remote reference of this interface, get Remote object of tracker 
 * then call method add peer for get registered.
 * it will return a peer ID, keep it with you
 * then call get Peers to get all the peers 
 * @author deepak
 *
 */
public interface TrackerInterface extends Remote {
	//hashTable getPeers
	//statusUpdate 
	//
	/**
	 * Call the method to get registered as a peer for a particular file if partial file available
	 * @param IPaddress give your IP address or HostName
	 * @param portNumber GIve your RMI port name
	 * @param fileName Give the file name your are interested
	 * @param peciesAvailabel And if you have partial file give details of species 
	 * @return
	 * @throws RemoteException
	 */
	public int addPeer(String IPaddress, String portNumber,String fileName, byte[] peciesAvailabel) throws RemoteException;
		
	/**
	 * Call this method Once you register, and get all  details for your peers
	 * call this method without registering will return null
	 * so make sure you, register you self using addPeer be
	 * @param fileName
	 * @return
	 * @throws RemoteException
	 */
	public HashMap<Integer,Peer> getPeers(int peerID,String fileName) throws RemoteException;
	/**
	 * For updating current status, call this method 
	 * @param fileName
	 * @param peerID
	 * @param pieceInfo
	 * @return action status on update request
	 * 
	 * 
	 * 
	 * @throws RemoteException
	 */
	public String updatePeerStatus(String fileName,int peerID, byte[] pieceInfo)
			throws RemoteException;
	
	public int publish(String fileName, String IPaddress, String PortNumber,
			byte[] byteInfo, int peiceLength, int numberOfPecies, int fileSize)
			throws RemoteException;
}
