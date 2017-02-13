import java.io.Serializable;



public class Peer  implements Serializable{
static int peerCount=1;	
private int peerId;
public byte[] getPieceInfo() {
	return pieceInfo;
}

public void setPieceInfo(byte[] pieceInfo) {
	this.pieceInfo = pieceInfo;
}
public int getPeerId() {
	return peerId;
}
public String getIPaddress() {
	return IPaddress;
}
public String getPortNumber() {
	return portNumber;
}
private String IPaddress;
private String portNumber;
private byte[] pieceInfo;

Peer(String IPaddress, String portNumber)
	{
	this.IPaddress=IPaddress;
	this.portNumber=portNumber;
	this.peerId=peerCount++;
	}
Peer(String IPaddress, String portNumber, byte[] pieceIfno)
	{

	this.IPaddress=IPaddress;
	this.portNumber=portNumber;
	this.pieceInfo=pieceIfno;
	this.peerId=peerCount++;
	}	
public String toString()
{
	return this.peerId+"\t"+this.IPaddress+"\n";//+pieceInfoConverter(this.pieceInfo);
}
public String pieceInfoConverter(byte[] pieceBytes)
{ StringBuilder bytes= new StringBuilder();
	for(int counter=0;counter<pieceBytes.length-1;counter++)
	{	bytes.append(pieceBytes[counter]);
	if (counter%10==0 && counter!=0)
		bytes.append(" ");
	
	if (counter%100==0 && counter!=0)
		bytes.append("\n");
	}
	
	return bytes.toString();
	
}
	

}
