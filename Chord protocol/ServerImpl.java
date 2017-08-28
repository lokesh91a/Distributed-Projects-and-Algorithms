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

import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;

/**
 * This is a Server implementation program which implements server interface
 * and implements all its functions alongwith some helper functions.
 * 
 * @author Lokesh Agrawal
 *
 */
public class ServerImpl extends UnicastRemoteObject implements ServerInterface
{
	private static final long serialVersionUID = 1L;
	private int port = 0, maxServers = 99, min = 0, max = 0;
    private String successor="", predecessor="";
    /**
     * This is a parameterized constructor
     * @param port : It is the port number used to make a connection.
     * @throws RemoteException
     */
    public ServerImpl(int port) throws RemoteException
	{
		this.port = port;	
	}   
    
    /**
     * This functions returns the starting point of the range.
     */
    public int getMin()
    {
    	return this.min;
    }
    
    
    /**
     * This functions is used to set the starting point of range of idSpace
     * @param int starting point of range
     * @return None
     */
    public void setMin(int min)
    {
    	this.min = min;
    }
    
    
    /**
     * This functions is used to get the ending point of range of idSpace
     * @param None
     * @return Ending point of range of idSpcae
     */
    public int getMax()
    {
    	return this.max;
    }
    
    
    /**
     * This functions is used to set the ending point of range of idSpace
     * @param int Ending point of range
     * @return None
     */
    public void setMax(int max)
    {
    	this.max = max;
    }
    
    
    /**
     * This functions is used to get the successor of current node.
     * @param None
     * @return String successor
     */
    public String getSuccessor()
    {
    	return this.successor;
    }
    
    
    /**
     * This functions is used to set the successor of the current node
     * @param String successor
     * @return None
     */
    public void setSuccessor(String successor)
    {
    	this.successor = successor;
    }
    
    
    /**
     * This functions is used to get the predecessor of current node.
     * @param None
     * @return String predecessor
     */
    public String getPredecessor()
    {
    	return this.predecessor;
    }
    
    
    /**
     * This functions is used to set the predecessor of the current node
     * @param String predecessor
     * @return None
     */
    public void setPredecessor(String predecessor)
    {
    	this.predecessor = predecessor;
    }
	
	
	
    /**
     * This function returns the idspace or the server name or the file name
     * @param String serverName or fileName
     * @return int its idSpace
     */
    public int idSpace(String serverName)
	{
		int idSpace= Math.abs(serverName.hashCode()%maxServers);
		if(serverName.equals("glados.cs.rit.edu"))
			return -1;
		else
			return idSpace;
	}
	
	
    /**
     * This function is used to search for the file with a specific name in 
     * the System and returns an list in which details of all request is present.
     * @param ArrayList<String> An array list is passed in which filename is
     * 							present at index 0.
     * @return ArrayList<String> An array list which contains all the nodes to
     * 							 which file search request has gone 
     */
    public ArrayList<String> searchFile(ArrayList<String> list)
	{
		try
		{
			String hostName = InetAddress.getLocalHost().getHostName()+".cs.rit.edu";
			ArrayList<String> requestTrail = list;
			//Hostname of the server on which request has come is added first to 
			//the list
			requestTrail.add(hostName);
			//Id Space of filename is generated
			int id = idSpace(requestTrail.get(0));
			System.out.println("idSpace of file is: "+id);
			
			//If it falls in the range of this servers idSpace then true or false
			//is appended if file is present or not and that list is returned.
			if(id<=this.max && id>=this.min)
			{
				if(chkForFile(requestTrail.get(0)))
				{
					requestTrail.add("true");
					return requestTrail;
				}
					
				else
				{
					requestTrail.add("false");
					return requestTrail;
				}
			}
			
			//This is to check whether the request should be sent to predecessor
			//or successor on the basis of which is present closer to it.
			else 
			{
				String serverBindName = "rmi://" + this.getSuccessor() + ":" + port + "/FileServer";
				ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
				//Search file is sent to the successor
				return fileInterface.searchFile(requestTrail);
			}
			
			/*else if(id>(((this.max+this.min)/2)+50))
			{
				String serverBindName = "rmi://" + this.getPredecessor() + ":" + port + "/FileServer";
				ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
				return fileInterface.searchFile(requestTrail);
			}*/
			
		}
		catch(Exception e)
		{
			System.out.println("I am in searchFile Exception");
			e.printStackTrace();
		}
		return list;
	
	}
	
	
    /**
     * This functions checks whether file is present on this server or not.
     * @param fileName name of file
     * @return true if file is present
     * 		   false if file is not present
     * @throws UnknownHostException
     */
    public boolean chkForFile(String fileName) throws UnknownHostException
	{
		String hostName = InetAddress.getLocalHost().getHostName();
		String address = System.getProperty("user.home");
		String fName = address +"/Courses/CSCI652/project1/" + hostName + "/" + fileName;
		File file = new File(fName);
		if(file.exists())
			return true;
		else
			return false;
	}
	
    public void writeFile(byte[] buffer, String fileName)
    {
    	try
    	{
    		String hostName = InetAddress.getLocalHost().getHostName();
			String address = System.getProperty("user.home");
			String fName = address +"/Courses/CSCI652/project1/"+hostName+"/"+fileName;
			File file = new File(fName);
    		BufferedOutputStream bufferStream = new BufferedOutputStream(new 
					FileOutputStream(fName));		
			bufferStream.write(buffer, 0, buffer.length);
			bufferStream.flush();
			bufferStream.close();
    	}
    	catch(Exception e)
    	{
    		System.out.println("Exception");
    		e.printStackTrace();
    	}
    }
    
    public void insertFile(String fileName)
    {
    	try
    	{
			ArrayList<String> list = new ArrayList<String>();
			list.add(fileName);
			list = searchFile(list);
			String serverBindName = "rmi://"+list.get(list.size()-2)+":"+port+"/FileServer";
			ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
			fileInterface.writeFile(this.readFile(fileName), fileName);
    	}
    	catch(Exception e)
    	{
    		System.out.println("I am in insertFile exdeption");
    		e.printStackTrace();
    	}
    }
	
    /**
     * This functions reads the data of the file with file name and returns
     * the data.
     * @param fileName name of file
     * @return byte[] byte array which has the data of file
     */
    public byte[] readFile(String fileName)
	{
		try
		{
			String hostName = InetAddress.getLocalHost().getHostName();
			String address = System.getProperty("user.home");
			String fName = address +"/Courses/CSCI652/project1/"+hostName+"/"+fileName;
			File file = new File(fName);
			if(file.exists())
			{
			byte buffer[] = new byte[(int)file.length()];
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(fName));
			input.read(buffer,0,buffer.length);
			input.close();
			return buffer;
			}
			else
			{
				System.out.println("Failure");
			}
		}
		catch(Exception e)
		{
			System.out.println("I am in downloadFile exception");
			e.printStackTrace();
		}
		return null;
	}
	
	
    /**
     * This function copies the file with fileName on this server if it is 
     * present anywhere on the system otherwise prints failure message.
     * @param fileName name of file
     * @return None
     */
    public void getFile(String fileName)
	{
		try
		{
			String hostName = InetAddress.getLocalHost().getHostName();
			String address = System.getProperty("user.home");
			String fName = address+"/Courses/CSCI652/project1/"+hostName+"/"+fileName;
			File file = new File(fName);
			ArrayList<String> list = new ArrayList<String>();
			list.add(fileName);
			list = searchFile(list);
			if(list.get(list.size()-1).equals("true"))
			{
				String serverBindName = "rmi://"+list.get(list.size()-2)+":"+port+"/FileServer";
				ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
				byte[] buffer = fileInterface.readFile(fileName);
				BufferedOutputStream bufferStream = new BufferedOutputStream(new 
						FileOutputStream(fName));		
				bufferStream.write(buffer, 0, buffer.length);
				bufferStream.flush();
				bufferStream.close();
			}
			else
				System.out.println("Failure. File is not present");
		}
		catch(Exception e)
		{
			System.out.println("I am in getFile exception");
			e.printStackTrace();
		}
	}
    
    /**
     * This function prints the details of the server
     * @param serverName name of server
     */
	
	public void view(String serverName)
	{
		String output = ("\n"+serverName + " details:" + "\nPredecessor: "+ this.getPredecessor() 
						+ "\nSuccessor: " + this.getSuccessor() + "\nRange: " + this.getMin() 
						+ " - " + this.getMax()+"\n");
		System.out.println(output);
	}
	
	
	/**
	 * This is a helper function for remove function. That is whenever a server
	 * is removed then that server calls this method which copies all its files
	 * to its predecessor before leaving.
	 * 
	 * @param fileInterface Object of serverinterface
	 * @return None
	 */
	public void giveToPredecessor(ServerInterface fileInterface)
	{
		try
		{
			String hostName = InetAddress.getLocalHost().getHostName();
			String address = System.getProperty("user.home");
			String fName = address+"/Courses/CSCI652/project1/"+hostName;
			File file = new File(fName);
			//to get List of all files present on this server
			File[] listOfFiles = file.listFiles();
			ArrayList<String> fileNames = new ArrayList<String>();
			for(int i=0;i<listOfFiles.length;i++)
			{
				if(listOfFiles[i].isFile())
					fileNames.add(listOfFiles[i].getName());
			}
			for(int i = 0;i<fileNames.size();i++)
				fileInterface.getFile(fileNames.get(i));
		}
		catch(Exception e)
		{
			System.out.println("I am in giveToSuccessor exception");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This function removes this server from the system by updating all its
	 * predecessor and successor details.
	 * @param serverName name of Server
	 * @return None
	 */
	public void removeServer(String serverName) throws RemoteException
	{
		try
		{
			String serverBindName = "rmi://"+this.getPredecessor()+":"+port+"/FileServer";
			ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
			this.giveToPredecessor(fileInterface);
			fileInterface.setSuccessor(this.getSuccessor());
			fileInterface.setMax(this.getMax());
			serverBindName = "rmi://"+this.getSuccessor()+":"+port+"/FileServer";
			fileInterface = (ServerInterface)Naming.lookup(serverBindName);
			fileInterface.setPredecessor(this.getPredecessor());
			this.setSuccessor(null);
			this.setPredecessor(null);
			this.setMax(-1);
			this.setMin(-1);
		}
		catch(Exception e)
		{
			System.out.println("I am in remove server exception");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This function adds this server to the system.
	 * 
	 * @param serverName name of server
	 * @return None
	 */
	public void addServer(String serverName) throws RemoteException
	{
		try
		{
			int idSpace = idSpace(serverName);
			System.out.println("idspace of " + serverName +" is: " + idSpace);
			if(serverName.equals("glados.cs.rit.edu"))
			{
				setMin(0);
				setMax(maxServers);
				setSuccessor(null);
				setPredecessor(null);
			}
			
			//If idspace is this servers range
			else if(idSpace<=this.max && idSpace>=this.min)
			{
				if(this.successor==null)
				{
					this.successor = serverName;
					this.predecessor = serverName;
					String hostName = InetAddress.getLocalHost().getHostName()+".cs.rit.edu";
					String serverBindName = "rmi://" + serverName + ":" + port + "/FileServer";
					ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
					fileInterface.setSuccessor(hostName);
					fileInterface.setPredecessor(hostName);
					fileInterface.setMax(this.max);
					fileInterface.setMin(idSpace);
					this.setMax(idSpace-1);
				}
				
				else
				{
					String hostName = InetAddress.getLocalHost().getHostName()+".cs.rit.edu";
					String serverBindName = "rmi://" + serverName + ":" + port + "/FileServer";
					ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
					fileInterface.setSuccessor(this.successor);
					fileInterface.setPredecessor(hostName);
					fileInterface.setMax(this.max);
					fileInterface.setMin(idSpace);
					serverBindName = "rmi://" + this.successor + ":" + port + "/FileServer";
					fileInterface = (ServerInterface)Naming.lookup(serverBindName);
					fileInterface.setPredecessor(serverName);
					this.setSuccessor(serverName);
					this.setMax(idSpace-1);
				}
			}
			
			//if idspace is not in its range then this request is forwarded to its 
			//predecessor or successor based on which is closer to it.
			else if(idSpace<=(((this.max+this.min)/2)+50) && this.successor!=null)
			{
				String serverBindName = "rmi://" + this.successor + ":" + port + "/FileServer";
				ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
				fileInterface.addServer(serverName);
			}
			else if(idSpace>(((this.max+this.min)/2)+50) && this.successor!=null)
			{
				String serverBindName = "rmi://" + this.predecessor + ":" + port + "/FileServer";
				ServerInterface fileInterface = (ServerInterface)Naming.lookup(serverBindName);
				fileInterface.addServer(serverName);
			}
			
		}
		catch(Exception e)
		{
			System.out.println("I am in ServverImpl Exception");
			e.printStackTrace();
		}
	}
}
