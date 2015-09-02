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
		SEL_CHARS_AMOUNT,
		SEL_CHARS,
		IN_MATCH,
		END_MATCH;
	}
	GameStatus statusMinor = GameStatus.START_MATCH;
	
	private String currMap = "0x0";
	private byte[] characters;
	
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
					bytes[0] = in.readByte();
					System.out.println("Gecko Console Version: " + DatatypeConverter.printHexBinary(bytes));
					statusMajor = Status.CONNECTED;
					break;
				
				case CONNECTED:
					if(in.available() >= 1){
						byte[] in_bytes = new byte[1];
						in_bytes[0] = in.readByte();
						String data = DatatypeConverter.printHexBinary(in_bytes);
						switch(statusMinor){
							case START_MATCH:
								currMap = data;
								System.out.println("Selected Map: " + currMap);
								statusMinor = GameStatus.SEL_CHARS_AMOUNT;
								break;
						
							case SEL_CHARS_AMOUNT:
								System.out.println("Number of Characters: " + data);
								characters = new byte[Integer.parseInt(data)];
								statusMinor = GameStatus.SEL_CHARS;
								break;
						
							case SEL_CHARS:
								if(currChar < characters.length){
									characters[currChar] = Byte.parseByte(data);
									currChar += 1;
								}else{
									currChar = 0;
									statusMinor = GameStatus.IN_MATCH;
								}					
								break;
						
							case IN_MATCH:
								break;
							
							case END_MATCH:
								currMap = "0x0";
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
