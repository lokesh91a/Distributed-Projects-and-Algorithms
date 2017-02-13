
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class FetchPieces extends UnicastRemoteObject implements FetchPiecesInterface
{
	private HashMap<Integer, ArrayList<Peer>> rarePiecesMap;
	private HashMap<Integer, Peer> peerMap;
	private MetaData MD;
	private HashMap<Integer, Integer> handshakeStatus, chokeStatus;
	private String IP, port, path;
	private int selfId;
	private int choke=1;
	private Queue<Integer> handshakeList, intrestList;
	private Queue<ReuestPacket> downloadRequester;
	//private HashMap<Integer,Integer> downloadRequest;
	private Object handshakelock, intrestMsgLock;
	private ArrayList<Integer> peices;
	private ArrayList<Integer>peicesInProgess;
	private Object PeicesLock = new Object();
	private Object downloadRequestLock = new Object();
	private Queue<DataPacket> ReceivedPacket; 
	private Object PacketLock = new Object();
	private Object receivedHaveLock = new Object();
	private Object haveLock = new Object();
	private Object peerMapLock = new Object();
	private Queue<Integer> sendHaveQue;
	private Queue<ReuestPacket> receiveHaveQue;
	
	public FetchPieces(HashMap<Integer,Peer> peerMap, MetaData MD, int id, String path) throws RemoteException
	{
		this.peerMap = peerMap;
		this.MD = MD;
		this.selfId = id;
		this.path = path;
		this.rarePiecesMap = new HashMap<Integer, ArrayList<Peer>>();
		this.handshakeStatus = new HashMap<Integer, Integer>();
		this.chokeStatus = new HashMap<Integer, Integer>();
		this.handshakeList = new ConcurrentLinkedQueue();
		this.intrestList = new ConcurrentLinkedQueue<>();
		this.handshakelock = new Object();
		this.intrestMsgLock = new Object();
		
		this.peerMap = peerMap;
		//this.downloadRequest = new HashMap<Integer,>();
		this.downloadRequester=new LinkedList<ReuestPacket>();
		this.ReceivedPacket = new LinkedList<DataPacket>();
		peices=new ArrayList<Integer>();
		peicesInProgess=new ArrayList<Integer>();
		sendHaveQue= new LinkedList<Integer>();
		receiveHaveQue = new LinkedList<ReuestPacket>();
		//System.out.println(peerMap);
		
		this.intrestMessageResolver();
		this.handShakeResolver();
		uploadRequestsResolver();
		
		FileWriter();
		haveResolverforReceivedHave();
		sendHaveQueResolver();
		haveResolverforReceivedHave();
	}
	
	public void controller()

	{
		this.init();
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DownloaderThread();
		
		
		
	}
	
	public void show(HashMap<Integer, Integer> map)
	{
		for(int id: map.keySet())
			System.out.println(id+" :"+ map.get(id));
	}
	
	public void getRequiredPecies()
	{	peices.clear();
		rarePiecesMap.clear();
		this.initRarePieceMap();
		buildRarePiecesMap();
		for(int id:rarePiecesMap.keySet())
			if (!rarePiecesMap.get(id).isEmpty()) peices.add(id);
	}
	
	public void init()
	{
		//this.buildRarePiecesMap();
		// check for chock message before creating this map
		
		this.sendHandShakes();
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sendIntrestMsgs();
	}
	
	
	
	private void initRarePieceMap()
	{
		for(int i=0;i<this.MD.getNoOfPieces();i++)
		{
			this.rarePiecesMap.put(i, new ArrayList<Peer>());
		}
	}

	private void buildRarePiecesMap()
	{
		for(Entry<Integer, Peer> entry: this.peerMap.entrySet())
		{
			if(this.selfId!=entry.getValue().getPeerId())
			{
				for(int i=0;i<this.MD.getNoOfPieces();i++)
				{
					if(entry.getValue().getPieceInfo()[i]==1 && peerMap.get(selfId).getPieceInfo()[i]!=1  )
					{
						this.rarePiecesMap.get(i).add(entry.getValue());
					}
				}
			}
		}
	}
	
	private void sendIntrestMsgs()
	{
		for(Entry<Integer, Peer> entry: this.peerMap.entrySet())
		{
			if(this.selfId!=entry.getValue().getPeerId())
			{
				try
				{
				
					int id = entry.getValue().getPeerId();
					this.chokeStatus.put(id, 0);
					FetchPiecesInterface fileInterface=contactClient(id);
		/*			String address = "rmi://" + peerMap.get(id).getIPaddress() + ":"
							+ peerMap.get(id).getPortNumber() + "/Fetch";
					//get object
					FetchPiecesInterface fileInterface = (FetchPiecesInterface)Naming
							.lookup(address);
					//Appropriate method called using interface
					*/
					fileInterface.receiveIntrest(selfId);
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("I am in sendintrest msg Exception");
				}
			}
		}
	}
	
	public void receiveIntrest(int id)
	{
		this.intrestList.add(id);
		synchronized (intrestMsgLock) {
			intrestMsgLock.notify();
		}
	}
	
	public void intrestMsgReply(int id)
	{
		//System.out.println("received reply from :" + id);
		this.chokeStatus.put(id, 1);
	}
	
	private void sendHandShakes()
	{
		for(Entry<Integer, Peer> entry: this.peerMap.entrySet())
		{
			if(this.selfId!=entry.getValue().getPeerId())
			{
				try
				{
				
					int id = entry.getValue().getPeerId();
					this.handshakeStatus.put(id, 0);
				/*	String address = "rmi://" + peerMap.get(id).getIPaddress() + ":"
							+ peerMap.get(id).getPortNumber() + "/Fetch";
					//get object
					FetchPiecesInterface fileInterface = (FetchPiecesInterface)Naming
							.lookup(address);
					//Appropriate method called using interface*/
					contactClient(id).receiveHandShake(selfId);
				}
				catch(Exception e)
				{
					System.out.println("I am in sendHandShakesException");
				}
				
			}
		}
	}
	
	public void receiveHandShake(int peerId)
	{
		this.handshakeList.add(peerId);
		synchronized (this.handshakelock) {
			this.handshakelock.notify();
		}
	}
	
	public void handShakeReply(int peerId, byte[] bitField)
	{
		this.handshakeStatus.put(peerId, 1);
		this.peerMap.get(peerId).setPieceInfo(bitField);
	}
	
	public HashMap<Integer, Peer> contactTracker(int id)
	{
		String registryURL = "rmi://" + this.MD.getTrackerServer() +".cs.rit.edu"+ ":"
				+ this.MD.getTrackerPort() + "/FileServer";
				try
				{ 
					TrackerInterface TI = (TrackerInterface) Naming
								.lookup(registryURL);
					return TI.getPeers(id, this.MD.getFileName());
				}
				catch(Exception e)
				{
					System.out.println("I am in contactTracker exception");
				}
				System.out.println("Returning null");
				return null;
	}
	
	public void handShakeResolver()
	{
		Thread a = new Thread(new Runnable()
		{
			public void run() 
			{
				try
				{
					while(true)
					{
						synchronized (handshakelock) {
							if(handshakeList.isEmpty())
							{
								//System.out.println("handshake resolver waiting");
								handshakelock.wait();
							}
								
						}
						//address is generated
						int id = handshakeList.poll();
						//System.out.println(id);
						if(peerMap==null)
							System.out.println("no peermap exists");
						if(!peerMap.containsKey(id))
						{
							HashMap<Integer, Peer> updated = contactTracker(id);
							peerMap.put(id, updated.get(id));
						}
						/*String address = "rmi://" + peerMap.get(id).getIPaddress() + ":"
								+ peerMap.get(id).getPortNumber() + "/Fetch";
						//get object
						FetchPiecesInterface fileInterface = (FetchPiecesInterface)Naming
								.lookup(address);
						//Appropriate method called using interface*/
						contactClient(id).handShakeReply(selfId, peerMap.get(selfId).getPieceInfo());
			
					  }
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("I am in handShakeResolver Exception");
				}	
			}
			});
		
		a.start();
	}
	
	public void intrestMessageResolver()
	{
		Thread a = new Thread(new Runnable()
		{
			public void run() 
			{
				try
				{
					while(true)
					{
						synchronized (intrestMsgLock) {
							if(intrestList.isEmpty())
							{
								//System.out.println("intrest msg resolver waiting");
								intrestMsgLock.wait();
							}
								
						}
						//address is generated
						int id = intrestList.poll();
						if(!peerMap.containsKey(id))
						{
							HashMap<Integer, Peer> updated = contactTracker(id);
							peerMap.put(id, updated.get(id));
						}
					/*	String address = "rmi://" + peerMap.get(id).getIPaddress() + ":"
								+ peerMap.get(id).getPortNumber() + "/Fetch";
						//get object
						FetchPiecesInterface fileInterface = (FetchPiecesInterface)Naming
								.lookup(address);
						//Appropriate method called using interface */
						contactClient(id).intrestMsgReply(selfId);
			
					  }
				}
				catch(Exception e)
				{
					e.printStackTrace();
					System.out.println("I am in intrestMsg resolver Exception");
				}	
			}
			});
		
		a.start();
	}	

	public void getPeice(int peiceNumber){
		ArrayList<Peer> peers;
		Random r = new Random();
		int counter=0;
		FetchPiecesInterface targetClient;
		Peer peer;
		peers=rarePiecesMap.get(peiceNumber);
		
		//while(counter<peers.size()){
		//System.out.println(peers.size());
		counter=r.nextInt(peers.size());
			peer=peers.get(counter);
			//System.out.println("Asking piece from: "+peer.getPeerId());
			if(this.chokeStatus.get(peer.getPeerId())==1)
				try {
					targetClient=	contactClient(peer.getPeerId());
					targetClient.receivedDownloadRequest(peiceNumber, selfId);
					//break;
				} catch (RemoteException e) {
				 // Any run time remote exception will be ignored and request will be forwarded to next server;
					e.printStackTrace();
				}
		//}
	}
	
	private void upload(ReuestPacket aRequest)
	{
		String home;
		int position=0;
		if(aRequest.getPeiceNumber()!=0)
			position = aRequest.getPeiceNumber()*this.MD.getPieceLength();
		byte[] data;
		//System.out.println("Uploading packet No" +aRequest.PeiceNumber +" to: "+aRequest.getPeerID());
		try {
			home = "/home/stu4/s15/la4401/Courses/CSCI652/BT/"+Inet4Address.getLocalHost().getHostName()+"/"+this.MD.getFileName();
			RandomAccessFile aFile = new RandomAccessFile(home, "r");
			if(this.MD.getSizeOfFile()-position>=this.MD.getPieceLength())
				data=new byte[this.MD.getPieceLength()];
			else
				data=new byte[(int)this.MD.getSizeOfFile()-position];
			System.out.println("Sending data: "+aRequest.PeiceNumber);
			aFile.seek(position);
			aFile.read(data);
			aFile.close();
			if(data!=null)
				  contactClient(aRequest.getPeerID()).ReceivePeice(data, aRequest.PeiceNumber,selfId);
			else
				System.out.println("No Data in packet ");
		} 
		
		catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	public void receivedDownloadRequest(int peiceNumber, int peerID) throws RemoteException
	{	ReuestPacket aRequest = new ReuestPacket(peerID,peiceNumber);
		//System.out.println("Received Download request for "+peiceNumber+" from "+peerID);
		if(!downloadRequester.contains(aRequest)){
		synchronized (downloadRequestLock) {
			this.downloadRequester.add(aRequest);
			downloadRequestLock.notify();
		}
		}
	}
	
	public void DownloaderThread()
	{
		Thread downloader  = new Thread(new Runnable() {
		//Random r = new Random();
	    int counter=0;
		int peiceNumber;
			public void run() {
				//while(peices.size()>0 || peicesInProgess.size()>0)
				while(peicesInProgess.size()==0 && MD.getNoOfPieces()-totalReceivedPackets(peerMap.get(selfId).getPieceInfo())>0)
				{
					getRequiredPecies();
					
					while(peices.size()>0)
						{
							synchronized(PeicesLock){
							//peiceNumber=peices.get(r.nextInt(peices.size()));
							peiceNumber=peices.get(0);	
							//System.out.println("Requesting for: " + peiceNumber);
							//peices.remove(peices.indexOf(peiceNumber));
							peices.remove(0);
							peicesInProgess.add(peiceNumber);}
							getPeice(peiceNumber);
					
						}
				
			}
				
				}
			
		});
				
		downloader.start();
}
	public int totalReceivedPackets(byte[] peiceInfo){
		   BitSet bitset = BitSet.valueOf(peiceInfo);  
		    return bitset.cardinality();
	}
	
	
	
	public void ReceivePeice (byte[] data, int peiceNumber, int donner) throws RemoteException{
		System.out.println("Received "+ peiceNumber+ " from: "+donner);
		if(data!=null){
			synchronized(PacketLock){
			ReceivedPacket.add(new DataPacket(data, peiceNumber));
			PacketLock.notify();
			}
		}
		
	}

	private void uploadRequestsResolver(){
		Thread uploader = new Thread(new Runnable() {
			ReuestPacket requester;
			//byte[] data;
			public void run() {
				while(true){
					//System.out.println("Upload Resolver Ready");
				
						try {
							
							synchronized(downloadRequestLock){		
					if (downloadRequester.size()==0)
							downloadRequestLock.wait();
					
							requester=downloadRequester.poll();}
							upload(requester);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					
				}
				
			
		}});
		
		uploader.start();
		
	}
	
	private void writePacket(DataPacket aDataPacket){
		String path="";
		int position=0;
		try {
			//System.out.println("writing packet: " + aDataPacket.getPeiceNumber());
			path = "/home/stu4/s15/la4401/Courses/CSCI652/BT/"+Inet4Address.getLocalHost().getHostName()+"/"+this.MD.getFileName();
			//System.out.println("Writing on: "+path);
			if(aDataPacket.getPeiceNumber()!=0)
				position = aDataPacket.getPeiceNumber()*this.MD.getPieceLength();
			RandomAccessFile aFile = new RandomAccessFile(path, "rw");
			aFile.seek(position);
			aFile.write(aDataPacket.getData());
			aFile.close();
			synchronized(haveLock){
			sendHaveQue.add(aDataPacket.peiceNumber);
			haveLock.notify();}
		} catch (Exception e) {
					e.printStackTrace();
		}
	}
	
	private void FileWriter()
	{
	
		Thread fileWriter = new Thread(new Runnable() {
			public void run() {
			DataPacket currentDataPacket;
			while(true){
				synchronized(PacketLock){
					if(ReceivedPacket.size()==0)
					{
						try {
							//System.out.println("File Writer waiting");
							PacketLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//System.out.println("started writing");
					currentDataPacket=ReceivedPacket.poll();
				}
				//System.out.println(currentDataPacket.peiceNumber);
				writePacket(currentDataPacket);
				
				synchronized(PeicesLock){
					peicesInProgess.remove(peicesInProgess.indexOf(currentDataPacket.getPeiceNumber()));
				}
				
			}	
			}
		});
		fileWriter.start();
		
	}

	private void sendHaveQueResolver(){
		Thread sendHaveThread = new Thread(new Runnable() {
			public void run() {
				int packageNum;
				synchronized(haveLock){
				while(true){
					try{
					if(sendHaveQue.isEmpty())
						haveLock.wait();
					packageNum=sendHaveQue.poll();
					sendHave(packageNum);
					}catch(Exception ex){
						ex.printStackTrace();
					}
					}
				}
				
			}
		});
		sendHaveThread.start();	
	}

	private void sendHave(int packageNum) throws RemoteException{
	 //ArrayList<Peer> mypeer = peerMap.keySet();
	//System.out.println("Sending have message to all peers");	
	  for (int id: peerMap.keySet()){
		  if(id!=selfId) contactClient(id).receiveHave(packageNum, selfId);
		  else receiveHave(packageNum, selfId);}
  }	
	
	public void receiveHave(int packNum, int sender) throws RemoteException
  {//System.out.println("Received Have for "+packNum +"from: "+sender);
	if(this.peerMap!=null){	
	synchronized(receivedHaveLock){
		
		receiveHaveQue.add(new ReuestPacket(sender, packNum));	
		receivedHaveLock.notify();
	}
	}
	if(sender==selfId && this.MD.getNoOfPieces()==totalReceivedPackets(this.peerMap.get(selfId).getPieceInfo()))
		{
			System.out.println("File Download Completed");
		}
  }
  
	public void haveResolverforReceivedHave(){
	  Thread haveSolver = new Thread(new Runnable() {
		public void run() {
		synchronized(receivedHaveLock){
			while(true){
				try{
				if(receiveHaveQue.isEmpty())
					receivedHaveLock.wait();
				updatePeersOnHave(receiveHaveQue.poll());
				}
				catch(Exception ex)
				{ex.printStackTrace();}
			}
		}	
		}
	});
	  haveSolver.start();
  }
  
	public void updatePeersOnHave(ReuestPacket have)
  {
	if(!peerMap.containsKey(have.peerID))
		peerMap.put(have.peerID, contactTracker(selfId).get(have.peerID)); ;
	  Peer havePeer =peerMap.get(have.peerID);
	  byte[] packet=havePeer.getPieceInfo();
	  packet[have.getPeiceNumber()]=1;
	  synchronized(peerMapLock){
		  peerMap.get(have.peerID).setPieceInfo(packet);}
	 // System.out.println("Updated Peer Map");
	  if(rarePiecesMap!=null && peerMap!=null && rarePiecesMap.containsKey(have.PeiceNumber) && peerMap.get(selfId).getPieceInfo()	[have.getPeiceNumber()]!=1)
		  rarePiecesMap.get(have.PeiceNumber).add(peerMap.get(have.peerID));
  }

	private FetchPiecesInterface contactClient(int id) throws  RemoteException{
		String address = "rmi://" + peerMap.get(id).getIPaddress() + ":"
				+ peerMap.get(id).getPortNumber() + "/Fetch";
		//get object
		FetchPiecesInterface fileInterface=null;
		try {
			fileInterface = (FetchPiecesInterface)Naming
					.lookup(address);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileInterface;
		
	}
	
	private FetchPiecesInterface contactClient(int id, String IP) throws  RemoteException{
		String address = "rmi://" + IP +  "/Fetch";
		//get object
		FetchPiecesInterface fileInterface=null;
		try {
			fileInterface = (FetchPiecesInterface)Naming
					.lookup(address);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return fileInterface;
		
	}

}
