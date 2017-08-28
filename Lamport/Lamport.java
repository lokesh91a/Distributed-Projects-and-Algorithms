import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;


public class Lamport extends UnicastRemoteObject implements ServerInterface, Runnable
{

	private int portNo, localAmount, sendAmount, receiveAmount, randomServer;
	private Random rand;
	private String selfName, firstReceivedMarkerHostName;
	private String[] serverList;
	private int localSnapshot;
	private volatile HashMap<String, Integer> linkSnap;
	private volatile HashSet<String> markerCount;
	public String sendMoney, receiveMoney, sendMarker, receiveMarker, localAmountLock, startSnapshot;
	private volatile boolean snapShotTaken = false, markerSentProgress = false;
	
	
	public Lamport(int portNo) throws RemoteException, UnknownHostException
	{
		this.linkSnap = new HashMap<String, Integer>();
		this.selfName = InetAddress.getLocalHost().getHostName();
		this.portNo = portNo;
		this.rand = new Random();
		this.firstReceivedMarkerHostName = "None";
		this.localAmount = 1000;
		this.markerCount = new HashSet<String>();
		this.sendMoney = new String();
		this.receiveMoney = new String();
		this.sendMarker = new String();
		this.startSnapshot = new String();
		this.receiveMarker = new String();
		this.localAmountLock = new String();
		this.serverList = new String[2];
		this.initServerList();
		this.randomServer = -1;
	}
	
	private void initServerList()
	{
		if(this.selfName.equals("glados"))
		{
			this.serverList[0] = "delaware";
			this.serverList[1] = "yes";
		}
		
		else if(this.selfName.equals("delaware"))
		{
			this.serverList[0] = "glados";
			this.serverList[1] = "yes";
		}
		
		else if(this.selfName.equals("yes"))
		{
			this.serverList[0] = "delaware";
			this.serverList[1] = "glados";
		}
		
		this.linkSnap.put(this.serverList[0], 0);
		this.linkSnap.put(this.serverList[1], 0);
	}
	
	private int getLocalAmount()
	{
		return this.localAmount;
	}
	
	private void updateLocalAmount(int amount, int mode)
	{
		synchronized (this.localAmountLock)
		{
			if(mode==1)
			{
				this.localAmount = this.localAmount + amount;
			}
			else if(mode==2)
			{
				this.localAmount =  this.localAmount-amount;
			}
			
		}
	}
	
	public void receiveMarker(String hostName)
	{
		synchronized (this.receiveMarker)
		{
			if(this.markerCount.add(hostName))
				this.receiveMarker.notify();
		}
		if(this.firstReceivedMarkerHostName.equals("None"))
			this.firstReceivedMarkerHostName = hostName;
	}
	
	private void sendMarker()
	{
		synchronized (this.sendMarker)
		{
			this.sendMarker.notify();
		}
	}
	
	public void receiveMoney(String hostName, int amount)
	{
		synchronized(this.receiveMoney)
		{
			this.receiveAmount = amount;
			
			if(snapShotTaken && !this.firstReceivedMarkerHostName.equals(hostName)
					&& !this.firstReceivedMarkerHostName.equals("None"))
			{
				this.linkSnap.put(hostName, this.linkSnap.get(hostName) + amount);
			}
			this.receiveMoney.notify();
		}

	}
	
	private void sendMoney(int amount)
	{
		synchronized (this.sendMoney)
		{
			this.sendAmount = amount;
			this.updateLocalAmount(amount, 2);
			this.sendMoney.notify();
		}
	}
	
	public void run()
	{	
		while(true)
		{
			if(Thread.currentThread().getName().equals("sendMoney"))
			{
				try
				{
					synchronized(this.sendMoney)
					{
						this.sendMoney.wait();
						String address = "rmi://" + this.serverList[this.randomServer] + ".cs.rit.edu" + ":" + this.portNo + "/FileServer";
						ServerInterface fileInterface = (ServerInterface)Naming.lookup(address);
						fileInterface.receiveMoney(this.selfName, this.sendAmount);
					}
				}
				catch(Exception e)
				{
					System.out.println("I am in sendMoney Exception");
					e.printStackTrace();
				}
			}
			
			else if(Thread.currentThread().getName().equals("receiveMoney"))
			{
				try
				{
					synchronized (this.receiveMoney) 
					{
						this.receiveMoney.wait();
						this.updateLocalAmount(this.receiveAmount, 1);
					}
				}
				catch(Exception e)
				{
					System.out.println("I am in receiveMoney Exception");
					e.printStackTrace();
				}
			}
			
			else if(Thread.currentThread().getName().equals("sendMarker"))
			{
				try
				{
					synchronized (this.sendMarker) 
					{
						this.sendMarker.wait();
						String address = "rmi://" + this.serverList[0] + ".cs.rit.edu" + ":" + this.portNo + "/FileServer";
						ServerInterface fileInterface = (ServerInterface)Naming.lookup(address);
						fileInterface.receiveMarker(this.selfName);
						address = "rmi://" + this.serverList[1] + ".cs.rit.edu" + ":" + this.portNo + "/FileServer";
						fileInterface = (ServerInterface)Naming.lookup(address);
						fileInterface.receiveMarker(this.selfName);
						this.markerSentProgress = false;
					}
				}
				catch(Exception e)
				{
					System.out.println("I am in sendMarker Exception");
					e.printStackTrace();
				}
			}
			
			else if(Thread.currentThread().getName().equals("receiveMarker"))
			{	
				try
				{
					synchronized (this.receiveMarker)
					{
						this.receiveMarker.wait();
						if(this.markerCount.size()==1)
						{
							if(!this.snapShotTaken)
								this.takeSnapShot();
						}
						
						else if(this.markerCount.size()==2)
						{
							this.printOutput();
							this.snapShotTaken = false;
							this.markerCount.clear();
							this.linkSnap.put(this.serverList[0],0);
							this.linkSnap.put(this.serverList[1],0);
						}
					}
				}
				catch(Exception e)
				{
					System.out.println("I am in receiveMarker Exception");
					e.printStackTrace();
				}
			}
			
			else if(Thread.currentThread().getName().equals("startSnapshot"))
			{	
				try
				{
					synchronized (this.startSnapshot)
					{
						this.startSnapshot.wait();
						if(InetAddress.getLocalHost().getHostName().equals("glados"))
						{
							while(true)
							{
							takeSnapShot();
							Thread.sleep(2000);
							}
						}
					}
				}
				catch(Exception e)
				{
					System.out.println("I am in startSnapshot Exception");
					e.printStackTrace();
				}
			}
		}
	}
	
	private void printOutput()
	{
		System.out.println("Local Value: " + this.localSnapshot);
		System.out.println("Value of incoming channel from " + this.serverList[0]
				+ " is: " + this.linkSnap.get(this.serverList[0]));
		System.out.println("Value of incoming channel from " + this.serverList[1]
				+ " is: " + this.linkSnap.get(this.serverList[1]));
	}
	
	private void takeSnapShot()
	{
		this.snapShotTaken = true;
		this.localSnapshot = this.getLocalAmount();
		this.sendMarker();
	}
	
	private void startSnapshot() throws UnknownHostException, InterruptedException
	{
		synchronized (this.startSnapshot)
		{
			this.startSnapshot.notify();
		}
	}
	
	public void start() throws InterruptedException, UnknownHostException
	{
		this.startSnapshot();
		Random rand = new Random();
		while(true)
		{
			//while(this.markerSentProgress){}
			int amount = rand.nextInt(51);
			this.randomServer = rand.nextInt(2);
			this.sendMoney(amount);
			Thread.sleep(100);
		}	
	}
}
