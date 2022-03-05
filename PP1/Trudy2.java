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

public class Trudy2 {

	// An eavesdropped ticket
	private byte[] ticket = new byte[] { 113, 112, -102, -103, 35, 70, 79, 54, 123, 15, 117, -80, -63, 76, -75, -97,
			-99, -52, 0, 6, 87, -97, -126, -8, 90, -30, -74, -29, 121, -1, -110, -38, 118, 92, -49, 7, -33, -121, -116,
			51, -121, -105, -63, -73, -46, 75, 62, 72, 14, 73, 112, 9, -38, 82, -50, 28, 125, -3, 19, -63, 51, -116, 8,
			-48, -46, 61, 105, -24, -85, 45, -63, -10, -102, 85, -103, -64, -30, -48, -86, -62, -24, 118, 46, -120, 91,
			-105, -102, -78, -33, 88, 20, -34, 95, 50, -95, -24, -89, -7, 80, 62, 38, -80, -21, -118, -48, 88, -29, 34,
			29, -35, -12, -38, -88, 37, -47, -85, 21, 64, 8, -79, 54, 118, 116, -35, -9, -60, 36, 41 };

	private byte[] stolenValue;

	/*
	 * private byte[] ticket = new byte[] {}
	 */

	private Socket socket;
	private DataInputStream iStream;
	private DataOutputStream oStream;

	public Trudy2(int port, byte[] nonce) throws IOException, InterruptedException {

		// Generate a ticket to Bob using the eavesdropped ticket and nonce
		AliceToBobFirstMessage message = new AliceToBobFirstMessage(ticket, nonce);
		byte[] messageBytes = Utility.prepBytes(6, message);

		try {
			// Setup sockets
			socket = new Socket("127.0.0.1", 10234);
			System.out.println("Client accepted!");

			iStream = new DataInputStream(socket.getInputStream());
			oStream = new DataOutputStream(socket.getOutputStream());

			//Send Bob the message
			oStream.writeInt(messageBytes.length);
			oStream.flush();
			oStream.write(messageBytes);
			oStream.flush();

			byte[] bobResponse = new byte[iStream.readInt()];
			iStream.readFully(bobResponse);

			//Splice out the first 8 bytes, because this is the section of the message where Bob subtracts one from the nonce
			byte[] response = new byte[8];
			for (int i = 0; i < response.length; i++)
				response[i] = bobResponse[i];

			stolenValue = response;

		} catch (Exception e) {
			iStream.close();
			oStream.close();
		}
	}
	
	public byte[] getValue() {
		return stolenValue;
	}
}
