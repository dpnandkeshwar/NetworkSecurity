package PP1;

import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;

class MultithreadedSocketServer extends Thread {

	public static void main(String[] args) throws Exception {

		try {
			ServerSocket server = new ServerSocket(10234);
			int counter = 0;
			System.out.println("Server Started ....");
			while (true) {
				counter++;
				Socket serverClient = server.accept(); // server accept the client connection request
				System.out.println(" >> " + "Trudy" + counter + " connection started!");
				BobReflectionAttack sct = new BobReflectionAttack(serverClient, counter); // send the request to a
																							// separate thread
				sct.start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

class BobReflectionAttack extends Thread {

	Socket serverClient;
	int clientNo;

	public BobReflectionAttack(Socket inSocket, int counter) {
		serverClient = inSocket;
		clientNo = counter;
	}

	private DataInputStream iStream;
	private DataOutputStream oStream;

	private String alice_bob_key;
	private SecretKeySpec bob_key = new SecretKeySpec("azas345ki6o19f4k6lfhd4l6".getBytes(), "DESede");
	private SecretKeySpec K_ab;
	private IvParameterSpec IV = new IvParameterSpec("vectorrr".getBytes());

	public void run() {

		Cipher encryptCipher = null;
		Cipher decryptCipher = null;
		Cipher sharedKeyEncrypt = null;
		Cipher sharedKeyDecrypt = null;

		// These two try/catch blocks are to initialize the encrypt and decrypt ciphers
		try {
			encryptCipher = Cipher.getInstance("DESede/ECB/NoPadding");
			decryptCipher = Cipher.getInstance("DESede/ECB/NoPadding");
			sharedKeyEncrypt = Cipher.getInstance("DESede/ECB/NoPadding");
			sharedKeyDecrypt = Cipher.getInstance("DESede/ECB/NoPadding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e2) {
			System.out.println("No such algorithm or no such padding found bru");
			System.exit(0);
		}

		try {
			encryptCipher.init(Cipher.ENCRYPT_MODE, bob_key);
			decryptCipher.init(Cipher.DECRYPT_MODE, bob_key);
		} catch (InvalidKeyException e1) {
			System.out.println("Invalid key found or invalid parameters found");
			System.exit(0);
		}

		try {
			// Setup the sockets
			iStream = new DataInputStream(serverClient.getInputStream());
			oStream = new DataOutputStream(serverClient.getOutputStream());

			// Constantly read input from clients
			while (true) {
				int message_length = iStream.readInt();
				byte[] message = new byte[message_length];
				iStream.readFully(message);

				/**
				 * Get serialized object - These objects are created to represent some of the
				 * message sent during the protocol. They implement the Serializable interface
				 * to make it easy to send objects across as byte arrays and convert them back
				 * with the necessary information.
				 */
				Object o = Utility.getObject(message);

				// Checks for initial message "I want to talk"
				if (o instanceof SerializedString) {
					Nonce nonce = new Nonce(12345678);
					byte[] nonce_bytes = Utility.prepBytes(1, nonce);
					nonce_bytes = Utility.encrypt(encryptCipher, nonce_bytes);

					System.out.println("2) Bob sends his first challenge");

					oStream.writeInt(nonce_bytes.length);
					oStream.flush();
					oStream.write(nonce_bytes);
					oStream.flush();
				}

				else {

					// By now, Alice has returned from the KDC and Bob is ready to receive the
					// ticket and her challenge
					AliceToBobFirstMessage firstMessageFromAlice = (AliceToBobFirstMessage) o;

					// Decrypt the ticket
					byte[] decryptedTicketBytes = Utility.decrypt(decryptCipher, firstMessageFromAlice.getTicket());
					Ticket ticket = (Ticket) Utility.getObject(decryptedTicketBytes);
					alice_bob_key = ticket.getKey();

					// Initialize the shared key K_ab
					K_ab = new SecretKeySpec(alice_bob_key.getBytes(), "DESede");

					try {
						sharedKeyEncrypt.init(Cipher.ENCRYPT_MODE, K_ab);
						sharedKeyDecrypt.init(Cipher.DECRYPT_MODE, K_ab);
					} catch (InvalidKeyException e1) {
						System.out.println("Invalid key found or invalid parameters found");
						System.exit(0);
					}

					// Decrypt the challenge from alice
					byte[] decryptedAliceNonceBytes = Utility.decrypt(sharedKeyDecrypt,
							firstMessageFromAlice.getEncryptedNonce());
					// Nonce aliceNonce = (Nonce) Utility.getObject(decryptedAliceNonceBytes);

					byte[] trueNonce = new byte[8];

					/**
					 * NOTE: The version of 3DES I am using adds additional padding to messages of
					 * only 8 bytes in length, so we only take the first 8 bytes to accommodate
					 */
					for (int i = 0; i < trueNonce.length; i++)
						trueNonce[i] = decryptedAliceNonceBytes[i];
					

					long aliceNonceValue = Utility.bytesToLong(trueNonce);
					aliceNonceValue -= 1;

					// Generates the message to send Alice's nonce, minus one, and then Bob sends
					// his own nonce
					long nonceValue = System.nanoTime();
					byte[] firstNonce = Utility.longToBytes(aliceNonceValue);

					byte[] secondNonce = Utility.longToBytes(nonceValue);
					byte[] responseToAliceBytes = Utility.combineTwoArrays(firstNonce, secondNonce);
					byte[] encryptedResponseToAliceBytes = Utility.encrypt(sharedKeyEncrypt, responseToAliceBytes);
					
					System.out.println("2) Read message from Alice (Trudy) "
							+ "and send back the nonce minus one and his own challenge");
					
					oStream.writeInt(encryptedResponseToAliceBytes.length);
					oStream.flush();
					oStream.write(encryptedResponseToAliceBytes);

					// Decrypt and read Alice's final nonce value
					byte[] finalResponseFromAliceBytes = new byte[iStream.readInt()];
					iStream.readFully(finalResponseFromAliceBytes);

					byte[] decryptedFinalResponseFromAliceBytes = Utility.decrypt(sharedKeyDecrypt,
							finalResponseFromAliceBytes);
					// Nonce finalResponseFromAlice = (Nonce)
					// Utility.getObject(decryptedFinalResponseFromAliceBytes);
					long finalResponseFromAlice = Utility.bytesToLong(decryptedFinalResponseFromAliceBytes);
					
					System.out.println("6) Verify Alice(Trudy)");

					// Check to see if the nonce values match
					if (finalResponseFromAlice != nonceValue - 1) {
						System.out.println("Incorrect nonce sent back!");
						System.exit(0);
					}

					System.out.println("Authenticaiton complete!");

				}
			}
		} catch (Exception e) {
			try {
				oStream.close();
				iStream.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

}
