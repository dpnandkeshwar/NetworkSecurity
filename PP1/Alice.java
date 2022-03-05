package PP1;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
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

public class Alice {

	private Socket bobSocket;
	private Socket kdcSocket;
	private DataOutputStream bobOStream;
	private DataOutputStream kdcOStream;
	// private InputStreamReader iStream;
	private DataInputStream bobIStream;
	private DataInputStream kdcIStream;

	private String alice_bob_key;
	private SecretKeySpec alice_key = new SecretKeySpec("aheivkal12i83jhg03kl45ka".getBytes(), "TripleDES");
	private SecretKeySpec K_ab;
	private IvParameterSpec IV = new IvParameterSpec("vectorrr".getBytes());

	public static void main(String[] args) {
		try {
			Alice a = new Alice("127.0.0.1", 10234, 10235);
		} catch (IOException e) {
			System.out.println("IOException found, please try again");
			System.exit(0);
		}
	}

	public Alice(String ipAddress, int bobPort, int kdcPort) throws UnknownHostException, IOException {

		Cipher encryptCipher = null;
		Cipher decryptCipher = null;
		Cipher sharedKeyEncrypt = null;
		Cipher sharedKeyDecrypt = null;

		// These two try/catch blocks are to initialize the encrypt and decrypt ciphers
		try {
			encryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			decryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			sharedKeyEncrypt = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			sharedKeyDecrypt = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e2) {
			System.out.println("No such algorithm or no such padding found");
			System.exit(0);
		}

		try {
			encryptCipher.init(Cipher.ENCRYPT_MODE, alice_key, IV);
			decryptCipher.init(Cipher.DECRYPT_MODE, alice_key, IV);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e1) {
			System.out.println("Invalid key found or invalid parameters found");
			System.exit(0);
		}

		try {
			// Setup sockets between Alice and Bob as well as Alice and KDC
			bobSocket = new Socket(ipAddress, bobPort);
			System.out.println("Connected to Bob");
			bobOStream = new DataOutputStream(bobSocket.getOutputStream());
			bobIStream = new DataInputStream(bobSocket.getInputStream());
			kdcIStream = new DataInputStream(bobSocket.getInputStream());

			// Setup initial message from Alice to Bob - "I wnat to talk"
			SerializedString initiateCommunication = new SerializedString("I want to talk");
			byte[] initiateCommuncationS = Utility.prepBytes(0, initiateCommunication);
			
			System.out.println("1) Alice initiates communication with Bob");
			
			bobOStream.writeInt(initiateCommuncationS.length);
			bobOStream.flush();
			bobOStream.write(initiateCommuncationS);

			// Read Bob's encrypted challenge as bytes
			byte[] bob_encrypted_challenge = new byte[bobIStream.readInt()];
			bobIStream.readFully(bob_encrypted_challenge);

			// Set up connection with the KDC
			kdcSocket = new Socket(ipAddress, kdcPort);
			System.out.println("Connected to KDC");
			kdcOStream = new DataOutputStream(kdcSocket.getOutputStream());
			kdcIStream = new DataInputStream(new BufferedInputStream(kdcSocket.getInputStream()));

			// Create a KDC request message
			AliceKDCRequest kdcRequest = new AliceKDCRequest(System.nanoTime(), "Alice wants Bob",
					bob_encrypted_challenge);
			byte[] aliceRequest = Utility.prepBytes(4, kdcRequest);
			
			System.out.println("3) Alice beings communication with the KDC");
			
			kdcOStream.writeInt(aliceRequest.length);
			kdcOStream.flush();
			kdcOStream.write(aliceRequest);
			kdcOStream.flush();

			// Read the response from the KDC
			byte[] responseFromKDC = new byte[kdcIStream.readInt()];
			kdcIStream.readFully(responseFromKDC);

			byte[] decryptedResponseFromKDC = Utility.decrypt(decryptCipher, responseFromKDC);

			KDCResponse kdcResponse = (KDCResponse) Utility.getObject(decryptedResponseFromKDC);

			// Set up the shared key K_ab
			alice_bob_key = kdcResponse.getKey();

			K_ab = new SecretKeySpec(alice_bob_key.getBytes(), "DESede");

			try {
				sharedKeyEncrypt.init(Cipher.ENCRYPT_MODE, K_ab, IV);
				sharedKeyDecrypt.init(Cipher.DECRYPT_MODE, K_ab, IV);
			} catch (InvalidKeyException e1) {
				System.out.println("Invalid key found or invalid parameters found");
				System.exit(0);
			}

			// Calculate Alice's first nonce value, and send to Bob with his ticket
			long nonceValue = System.nanoTime();
			Nonce nonceToBob = new Nonce(nonceValue);
			byte[] nonceToBobBytes = Utility.prepBytes(1, nonceToBob);
			
			//To get information for reflection attack
			//nonceToBobBytes = Utility.longToBytes(nonceValue);
			
			byte[] nonceToBobBytesEncrypted = Utility.encrypt(sharedKeyEncrypt, nonceToBobBytes);

			AliceToBobFirstMessage firstMessage = new AliceToBobFirstMessage(kdcResponse.getTicket(),
					nonceToBobBytesEncrypted);
			byte[] firstMessageBytes = Utility.prepBytes(6, firstMessage);
			
			System.out.println("6) Alice begins communication with Bob, sends first nonce");

			bobOStream.writeInt(firstMessageBytes.length);
			bobOStream.flush();
			bobOStream.write(firstMessageBytes);

			// Read Bob's response
			byte[] bobAuthenticationResponseBytes = new byte[bobIStream.readInt()];
			bobIStream.readFully(bobAuthenticationResponseBytes);
			byte[] bobAuthenticationResponseBytesDecrypted = Utility.decrypt(sharedKeyDecrypt,
					bobAuthenticationResponseBytes);
			TwoNonce bobAuthenticationResponse = (TwoNonce) Utility.getObject(bobAuthenticationResponseBytesDecrypted);

			// Check to see if Bob's nonce matches the one Alice sent, minus one
			if (bobAuthenticationResponse.getNonceOne() != nonceValue - 1) {
				System.out.println("Incorrect nonce sent back!");
				System.exit(0);
			}

			// Take Bob's nonce and minus one from it
			long nonceFromBob = bobAuthenticationResponse.getNonceTwo();
			nonceFromBob -= 1;

			// Setup Alice's final response to Bob
			Nonce finalResponseToBob = new Nonce(nonceFromBob);
			byte[] finalResponseToBobBytes = Utility.prepBytes(1, finalResponseToBob);
			byte[] encryptedFinalResponseToBobBytes = Utility.encrypt(sharedKeyEncrypt, finalResponseToBobBytes);
			
			System.out.println("8) Alice verifies the nonce Bob sends back, and sends his nonce value, minus one");

			bobOStream.writeInt(encryptedFinalResponseToBobBytes.length);
			bobOStream.flush();
			bobOStream.write(encryptedFinalResponseToBobBytes);
			bobOStream.flush();

			System.out.println("Authentication complete!");
		} catch (Exception e) {
			bobOStream.close();
			bobIStream.close();
			kdcOStream.close();
			kdcIStream.close();
		}
	}
}
