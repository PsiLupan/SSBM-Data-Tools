package dgecko;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.DatatypeConverter;

public class DolphinGecko {
	private Socket socket;
	private DataInputStream in;
	
	private enum Status {
		CONNECT, 
		CONNECTED, 
		DISCONNECT
	}
	Status statusMajor = Status.CONNECT;
	
	private enum GameStatus{
		START_MATCH,
		SELECTED_CHARS,
		IN_MATCH,
		END_MATCH;
	}
	GameStatus statusMinor = GameStatus.START_MATCH;
	
	private String currMap = "0x0";
	private byte[] characters = new byte[4];
	private byte[] stocks = new byte[4];
	
	DolphinGecko() throws UnknownHostException, IOException{
		short currChar = 0;
		
		while(true){
			switch (statusMajor){
				case CONNECT:
					socket = new Socket("localhost", 55020);
				    in = new DataInputStream(socket.getInputStream());
					System.out.println("Connected to Dolphin");
					socket.getOutputStream().write((byte)0x99);
					byte[] bytes = new byte[1];
					in.read(bytes);
					System.out.println("Gecko Console Version: " + DatatypeConverter.printHexBinary(bytes));
					statusMajor = Status.CONNECTED;
					break;
				
				case CONNECTED:
					if(in.available() > 0){
						byte[] in_bytes = new byte[in.available()];
						in.read(in_bytes);
						String data = DatatypeConverter.printHexBinary(in_bytes);
						switch(statusMinor){
							case START_MATCH:
								currMap = data;
								System.out.println("Selected Map: " + currMap);
								statusMinor = GameStatus.SELECTED_CHARS;
								break;
						
							case SELECTED_CHARS:
								System.out.println("Character: " + data);
								characters[currChar] = in_bytes[0];
								currChar += 1;
								if(currChar > 1){ //TODO: Add some kind of ACK to support more than 2 characters
									currChar = 0;
									statusMinor = GameStatus.IN_MATCH;
								}
								break;
						
							case IN_MATCH:
								if(data.equals("AA")){
									//TODO: Use ACK to determine character to remove stock from
								}
									
								break;
							
							case END_MATCH:
								cleanupPostMatch();
								break;
						}
					}
					break;
				
				case DISCONNECT:
					System.exit(0);
					break;
			}
		}
	}
	
	private void cleanupPostMatch(){
		//TODO: Send results before 0ing.
		currMap = "0x0";
		for(short i = 0; i < characters.length; i++){
			characters[i] = 0;
		}
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
