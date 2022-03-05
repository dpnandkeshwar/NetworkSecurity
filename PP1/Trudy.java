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

public class Trudy {

	// An eavesdropped encrypted nonce
	private byte[] encryptedNonce = new byte[] { 6, -17, 49, 121, -14, 56, -70, -40, -4, 
	                                              100, -17, 77, -120, -25, 117, 35 };
	
	// An eavesdropped ticket to Bob
	private byte[] ticket = { 113, 112, -102, -103, 35, 70, 79, 54, 123, 15, 117, -80, -63, 76, -75, -97,
			-99, -52, 0, 6, 87, -97, -126, -8, 90, -30, -74, -29, 121, -1, -110, -38, 118, 92, -49, 7, -33, -121, -116,
			51, -121, -105, -63, -73, -46, 75, 62, 72, 14, 73, 112, 9, -38, 82, -50, 28, 125, -3, 19, -63, 51, -116, 8,
			-48, -46, 61, 105, -24, -85, 45, -63, -10, -102, 85, -103, -64, -30, -48, -86, -62, -24, 118, 46, -120, 91,
			-105, -102, -78, -33, 88, 20, -34, 95, 50, -95, -24, -89, -7, 80, 62, 38, -80, -21, -118, -48, 88, -29, 34,
			29, -35, -12, -38, -88, 37, -47, -85, 21, 64, 8, -79, 54, 118, 116, -35, -9, -60, 36, 41 };

	private Socket socket;
	private DataInputStream iStream;
	private DataOutputStream oStream;

	public static void main(String[] args) {
		try {
			Trudy t = new Trudy(10234);
		} catch (InterruptedException | IOException e) {
			System.out.println("Unhandled exception, please try again");
			System.exit(0);
		}
	}

	public Trudy(int port) throws IOException, InterruptedException {

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
			Trudy2 reflection = new Trudy2(port, response);
			
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
