

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class MetaData implements Serializable
{
	private String fileName, path, trackerServer;
	private int pieceLength, trackerPort;
	private long sizeOfFile, noOfPieces;
	private byte[] hashCodes;
	File file;
	
	public MetaData(String path, String fileName)
	{
		this.fileName = fileName;
		this.path = path;
		this.pieceLength = 20;
		this.file = new File(path);
		try {
			this.trackerServer = "newyork";
		} catch (Exception e) {
			System.out.println("I am in metadata constructor exception");
		}
		this.trackerPort = 8963;
	}
	
	public String getFileName()
	{
		return this.fileName;
	}
	
	public String getTrackerServer()
	{
		return this.trackerServer;
	}
	
	public int getPieceLength()
	{
		return this.pieceLength;
	}
	
	public int getTrackerPort()
	{
		return this.trackerPort;
	}
	
	public long getSizeOfFile()
	{
		return this.sizeOfFile;
	}
	
	public long getNoOfPieces()
	{
		return this.noOfPieces;
	}
	
	private void set()
	{
		if(this.file.exists())
			System.out.println("Exists");
		this.sizeOfFile = this.file.length();
		this.noOfPieces = this.sizeOfFile%this.pieceLength==0?
				this.sizeOfFile/this.pieceLength:this.sizeOfFile/this.pieceLength+1;
		this.hashCodes = new byte[(int) this.noOfPieces];
	}
	
	private void setHashCodes()
	{
		try
		{
			
			long position=0, piece=0;
			String val="";
			while(position<=this.sizeOfFile)
			{
				RandomAccessFile file = new RandomAccessFile(this.path, "r");
				file.seek(position);
		        byte[] bytes = new byte[this.pieceLength];
		        file.read(bytes);
		        file.close();
		        val = String.valueOf(bytes);
		        position+=this.pieceLength;
		        if(Math.abs(val.hashCode())%2==0)
		        	this.hashCodes[(int) piece]=0;
		        else
		        	this.hashCodes[(int) piece]=1;
		        piece++;
			}
			if(this.sizeOfFile-position>0)
			{
				RandomAccessFile file = new RandomAccessFile(this.path, "r");
				file.seek(position);
		        byte[] bytes = new byte[(int)(this.sizeOfFile-position)];
		        file.read(bytes);
		        file.close();
		        val = String.valueOf(bytes);
		        position+=this.pieceLength;
		        if(Math.abs(val.hashCode())%2==0)
		        	this.hashCodes[(int) piece]=0;
		        else
		        	this.hashCodes[(int) piece]=1;
		        piece++;
			}
		}
		catch(Exception e)
		{
			System.out.println("I am in metadata exception");
		}
	}
	
	private void serialize(MetaData MD)
	{
		try
		{
			FileOutputStream fout=new FileOutputStream("metadata.txt");  
			ObjectOutputStream out=new ObjectOutputStream(fout);
			out.writeObject(MD);
			out.flush();
		}
		catch(Exception e)
		{
			System.out.println("I am in exception while serialize");
		}
	}
	
	public static void main(String arg[])
	{
		String path = "/home/devil/Desktop/Test";
		File file = new File(path);
		MetaData MD = new MetaData(path, "Test");
		MD.set();
		MD.setHashCodes();
		MD.serialize(MD);
	}
}
