package dgecko;

import java.io.DataInputStream;
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
	Status statusMajor = Status.CONNECT;
	
	private enum GameStatus{
		START_MATCH,
		SEL_CHARS_AMOUNT,
		SEL_CHARS,
		IN_MATCH,
		END_MATCH;
	}
	GameStatus statusMinor = GameStatus.START_MATCH;
	
	private byte currMap = 0x0;
	private byte[] characters;
	
	DolphinGecko() throws UnknownHostException, IOException{
		short currChar = 0;
		
		while(true){
			try{
				Thread.sleep(5);
			}catch(Exception e){
			}
			switch (statusMajor){
				case CONNECT:
					socket = new Socket("localhost", 55020);
				    in = new DataInputStream(socket.getInputStream());
					System.out.println("Connected to Dolphin");
					statusMajor = Status.CONNECTED;
					break;
				
				case CONNECTED:
					byte data = in.readByte();
					switch(statusMinor){
						case START_MATCH:
							currMap = data;
							System.out.println("Selected Map: " + currMap);
							statusMinor = GameStatus.SEL_CHARS_AMOUNT;
							break;
						
						case SEL_CHARS_AMOUNT:
							System.out.println("Number of Characters: " + data);
							characters = new byte[data];
							statusMinor = GameStatus.SEL_CHARS;
							break;
						
						case SEL_CHARS:
							if(currChar < characters.length){
								characters[currChar] = data;
								currChar += 1;
							}else{
								currChar = 0;
								statusMinor = GameStatus.IN_MATCH;
							}					
							break;
						
						case IN_MATCH:
							break;
							
						case END_MATCH:
							currMap = 0x0;
							break;
					}
					break;
				
				case DISCONNECT:
					System.exit(0);
					break;
			}
		}
	}
}
