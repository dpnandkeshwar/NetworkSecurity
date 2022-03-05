package PP1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TrudyCBC {

	// An eavesdropped encrypted nonce
	private byte[] encryptedNonce = new byte[] { 13, 19, -1, -33, 94, -26, -24, -120, 
	                                              -90, -21, 44, 106, -99, -68, 31, -39 };
	
	// An eavesdropped ticket to Bob
	private byte[] ticket = new byte[] { -112, -2, -19, -93, 17, 38, 102, 15, 69, -104, -12, 100, 79, -122, -17, -84,
			-117, -34, 17, -103, 36, 20, 15, 41, -98, 61, 93, 7, -127, 92, -22, 4, 98, 91, 9, -113, -90, 92, 68, 33,
			110, -94, 80, -15, -88, -76, -80, 27, -114, -46, 22, 4, -41, 45, 25, -112, 106, 49, 104, 4, -5, -84, -47,
			-100, 124, -85, 114, -41, 56, 76, 25, -22, 37, 127, -4, 44, -83, -51, -58, 104, 87, 114, 62, 55, -48, -31,
			-115, -70, -128, -99, 90, -78, 42, -59, -20, 99, 11, 78, 6, 55, -106, -115, 111, 123, 65, 67, -51, 78, 78,
			9, -100, -78, 1, -15, 9, -115, 37, 27, 3, -36, -103, -25, 37, 68, 24, -47, -20, 11 };

	private Socket socket;
	private DataInputStream iStream;
	private DataOutputStream oStream;

	public static void main(String[] args) {
		try {
			TrudyCBC t = new TrudyCBC(10234);
		} catch (InterruptedException | IOException e) {
			System.out.println("Unhandled exception, please try again");
			System.exit(0);
		}
	}

	public TrudyCBC(int port) throws IOException, InterruptedException {

		try {
			// Setup the sockets
			socket = new Socket("127.0.0.1", 10234);
			System.out.println("Client accepted!");

			iStream = new DataInputStream(socket.getInputStream());
			oStream = new DataOutputStream(socket.getOutputStream());
						
			//Generate a message to Bob using the eavesdropped ticket and nonce
			AliceToBobFirstMessage initialMessage = new AliceToBobFirstMessage(ticket, encryptedNonce);
			byte[] initialMessageS = Utility.prepBytes(6, initialMessage);

			System.out.println("1) Send Bob an eaves dropped message");
			oStream.writeInt(initialMessageS.length);
			oStream.flush();
			oStream.write(initialMessageS);
			oStream.flush();

			byte[] bobResponse = new byte[iStream.readInt()];
			iStream.readFully(bobResponse);

			System.out.println("3) Read Bob's response and attempt to splice out N_3");
			
			// Splice the last 8 bits to extract Bob's nonce
			byte[] response = new byte[8];
			for (int i = 0; i < response.length; i++)
				response[i] = bobResponse[i + 8];
			
			System.out.println("4) Open up a second connection to Bob to trick him into sending us back N_3 - 1");

			// Open up the second connection to Bob through a second Trudy class
			Trudy2CBC reflection = new Trudy2CBC(port, response);
			
			byte[] stolenValue = reflection.getValue();
			
			System.out.println("5) Finally, send back to Bob N_3 - 1 and try to authenticate");
			
			oStream.writeInt(stolenValue.length);
			oStream.flush();
			oStream.write(stolenValue);
			oStream.flush();
			
		} catch (Exception e) {
			iStream.close();
			oStream.close();
		}
	}
}
