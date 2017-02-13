

import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Tracker extends UnicastRemoteObject implements TrackerInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String portNo;
	private String selfName;
	HashMap<String, File> fileRecord = new HashMap<String, File>();

	public Tracker(String portNo) throws RemoteException {
		super();
		try {
			this.selfName = Inet4Address.getLocalHost().getHostName();
			this.portNo = portNo;
		} catch (Exception e) {
			System.out.println("I am in Tracker constructor exception");
		}

	}

	public synchronized int addPeer(String IPaddress, String portNumber,
			String fileName, byte[] peciesAvailabel) throws RemoteException {
		//System.out.println("Request received from " + IPaddress);
		Peer requester = new Peer(IPaddress, portNumber, peciesAvailabel);
		if (fileRecord.containsKey(fileName)) {
			//System.out.println(requester.getPeerId());
			
			fileRecord.get(fileName).getPeerRecord().put(requester.getPeerId(), requester);
			//System.out.println(fileRecord.get(fileName).getPeerRecord().get(requester.getPeerId()).getIPaddress());
			show();
			return requester.getPeerId();
		} else
			return -1;
	}
	
	
	public HashMap<Integer, Peer> getPeers(int peerID, String fileName)
			throws RemoteException {
		if (fileRecord.get(fileName).getPeerRecord().containsKey(peerID)
				&& fileRecord.containsKey(fileName))
			return fileRecord.get(fileName).getPeers();
		else
			return null;

	}

	/**
	 * We can maintain a que for the purpose of resolving
	 */
	public String updatePeerStatus(String fileName, int peerID, byte[] pieceInfo)
			throws RemoteException {
		if (fileRecord.containsValue(fileName)) {

			Peer aPeer = fileRecord.get(fileName).getPeerRecord().get(peerID);
			if (aPeer != null) {
				aPeer.setPieceInfo(pieceInfo);
				fileRecord.get(fileName).getPeerRecord().put(peerID, aPeer);
				return "Successfully Details updated";
			} else {
				return "You are not a registered peer for given file";
			}
		} else {
			return "No Details found for given file";
		}
	}

	public int publish(String fileName, String IPaddress, String PortNumber,
			byte[] byteInfo, int peiceLength, int numberOfPecies, int fileSize)
			throws RemoteException {
		//System.out.println("Publish request from " + IPaddress);
		//System.out.println("PortNO: "+PortNumber+"\n Piecelength: "+ peiceLength);
		if(!fileRecord.containsKey(fileName)){
			File newFile = new File(fileName, numberOfPecies, peiceLength);
			fileRecord.put(fileName, newFile);}
		
		return addPeer(IPaddress, PortNumber, fileName, byteInfo);

	}
	
	public void show(){
		System.out.println("Iam in show");
		System.out.println(fileRecord.size());
		for(String file: this.fileRecord.keySet())
			System.out.println(fileRecord.get(file));
		
	}
	/*
	 * public ClientInterfaceForTracker getRemoteClient(String hostName, String
	 * RMIport) throws RemoteException{
	 * 
	 * 
	 * String registryURL = "rmi://" + hostName + ":" + RMIport + "/server"; try
	 * { ClientInterfaceForTracker aClient = (ClientInterfaceForTracker) Naming
	 * .lookup(registryURL); return aClient; } catch (MalformedURLException e) {
	 * e.printStackTrace(); } catch (NotBoundException e) { e.printStackTrace();
	 * } return null; }
	 */
}
