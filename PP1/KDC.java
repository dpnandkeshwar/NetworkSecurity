package PP1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KDC {

	private Socket socket;
	private ServerSocket serverSocket;
	private DataInputStream iStream;
	private DataOutputStream oStream;

	private String alice_bob_key = "al231kdjfn12ifbhz8k3l4f8";

	private SecretKeySpec bob_key = new SecretKeySpec("azas345ki6o19f4k6lfhd4l6".getBytes(), "DESede");
	private SecretKeySpec alice_key = new SecretKeySpec("aheivkal12i83jhg03kl45ka".getBytes(), "DESede");
	private IvParameterSpec IV = new IvParameterSpec("vectorrr".getBytes());

	public static void main(String[] args) {
		try {
			KDC kdc = new KDC(10235);
		} catch (InterruptedException | IOException e) {
			System.out.println("Unhandled exception, please try again");
			System.exit(0);
		}
	}

	public KDC(int port) throws IOException, InterruptedException {

		Cipher aliceEncryptCipher = null;
		Cipher aliceDecryptCipher = null;
		Cipher bobDecryptCipher = null;
		Cipher bobEncryptCipher = null;

		// These two try/catch blocks are to initialize the encrypt and decrypt ciphers
		try {
			aliceEncryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			aliceDecryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			bobEncryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			bobDecryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e2) {
			System.out.println("No such algorithm or no such padding found");
			System.exit(0);
		}

		try {
			aliceEncryptCipher.init(Cipher.ENCRYPT_MODE, alice_key, IV);
			aliceDecryptCipher.init(Cipher.DECRYPT_MODE, alice_key, IV);
			bobEncryptCipher.init(Cipher.ENCRYPT_MODE, bob_key, IV);
			bobDecryptCipher.init(Cipher.DECRYPT_MODE, bob_key, IV);

		} catch (InvalidKeyException | InvalidAlgorithmParameterException e1) {
			System.out.println("Invalid key found or invalid parameters found");
			System.exit(0);
		}

		try {
			// Setup the sockets
			serverSocket = new ServerSocket(port);
			System.out.println("Server started");
			System.out.println("Waiting for client connection...");

			socket = serverSocket.accept();
			System.out.println("Client accepted!");

			iStream = new DataInputStream(socket.getInputStream());
			oStream = new DataOutputStream(socket.getOutputStream());

			// Read Alice's request to the KDC
			byte[] kdcRequestBytes = new byte[iStream.readInt()];
			iStream.readFully(kdcRequestBytes);
			
			System.out.println("4) KDC receives communication from Alice");

			Object o = Utility.getObject(kdcRequestBytes);
			AliceKDCRequest kdcRequest = (AliceKDCRequest) o;

			// Extract Bob's challenge
			byte[] bobChallenge = kdcRequest.getEncryptedChallenge();
			byte[] decryptedBobChallenge = Utility.decrypt(bobDecryptCipher, bobChallenge);

			Nonce bobNonce = (Nonce) Utility.getObject(decryptedBobChallenge);

			// Create the ticket to Bob
			Ticket ticketToBob = new Ticket(alice_bob_key, "1", bobNonce.getNonce());
			byte[] ticketBytes = Utility.prepBytes(5, ticketToBob);
			byte[] encryptedTicketBytes = Utility.encrypt(bobEncryptCipher, ticketBytes);

			// Send the key and the ticket to Bob back to Alice
			KDCResponse kdcResponse = new KDCResponse(kdcRequest.getNonce(), "1", alice_bob_key, encryptedTicketBytes);
			byte[] kdcResponseBytes = Utility.prepBytes(2, kdcResponse);
			byte[] encryptedKDCResponseBytes = Utility.encrypt(aliceEncryptCipher, kdcResponseBytes);
			
			System.out.println("5) KDC sends back shared key to Alice along with ticket to Bob");

			oStream.writeInt(encryptedKDCResponseBytes.length);
			oStream.flush();
			oStream.write(encryptedKDCResponseBytes);
			oStream.flush();

			System.out.println("KDC Finished");

		} catch (Exception e) {
			iStream.close();
			oStream.close();
		}
	}
}
