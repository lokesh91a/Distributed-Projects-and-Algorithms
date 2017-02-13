
public class DataPacket {
byte[] data;
public byte[] getData() {
	return data;
}
int peiceNumber;
public int getPeiceNumber() {
	return peiceNumber;
}

public DataPacket(byte[] data, int peiceNumber) {
	super();
	this.data = data;
	this.peiceNumber = peiceNumber;
}

	
	
}
