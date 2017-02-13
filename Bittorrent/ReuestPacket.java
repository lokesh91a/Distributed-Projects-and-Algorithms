
public class ReuestPacket {
public ReuestPacket(int peerID, int peiceNumber) {
		super();
		this.peerID = peerID;
		PeiceNumber = peiceNumber;
	}
int peerID;
@Override
public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + PeiceNumber;
	result = prime * result + peerID;
	return result;
}
@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	ReuestPacket other = (ReuestPacket) obj;
	if (PeiceNumber != other.PeiceNumber)
		return false;
	if (peerID != other.peerID)
		return false;
	return true;
}
public int getPeerID() {
	return peerID;
}
public void setPeerID(int peerID) {
	this.peerID = peerID;
}
int PeiceNumber;
public int getPeiceNumber() {
	return PeiceNumber;
}
public void setPeiceNumber(int peiceNumber) {
	PeiceNumber = peiceNumber;
}
}
