import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;


public class Raymond implements ServerInterface, Runnable
{
	private int count, portNo;
	private String holder, selfName, hostName, address, status, tHostName;
	private Queue<String> requestQueue;
	private Object receiveRequest, receiveToken;
	private String[] serverList;
	private ServerInterface fileInterface;
	
	public Raymond(int portNo) throws RemoteException
	{
		this.requestQueue = new LinkedList<String>();
		this.receiveRequest = new Object();
		this.receiveToken = new Object();
		this.serverList = new String[7];
		this.initServerList();
		this.portNo = portNo;
		this.status = "free";
		this.holder = "glados";
	}
	
	private void initServerList()
	{
		this.serverList[0] = "glados";
		this.serverList[1] = "delaware";
		this.serverList[2] = "yes";
		this.serverList[3] = "doors";
		this.serverList[4] = "newyork";
		this.serverList[5] = "buddy";
		this.serverList[6] = "arizona";
	}
	
	private void enterCS()
	{
		try
		{
			if(holder.equals(this.selfName))
			{
				this.status = "using";
				Thread.currentThread().sleep(10000);
				this.status = "free";
			}
			else
			{
				if(this.requestQueue.contains(this.selfName)
						this.requestQueue.add(this.selfName);
				this.address = "rmi://" + this.holder + ".cs.rit.edu" + ":"
						+ this.portNo + "/FileServer";
				fileInterface = (ServerInterface) Naming.lookup(address);
				fileInterface.receiveRequest(this.selfName);
			}
		}
		catch(Exception e)
		{
			System.out.println("I am in enterCS exception");
			e.printStackTrace();
		}
	}
	
	public void receiveRequest(String hostName)
	{
		System.out.println("Recieved request for token from: " + hostName);
		synchronized (this.receiveRequest)
		{
			this.hostName = hostName;
			this.receiveRequest.notify();	
		}
	}
	
	public void receiveToken(String hostNme)
	{
		synchronized (this.receiveToken)
		{
			System.out.println("Token received from " + hostNme);
			this.tHostName = hostNme;
			this.receiveToken.notify();
		}		
	}

	public void startRM() throws InterruptedException
	{
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter any key to start program");
		scan.next();
		Random rand = new Random();
		while(true)
		{
			Thread.currentThread().sleep(2000);
			this.enterCS();
		}
	}
	
	public void run()
	{
		try
		{
			while(true)
			{
				synchronized (this.receiveRequest)
				{
					if(Thread.currentThread().getName().equals("receiveRequest"))
					{
						this.receiveRequest.wait();
						try
						{
							if(!this.requestQueue.contains(this.hostName))
								this.requestQueue.add(this.hostName);
								if(this.holder.equals(this.selfName) && status.equals("free"))
								{
									String dequeue = this.requestQueue.poll();
									this.holder = dequeue;
									this.address = "rmi://" + dequeue + ".cs.rit.edu" + ":"
											+ this.portNo + "/FileServer";
									this.fileInterface = (ServerInterface) Naming.lookup(address);
									this.fileInterface.receiveToken(this.selfName);	
								}
								else
								{
									this.address = "rmi://" + this.holder + ".cs.rit.edu" + ":"
											+ this.portNo + "/FileServer";
									this.fileInterface = (ServerInterface) Naming.lookup(address);
									this.fileInterface.receiveRequest(this.selfName);
								}
							
						}
						catch(Exception e)
						{
							System.out.println("I am in receiveRequest Exception");
							e.printStackTrace();
						}
					}
				}
				
				synchronized (this.receiveToken)
				{
					if(Thread.currentThread().getName().equals("receiveToken"))
					{
						this.receiveToken.wait();
						try
						{
							this.holder = this.selfName;
							if(this.tHostName.equals(this.selfName))
							{
								this.enterCS();
							}
							else
							{
								if(!this.requestQueue.isEmpty())
								{
									String dequeued = this.requestQueue.poll();
									this.holder = dequeued;
									this.address = "rmi://" + this.holder + ".cs.rit.edu" + ":"
												+ this.portNo + "/FileServer";
									fileInterface = (ServerInterface) Naming.lookup(address);
									fileInterface.receiveToken(this.selfName);	
								}
							}
							
						}
						catch(Exception e)
						{
							System.out.println("I am in receiveToken exception");
							e.printStackTrace();
						}
					}
				}
				
			}

		}
		catch(Exception e)
		{
			System.out.println("I am in run exception");
			e.printStackTrace();
		}
	}
}
