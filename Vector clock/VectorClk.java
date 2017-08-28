import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class VectorClk extends UnicastRemoteObject implements ServerInterface
{
	private HashMap<String, Integer> clkTime;
	private int portNo, amount;
	private String selfName;
	private String[] serverList, completeList;
	Random rand;
	
	
	public VectorClk(int portNo) throws RemoteException, UnknownHostException
	{
		this.clkTime = new HashMap<String, Integer>();
		this.amount = 1000;
		this.serverList = new String[2];
		this.completeList = new String[3];
		this.rand = new Random();
		this.portNo = portNo;
		this.selfName = InetAddress.getLocalHost().getHostName();
		this.initClkTime();
		this.initServerList();
	}

	private void initClkTime() throws UnknownHostException
	{
		this.clkTime.put("glados", 0);
		this.clkTime.put("yes", 0);
		this.clkTime.put("delaware", 0);
		this.clkTime.put(this.selfName, 1);
		this.completeList[0] = "glados";
		this.completeList[1] = "delaware";
		this.completeList[2] = "yes";
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
	}
	
	private void withdraw(int amount)
	{
		this.amount -= amount;
		this.clkTime.put(this.selfName, this.clkTime.get(this.selfName) + 1);
	}
	
	private void deposit(int amount)
	{
		this.amount += amount;
		this.clkTime.put(this.selfName, this.clkTime.get(this.selfName) + 1);
	}
	
	private void sendMoney(int amount, String hostName) throws UnknownHostException, MalformedURLException, RemoteException, NotBoundException
	{
		this.amount -= amount;
		this.clkTime.put(this.selfName, this.clkTime.get(this.selfName) + 1);
		String address = "rmi://" + hostName + ".cs.rit.edu" + ":" + this.portNo + "/FileServer";
		ServerInterface fileInterface = (ServerInterface)Naming.lookup(address);
		fileInterface.receiveMoney(this.clkTime.get("glados"), this.clkTime.get("delaware"), this.clkTime.get("yes"));
	}
	
	private int findMax(int value1, int value2)
	{
		if(value1>value2)
			return value1;
		else
			return value2;
	}
	
	public void receiveMoney(int time1, int time2, int time3)
	{
			int max=0;
			int ownValue = this.clkTime.get(this.selfName)+1;
			if(this.selfName.equals("glados"))
			{
				max = this.findMax(time1, ownValue);
				this.clkTime.put("glados", max);
				max = this.findMax(time2, this.clkTime.get("delaware"));
				this.clkTime.put("delaware", max);
				max = this.findMax(time3, this.clkTime.get("yes"));
				this.clkTime.put("yes", max);
			}
			
			else if(this.selfName.equals("delaware"))
			{
				max = this.findMax(time2, ownValue);
				this.clkTime.put("delaware", max);
				max = this.findMax(time1, this.clkTime.get("glados"));
				this.clkTime.put("glados", max);
				max = this.findMax(time3, this.clkTime.get("yes"));
				this.clkTime.put("yes", max);
			}
			
			else if(this.selfName.equals("yes"))
			{
				max = this.findMax(time3, ownValue);
				this.clkTime.put("yes", max);
				max = this.findMax(time2, this.clkTime.get("delaware"));
				this.clkTime.put("delaware", max);
				max = this.findMax(time1, this.clkTime.get("glados"));
				this.clkTime.put("glados", max);
			}
			System.out.println("Vector clock receive event is: " +
					"glados:yes:delaware = " + this.clkTime.get("glados") + ":" + 
					this.clkTime.get("yes") + ":" + this.clkTime.get("delaware"));
	}
	
	private void executeProcess(int processId, int amount) throws UnknownHostException, MalformedURLException, RemoteException, NotBoundException
	{
		if(processId==1)
		{
			this.deposit(amount);
			System.out.println("Deposited amount = " + amount);
		}
			
		else if(processId==2)
		{
			this.withdraw(amount);
			System.out.println("Withdrawed amount = " + amount);
		}
			
		else if(processId==3)
		{
			int server = this.rand.nextInt(2);
			this.sendMoney(amount, this.serverList[server]);
			System.out.println("Sent amount " + amount + " to " + this.serverList[server]);
		}
		System.out.println("Vector clock for this event is: " +
				"glados:yes:delaware = " + this.clkTime.get("glados") + ":" + 
				this.clkTime.get("yes") + ":" + this.clkTime.get("delaware"));
	}
	
	public void run()
	{
		try
		{
			System.out.println("Initial vector clock for this server is: " +
					"glados:yes:delaware = " + this.clkTime.get("glados") + ":" + 
					this.clkTime.get("yes") + ":" + this.clkTime.get("delaware"));
			while(true)
			{
				int time = this.rand.nextInt(6);
				Thread.sleep(time*1000);
				int processId = this.rand.nextInt(3)+1;
				int amount = this.rand.nextInt(101);
				this.executeProcess(processId, amount);
				Thread.sleep(5000-time*1000);
			}
		}
		
		catch(Exception e)
		{
			System.out.println("I am in run exception");
			e.printStackTrace();
		}
	}
	
}
