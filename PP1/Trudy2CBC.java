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

public class Trudy2CBC {

	// An eavesdropped ticket
	private byte[] ticket = new byte[] { -112, -2, -19, -93, 17, 38, 102, 15, 69, -104, -12, 100, 79, -122, -17, -84,
			-117, -34, 17, -103, 36, 20, 15, 41, -98, 61, 93, 7, -127, 92, -22, 4, 98, 91, 9, -113, -90, 92, 68, 33,
			110, -94, 80, -15, -88, -76, -80, 27, -114, -46, 22, 4, -41, 45, 25, -112, 106, 49, 104, 4, -5, -84, -47,
			-100, 124, -85, 114, -41, 56, 76, 25, -22, 37, 127, -4, 44, -83, -51, -58, 104, 87, 114, 62, 55, -48, -31,
			-115, -70, -128, -99, 90, -78, 42, -59, -20, 99, 11, 78, 6, 55, -106, -115, 111, 123, 65, 67, -51, 78, 78,
			9, -100, -78, 1, -15, 9, -115, 37, 27, 3, -36, -103, -25, 37, 68, 24, -47, -20, 11 };

	private byte[] stolenValue;

	private Socket socket;
	private DataInputStream iStream;
	private DataOutputStream oStream;

	public Trudy2CBC(int port, byte[] nonce) throws IOException, InterruptedException {

		// Generate a ticket to Bob using the eavesdropped ticket and nonce
		AliceToBobFirstMessage message = new AliceToBobFirstMessage(ticket, nonce);
		byte[] messageBytes = Utility.prepBytes(6, message);

		try {
			// Setup sockets
			socket = new Socket("127.0.0.1", 10234);
			System.out.println("Client accepted!");

			iStream = new DataInputStream(socket.getInputStream());
			oStream = new DataOutputStream(socket.getOutputStream());
			
			// Send Bob the message
			oStream.writeInt(messageBytes.length);
			oStream.flush();
			oStream.write(messageBytes);
			oStream.flush();

			byte[] bobResponse = new byte[iStream.readInt()];
			iStream.readFully(bobResponse);
			
			// Splice out the first 8 bytes, because this is the section of the message
			// where Bob subtracts one from the nonce
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
