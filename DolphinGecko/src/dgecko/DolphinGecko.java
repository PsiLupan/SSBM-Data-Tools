package dgecko;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class DolphinGecko {
	private Socket socket;
	private DataInputStream in;
	
	private enum Status {
		CONNECT, 
		CONNECTED, 
		DISCONNECT
	}
	Status status = Status.CONNECT;
	
	DolphinGecko() throws UnknownHostException, IOException{	
		while(true){
			try{
				Thread.sleep(5);
			}catch(Exception e){
			}
			switch (status){
				case CONNECT:
					socket = new Socket("localhost", 55020);
				    in = new DataInputStream(socket.getInputStream());
					System.out.println("Connected to Dolphin");
					status = Status.CONNECTED;
					break;
				case CONNECTED:
					System.out.println(in.readByte());
					break;
				case DISCONNECT:
					System.exit(0);
					break;
			}
		}
	}
}
