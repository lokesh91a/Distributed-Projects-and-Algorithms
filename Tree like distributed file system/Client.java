import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Random;


public class Client extends UnicastRemoteObject implements ClientInterface
{
	private String fileName;
	private int port, maxServers = 0;
	private String[] serverTable;

	public Client(String args[]) throws RemoteException
	{
		this.fileName = args[0];
		this.port = Integer.parseInt(args[1]);
	}
	
	public void initServerTable()
	{
		serverTable = new String[]{"kansas.cs.rit.edu", "arizona.cs.rit.edu", 
				"gorgon.cs.rit.edu", "newyork.cs.rit.edu","yes.cs.rit.edu",
				"kinks.cs.rit.edu", "medusa.cs.rit.edu", "joplin.cs.rit.edu",
				"delaware.cs.rit.edu","buddy.cs.rit.edu", "glados.cs.rit.edu"};
		maxServers = serverTable.length;
	}
	
	public String findServerName(PacketDetails packet)
	{
		String fullName = this.fileName + packet.getLevel() + packet.getleaf();
		int hashValue = Math.abs(fullName.hashCode()%maxServers);
		return serverTable[hashValue];
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
	
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}
	
	public PacketDetails findLeafServer() throws RemoteException
	{
		Random leaf = new Random();
		return new PacketDetails(this.fileName,2,leaf.nextInt(4));
	}
	
	public void fileNotFoundMessage(String message)
	{
		System.out.println(message);
	}
	
	public void writeFileToClient(byte[] buffer, PacketDetails packet)
	{
		File file;
		try {
			file = new File(System.getProperty("user.home")+ "/Courses/CSCI652/"
		+ InetAddress.getLocalHost().getHostName()+"/"+packet.getFileName());
		 
		ArrayList<String> packetPast = packet.getPacketPast();
		System.out.println(packet.getFileName() + " received");
		System.out.println("Trail of this file request is");
		for(int i=0; i<packet.getPacketPast().size();i++)
		{
			System.out.println(packet.getPacketPast().get(i));
		}
		
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
	
	public static void main(String args[]) throws RemoteException, 
	MalformedURLException, NotBoundException
	{
		Client fileClient = new Client(args);
		fileClient.initServerTable();
		PacketDetails packet = fileClient.findLeafServer();
		String fileServerName = fileClient.findServerName(packet);
		System.out.println(fileServerName);
		String name = "rmi://" + fileServerName + ":" + fileClient.port + 
				"/FileServer";
		ServerInterface fileInterface = (ServerInterface)Naming.lookup(name);
		fileInterface.sendFileToClient(packet,fileClient);
	}
	
	
}
