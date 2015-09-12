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
		CONNECT, CONNECTED, DISCONNECT
	}

	Status statusMajor = Status.CONNECT;

	private enum GameStatus {
		START_MATCH, SELECTED_CHARS, IN_MATCH, END_MATCH;
	}

	private enum StockUpdate {
		ACK, CHAR, STOCK;
	}

	GameStatus statusMinor = GameStatus.START_MATCH;
	StockUpdate stockUpdate = StockUpdate.ACK;

	private String currMap = "0x0";
	private String[] characters = new String[4];
	private byte[] stocks = new byte[4];

	DolphinGecko() throws UnknownHostException, IOException {
		short currChar = 0;
		for (short i = 0; i < stocks.length; i++) {
			stocks[i] = 0x04;
		}

		while (true) {
			switch (statusMajor) {
			case CONNECT:
				socket = new Socket("localhost", 55020);
				in = new DataInputStream(socket.getInputStream());
				System.out.println("Connected to Dolphin");
				statusMajor = Status.CONNECTED;
				break;

			case CONNECTED:
				if (in.available() > 0) {
					// System.out.println("Available Bytes: " + in.available());
					byte[] in_bytes = new byte[in.available()];
					in.read(in_bytes);
					String data = DatatypeConverter.printHexBinary(in_bytes);
					switch (statusMinor) {
					case START_MATCH:
						currMap = data;
						System.out.println("Selected Map: " + currMap);
						statusMinor = GameStatus.SELECTED_CHARS;
						break;

					case SELECTED_CHARS:
						characters[currChar] = lookupChar(data);
						System.out
								.println("Character: " + characters[currChar]);
						currChar += 1;
						if (currChar > 1) { // TODO: Add some kind of ACK to
											// support more than 2 characters
							statusMinor = GameStatus.IN_MATCH;
						}
						break;

					case IN_MATCH:
						switch (stockUpdate) {
						case ACK:
							if (in_bytes[0] == -84) {
								if (in_bytes.length == 3) {
									currChar = in_bytes[1];
									stocks[currChar] = in_bytes[2];
									System.out.println("Stock Update on P"
											+ (currChar + 1) + ": "
											+ stocks[currChar]);
									break;
								} else if (in_bytes.length == 2) {
									currChar = in_bytes[1];
									stockUpdate = StockUpdate.STOCK;
									break;
								}
								stockUpdate = StockUpdate.CHAR;
							}
							break;

						case CHAR:
							if (in_bytes.length == 2) {
								currChar = in_bytes[0];
								stocks[currChar] = in_bytes[1];
								System.out.println("Stock Update on P"
										+ (currChar + 1) + ": "
										+ stocks[currChar]);
								stockUpdate = StockUpdate.ACK;
								break;
							}
							currChar = in_bytes[0];
							stockUpdate = StockUpdate.STOCK;
							break;

						case STOCK:
							stocks[currChar] = in_bytes[0];
							System.out.println("Stock Update on P"
									+ (currChar + 1) + ": " + stocks[currChar]);
							stockUpdate = StockUpdate.ACK;
							break;
						}
						if(stocks[currChar] == 0x0){
							statusMinor = GameStatus.END_MATCH;
						}
						break;

					case END_MATCH:
						cleanupPostMatch();
						statusMinor = GameStatus.START_MATCH;
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

	private void cleanupPostMatch() {
		// TODO: Send results before 0ing.
		currMap = "0x0";
		for (short i = 0; i < characters.length; i++) {
			characters[i] = "0";
		}
		for (short i = 0; i < stocks.length; i++) {
			stocks[i] = 0x04;
		}
	}

	private String lookupChar(String id) {
		String character = "";
		switch (id) {
		case "00":
			character = "Captain Falcon";
			break;
		case "01":
			character = "Donkey Kong";
			break;
		case "02":
			character = "Fox";
			break;
		case "03":
			character = "Mr. Game & Watch";
			break;
		case "04":
			character = "Kirby";
			break;
		case "05":
			character = "Bowser";
			break;
		case "06":
			character = "Link";
			break;
		case "07":
			character = "Luigi";
			break;
		case "08":
			character = "Mario";
			break;
		case "09":
			character = "Marth";
			break;
		case "0A":
			character = "Mewtwo";
			break;
		case "0B":
			character = "Ness";
			break;
		case "0C":
			character = "Peach";
			break;
		case "0D":
			character = "Pikachu";
			break;
		case "0E":
			character = "Ice Climbers";
			break;
		case "0F":
			character = "Jigglypuff";
			break;
		case "10":
			character = "Samus";
			break;
		case "11":
			character = "Yoshi";
			break;
		case "12":
			character = "Zelda";
			break;
		case "13":
			character = "Sheik";
			break;
		case "14":
			character = "Falco";
			break;
		case "15":
			character = "Young Link";
			break;
		case "16":
			character = "Dr. Mario";
			break;
		case "17":
			character = "Roy";
			break;
		case "18":
			character = "Pichu";
			break;
		case "19":
			character = "Ganondorf";
			break;
		case "20":
			character = "Popo";
			break;
		default:
			character = "ILLEGAL";
			break;
		}
		return character;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
