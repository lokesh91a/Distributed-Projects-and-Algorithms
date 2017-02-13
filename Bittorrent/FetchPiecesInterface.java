

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.lang.model.type.UnionType;

public interface FetchPiecesInterface extends Remote
{
	public void receiveHandShake(int peerId) throws RemoteException;
	public void handShakeReply(int peerId, byte[] bitField) throws RemoteException;
	public void controller() throws RemoteException;
	public void receiveIntrest(int id) throws RemoteException;
	public void intrestMsgReply(int id) throws RemoteException;
	public void receivedDownloadRequest(int peiceNumber, int peerID) throws RemoteException;
	public void ReceivePeice (byte[] data, int peiceNumber, int donner) throws RemoteException;
	public void receiveHave(int packNum, int sender) throws RemoteException;
}
