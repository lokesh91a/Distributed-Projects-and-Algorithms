import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;


public class PacketDetails implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String fName;
	private int i,j;
	private ArrayList<String> packetPast;


	public PacketDetails(String fName, int i, int j) throws RemoteException
	{
		this.fName = fName;
		this.i = i;
		this.j = j;
		this.packetPast= new ArrayList<String>();
	}
	
	public int getLevel()
	{
		return i;
	}
	
	public void setLevel(int level)
	{
		this.i = level;
	}
	
	public int getleaf()
	{
		return j;
	}
	
	public void setleaf(int leaf)
	{
		this.j = leaf;
	}
	
	public String getFileName()
	{
		return fName;
	}
	
	public void setFileName(String name)
	{
		this.fName = name;
	}
	
	public ArrayList<String> getPacketPast()
	{
		return packetPast;
	}
	
	public void addPacketPast(String hostName)
	{
		if (packetPast==null)
			System.out.println("THis is null");
		packetPast.add(hostName);
	}

}