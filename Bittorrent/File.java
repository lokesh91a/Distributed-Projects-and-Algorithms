

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;


public class File {
private String FileName;
private int numberOfPecies;
private int peiceLength;
private HashMap<Integer,Peer> peerRecord= new HashMap<Integer, Peer>();



public HashMap<Integer, Peer> getPeerRecord() {
	return peerRecord;
}

File(String fileName, int numberOfPecies, int peiceLength)
	{
		this.FileName=fileName;
		this.numberOfPecies=numberOfPecies;
		this.peiceLength=peiceLength;
	
	}

public synchronized void addPeer(Peer aPeer){
peerRecord.put(aPeer.getPeerId(), aPeer);
}

/**
 * call this mathod to get all peers 
 * @return
 */
public synchronized HashMap<Integer, Peer> getPeers()
{

	return  (HashMap<Integer,Peer>)peerRecord.clone();
}

public String toString(){
	String output="";
	for(int id : this.peerRecord.keySet())
		output=output+"\n"+peerRecord.get(id).toString();
	return output;
}

}
