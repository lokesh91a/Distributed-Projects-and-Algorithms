import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;


public class ServerImpl extends UnicastRemoteObject implements ServerInterface
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String[] serverTable;
	private int port = 2016, maxServers = 0;
	private HashMap<String, Integer> popularCount;
	static int requestCount = 0;
	
	public ServerImpl(int port) throws RemoteException
	{
		this.port = port;
		this.popularCount = new HashMap<String, Integer>();
	}
	
	public void initServerTable()
	{
		serverTable = new String[]{"kansas.cs.rit.edu", "arizona.cs.rit.edu", 
				"gorgon.cs.rit.edu", "newyork.cs.rit.edu","yes.cs.rit.edu",
				"kinks.cs.rit.edu", "medusa.cs.rit.edu", "joplin.cs.rit.edu",
				"delaware.cs.rit.edu", "buddy.cs.rit.edu","glados.cs.rit.edu"};
		maxServers = serverTable.length;
	}
	
	public String findServerName(String fileName, int level, int leaf)
	{
		String fullName = fileName + level + leaf;
		int hashValue = Math.abs(fullName.hashCode()%maxServers);
		return serverTable[hashValue];
	}
	/*
	public boolean ifRootNode(String fileName, int level, int depth) throws
	UnknownHostException
	{
		String rootServerName = findServerName(fileName, 0, 0);
		String presentServerName = InetAddress.getLocalHost().getHostName();
		System.out.println("Root: "+rootServerName+ "PresentServer "+ presentServerName);
		if(rootServerName.substring(0, rootServerName.indexOf('.')).equals(presentServerName))
			return true;
		return false;
					
	}
	*/
	public void updateRequestCountTable(PacketDetails packet)
	{
		String key = packet.getPacketPast().get(packet.getPacketPast().size()-2) + packet.getFileName();
		if(popularCount.containsKey(key))
			popularCount.put(key, popularCount.get(key)+1);
		else
			popularCount.put(key,1);
	}
	
	public int getPopularCount(PacketDetails packet)
	{
		return popularCount.get(packet.getPacketPast().get(packet.getPacketPast().size()-2) + packet.getFileName());
	}
		
	public void prepareToSendFiletoChild(byte[] buffer, PacketDetails packet) throws RemoteException, NotBoundException, MalformedURLException
	{
		String leftChild = findServerName(packet.getFileName(), packet.getLevel()+1, packet.getleaf()*2);
		String rightChild = findServerName(packet.getFileName(), packet.getLevel()+1, (packet.getleaf()*2)+1);
		System.out.println("Replicating" + packet.getFileName() + "to: " + leftChild + " and " + rightChild);
		String leftChildBindName = "rmi://" + leftChild + ":" + port + "/FileServer";
		String rightChildBindName = "rmi://" + rightChild + ":" + port + "/FileServer";
		ServerInterface fileInterface1 = (ServerInterface)Naming.lookup(leftChildBindName);
		fileInterface1.sendFileToChilds(buffer, packet, leftChild);
		ServerInterface fileInterface2 = (ServerInterface)Naming.lookup(rightChildBindName);
		fileInterface2.sendFileToChilds(buffer, packet, rightChild);
	}
	
	public void sendFileToChilds(byte[] buffer, PacketDetails packet, String child)
	{
		File file;
		try {
			file = new File(System.getProperty("user.home")+ "/Courses/CSCI652/"
		+ child+"/"+packet.getFileName());
		 
		ArrayList<String> packetPast = packet.getPacketPast();
		System.out.println(packet.getFileName() + " received");
		
		BufferedOutputStream bufferStream;
		
			bufferStream = new BufferedOutputStream(new FileOutputStream(file.
					getName()));
			bufferStream.write(buffer,0, buffer.length);
			bufferStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void findParent(PacketDetails packet)
	{
		packet.setLevel(packet.getLevel()-1);
		packet.setleaf(packet.getleaf()/2);
	}
	
	public void sendFileToClient(PacketDetails packet, ClientInterface fileClient)
	{
		try
		{
			System.out.println("Request received from client");
			String hostName = InetAddress.getLocalHost().getHostName();
			packet.addPacketPast(hostName);
//			updateRequestcountTable(packet);
			String address = System.getProperty("user.home");
			String fileName = address +"/Courses/CSCI652/" + hostName + "/" + packet.getFileName();
			File file = new File(fileName);
			if(!file.exists())
			{
				if(packet.getLevel()==0)
				{
					fileClient.fileNotFoundMessage("File is not present on any Server");
					for(int i=0;i<packet.getPacketPast().size();i++)
						System.out.println(packet.getPacketPast().get(i));
				}
				else
				{
					findParent(packet);
					String fileServerName = findServerName(packet.getFileName(), packet.getLevel(), packet.getleaf());
					System.out.println("Request sent to my parent: " + fileServerName);
					String name = "rmi://" + fileServerName + ":" + port + "/FileServer";
					ServerInterface fileInterface = (ServerInterface) Naming.lookup(name);
					fileInterface.sendFileToClient(packet, fileClient);
				}
			}
			
			else
			{
				requestCount++;
				updateRequestCountTable(packet);
				byte[] buffer = new byte[(int)file.length()];
				BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName));
				inputStream.read(buffer, 0, buffer.length);
				if(getPopularCount(packet)==5||requestCount==5)
					prepareToSendFiletoChild(buffer, packet);
				inputStream.close();
				fileClient.writeFileToClient(buffer, packet);
			}

		}
		
		catch(Exception e)
		{	e.printStackTrace();
			System.out.println("Exveption occured");
		}
				
		
	}

}
