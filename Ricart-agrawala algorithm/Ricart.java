import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

import javax.swing.text.html.HTMLDocument.Iterator;

/**
 * This Implements the Ricart Agrawala mutual exclusion algorithm
 * This is the implementation program which implements server interface and
 * implements all its functions alongwith some helper functions.
 * 
 * @author Lokesh Agrawal
 * 
 */
public class Ricart extends UnicastRemoteObject implements ServerInterface,
		Runnable
{
	private int portNo, processId, totalRequests, inProcessId;
	private Map<String, Integer> vectorClk, criticalClk, incomingCriticalClk,incomingClk;
	private String[] serverList;
	//Queue for the requests while wanted status
	private Queue<String> requestQueue;
	private String selfName, globalStatus, hostName;
	//Locks for threads
	private Object receiveRequest, criticalSection;
	private Random rand;

	public Ricart(int portNo, int processId) throws RemoteException
	{
		try
		{
			//process id and port No is assigned to this server
			this.processId = processId;
			this.portNo = portNo;
			
			//hashmap to store the critical clock of the incoming request
			this.incomingCriticalClk = new HashMap<String, Integer>();
			//HashMap to store vector clock of incoming clock
			this.incomingClk = new HashMap<String, Integer>();
			//Vector clock of self
			this.vectorClk = new HashMap<String, Integer>();
			//vector clock of self when this wanted critical section
			this.criticalClk = new HashMap<String, Integer>();
			this.selfName = Inet4Address.getLocalHost().getHostName();
			this.requestQueue = new LinkedList<String>();
			this.receiveRequest = new Object();
			this.criticalSection = new Object();
			//To store the global status
			this.globalStatus = "released";
			this.rand = new Random();
			this.serverList = new String[4];
			//Initialize the serverList
			this.initServerList();
			//Initialize the vector clock of self
			this.initVectorClk();
		}
		catch(Exception e)
		{
			System.out.println(" I am in constructor initialization exception");
			e.printStackTrace();
		}
		
	}

	/**
	 * ServerList is initialized
	 */
	private void initServerList()
	{
		this.serverList[0] = "glados";
		this.serverList[1] = "doors";
		this.serverList[2] = "yes";
		this.serverList[3] = "delaware";
	}

	/**
	 * Vector clock is initialized
	 */
	private void initVectorClk()
	{
		for (int i = 0; i < this.serverList.length; i++)
		{
			this.vectorClk.put(this.serverList[i], 0);
		}
		this.vectorClk.put(this.selfName, 1);
	}

	/**
	 * This method sends request to all servers in serverList except itself 
	 * for the token after coming to the wanted Status
	 */
	private void sendRequest()
	{
		try
		{
			//This event first increases its own clock by 1
			this.vectorClk.put(this.selfName, this.vectorClk.get(this.selfName) + 1);
			String address;
			ServerInterface fileInterface;
			//Takes object of each server 1 by 1 through RMI and call there receiveRequest function
			for (int i = 0; i < this.serverList.length; i++)
			{
				if (!this.serverList[i].equals(this.selfName))
				{
					System.out.println("Sending request for token to: "
							+ this.serverList[i]);
					address = "rmi://" + this.serverList[i] + ".cs.rit.edu" + ":"
							+ this.portNo + "/FileServer";
					fileInterface = (ServerInterface) Naming.lookup(address);
					fileInterface.receiveRequest(this.criticalClk, this.vectorClk,
							this.processId, this.selfName);
				}
			}
		}
		catch(Exception e)
		{
			System.out.println("I am in sendRequest Exception");
			e.printStackTrace();
		}
	}

	/**
	 * This is receive Token function which is called by other functions to deliver
	 * the token Requested
	 * 
	 * @param hostNme Name of the server from which token received
	 * 
	 */
	public void incrementRequestCount(String hstNme)
	{
		synchronized (this.criticalSection)
		{
			try 
			{
				System.out.println("Received token from " + hstNme);
				this.totalRequests++;
				//When it receives all requests then it goes to the critical section
				//by notifying the thread waiting on a lock
				if (totalRequests == 3)
					this.criticalSection.notify();
			}
			catch (Exception e)
			{
				System.out.println("I am in incrementRequestCount exception");
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method is for receiving the request for the lock. It is called by the 
	 * other servers when they send request for the lock 
	 */
	public void receiveRequest(Map<String, Integer> incomingCriticalClk,
			Map<String, Integer> incomingClk, int inProcessId, String hstName)
	{
		try
		{
			synchronized (this.receiveRequest)
			{
				this.incomingCriticalClk.putAll(incomingCriticalClk);
				this.incomingClk.putAll(incomingClk);
				this.inProcessId = inProcessId;
				this.hostName = hstName;
				this.receiveRequest.notify();
			}
		}
		catch (Exception e)
		{
			System.out.println("in Exception receiveRequest");
			e.printStackTrace();
		}
	}

	/**
	 * This method increases its vector clock compared with the incoming vector clock
	 * as this is counted as a event
	 * @param incomingClk Vector clock of the server requested for Token
	 */
	private void increaseClk(Map<String, Integer> incomingClk)
	{
		for (Map.Entry<String, Integer> entry : incomingClk.entrySet())
		{
			if(entry.getKey().equals(this.selfName))
			{
				if (this.vectorClk.get(entry.getKey()) + 1 < entry.getValue())
					this.vectorClk.put(entry.getKey(), entry.getValue());
				else
					this.vectorClk.put(this.selfName,this.vectorClk.get(this.selfName) + 1);
			}
			else 
			{
				if (this.vectorClk.get(entry.getKey()) < entry.getValue())
					this.vectorClk.put(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * This method can be used at any time to print the contents of HashMap(Vector Clock)
	 * @param fuckme
	 */
	private void printHashMap(Map<String, Integer> incming)
	{
		for (Map.Entry<String, Integer> entry : incming.entrySet())
		{
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}

	/**
	 * This function compares 2 vector clock and decides which one is greater based
	 * on which it decides that who needs the critical resource first. 
	 * @param incomingCriticalClk Clock of requesting server at which it wanted the 
	 * 							  critical section
	 * @param incomingClk		Vector clock of requesting server when it sent the request
	 * @param inProcessId		Process id of requesting server
	 * @param hostName			Name of requesting server
	 * @return true: it itself needs the critical section first
	 * 		  false: It has no problem in giving the requesting server token
	 */
	private boolean compareClk(Map<String, Integer> incomingCriticalClk, Map<String, 
			Integer> incomingClk, int inProcessId, String hostName)
	{
		this.increaseClk(incomingClk);		
		if(globalStatus.equals("held") || (globalStatus.equals("wanted") && 
				compareClocks(incomingCriticalClk, inProcessId)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Two vector clocks are compared and boolean result if returned.
	 * If there is a tie then boolean result is returned based on their
	 * processId. Server with larger processId is given te preference 
	 */
	private boolean compareClocks(Map<String, Integer> incomingCriticalClk,
			int inProcessId)
	{
		int lCount = 0, sCount = 0;
		
		for (Map.Entry<String, Integer> entry : incomingCriticalClk.entrySet())
		{
			if(this.criticalClk.get(entry.getKey())>entry.getValue())
				lCount++;
			if(this.criticalClk.get(entry.getKey())<entry.getValue() )
				sCount++;
		}
		
		if(lCount != 0)
		{
			if(sCount == 0)
			{
				return false;
			}
			return processId < inProcessId? false : true;
		}
		
		if ( sCount != 0)
		{
			if (lCount == 0)
			{
				return true;
			}
			return processId < inProcessId? false : true;
		}
			
		return processId < inProcessId? false : true;
	}

	/**
	 * When a server comes out of the critical section then if there are any requests
	 * pending in the queue are fulfilled by sending them all a token
	 */
	private void sendRequestQueue()
	{
		try 
		{
			while (this.requestQueue.peek() != null) 
			{
				String hostName = this.requestQueue.poll();
				System.out.println("Sending token to " + hostName);
				String address = "rmi://" + hostName + ".cs.rit.edu" + ":"
						+ this.portNo + "/FileServer";
				ServerInterface fileInterface = (ServerInterface) Naming
						.lookup(address);
				fileInterface.incrementRequestCount(this.selfName);
			}
		} catch (Exception e) {
			System.out.println("I am in sendRequestQueue exception");
			e.printStackTrace();
		}
	}

	/**
	 * This function initiates the algorithm by taking any alphabet as an
	 * input.
	 * This also requests for the critical section after every random seconds(2000ms)
	 */
	public void startRA()
	{
		try
		{
			Scanner scan = new Scanner(System.in);
			System.out.println("Enter any alphabet to start");
			scan.next();
			int time;
			while(true)
			{
				time = this.rand.nextInt(2000);
				Thread.currentThread().sleep(time);
				if (this.globalStatus.equals("released"))
				{
					this.globalStatus = "wanted";
					System.out.println("\n"+"Staus of " + this.selfName + " is: "
							+ this.globalStatus+"\n");
					this.criticalClk.putAll(this.vectorClk);
					this.sendRequest();
				}
			}
		} 
		catch(Exception e)
		{
			System.out.println("I am in startRA exception");
			e.printStackTrace();
		}
	}

	/**
	 * There are 2 threads inside this run on a single object with different names
	 * 1. Whenever a request is received for the token then this thread is 
	 * notifying waiting on the lock receiveRequest
	 * 2.When a server receives all the tokens from the servers then this thread
	 * is notified to go in critical section by notifying the thread  waiting on 
	 * the lock criticalSection
	 */
	public void run()
	{
		while (true)
		{
			if (Thread.currentThread().getName().equals("receiveRequest"))
			{
				try
				{
					synchronized (this.receiveRequest)
					{
						this.receiveRequest.wait();
						String address;
						ServerInterface fileInterface;
						if (!this.compareClk(this.incomingCriticalClk,
								this.incomingClk, this.inProcessId,
								this.hostName))
						{
							address = "rmi://" + this.hostName + ".cs.rit.edu"
									+ ":" + this.portNo + "/FileServer";
							fileInterface = (ServerInterface) Naming
									.lookup(address);
							fileInterface.incrementRequestCount(this.selfName);
						}
						else
						{
							System.out.println(this.hostName
									+ " added to queue");
							this.requestQueue.add(this.hostName);
						}
						this.incomingClk.clear();
						this.incomingCriticalClk.clear();
					}
				}
				catch(Exception e)
				{
					System.out.println("I am in receive request exception");
					e.printStackTrace();
				}
			}

			else if(Thread.currentThread().getName().equals("criticalSection"))
			{
				try
				{
					synchronized(this.criticalSection)
					{
						this.criticalSection.wait();
						this.globalStatus = "held";
						System.out.println("\n"+"Staus of " + this.selfName
								+ " is: " + this.globalStatus+"\n");
						Thread.currentThread().sleep(10000);
						this.totalRequests = 0;
						this.criticalClk.clear();
						if (!this.requestQueue.isEmpty())
							sendRequestQueue();
						this.globalStatus = "released";
						System.out.println("\n"+"Staus of " + this.selfName
								+ " is: " + this.globalStatus+"\n");
					}
				}
				catch(Exception e)
				{
					System.out.println("I am in receive criticalexception");
					e.printStackTrace();
				}
			}
		}
	}
}
