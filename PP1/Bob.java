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
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Bob {

	private Socket socket;
	private ServerSocket serverSocket;
	private DataInputStream iStream;
	private DataOutputStream oStream;

	private String alice_bob_key;
	private SecretKeySpec bob_key = new SecretKeySpec("azas345ki6o19f4k6lfhd4l6".getBytes(), "DESede");
	private SecretKeySpec K_ab;
	private IvParameterSpec IV = new IvParameterSpec("vectorrr".getBytes());

	public static void main(String[] args) {
		try {
			Bob b = new Bob(10234);
		} catch (InterruptedException | IOException e) {
			System.out.println("Unhandled exception, please try again");
			System.exit(0);
		}
	}

	public Bob(int port) throws IOException, InterruptedException {
		
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
			encryptCipher.init(Cipher.ENCRYPT_MODE, bob_key, IV);
			decryptCipher.init(Cipher.DECRYPT_MODE, bob_key, IV);
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

			// Constantly read input from clients
			while (true) {
				int message_length = iStream.readInt();
				byte[] message = new byte[message_length];
				iStream.readFully(message);

				/**
				 * Get serialized object - These objects are created to represent some of the message sent during the protocol.
				 * They implement the Serializable interface to make it easy to send objects across as byte arrays and convert
				 * them back with the necessary information.
				 */
				Object o = Utility.getObject(message);

				//Checks for initial message "I want to talk"
				if (o instanceof SerializedString) {
					Nonce nonce = new Nonce(System.nanoTime());
					byte[] nonce_bytes = Utility.prepBytes(1, nonce);
					nonce_bytes = Utility.encrypt(encryptCipher, nonce_bytes);
					
					System.out.println("2) Bob sends his first challenge");

					oStream.writeInt(nonce_bytes.length);
					oStream.flush();
					oStream.write(nonce_bytes);
					oStream.flush();
				}

				else {

					//By now, Alice has returned from the KDC and Bob is ready to receive the ticket and her challenge
					AliceToBobFirstMessage firstMessageFromAlice = (AliceToBobFirstMessage) o;

					//Decrypt the ticket
					byte[] decryptedTicketBytes = Utility.decrypt(decryptCipher, firstMessageFromAlice.getTicket());
					Ticket ticket = (Ticket) Utility.getObject(decryptedTicketBytes);
					alice_bob_key = ticket.getKey();

					//Initialize the shared key K_ab
					K_ab = new SecretKeySpec(alice_bob_key.getBytes(), "DESede");

					try {
						sharedKeyEncrypt.init(Cipher.ENCRYPT_MODE, K_ab, IV);
						sharedKeyDecrypt.init(Cipher.DECRYPT_MODE, K_ab, IV);
					} catch (InvalidKeyException e1) {
						System.out.println("Invalid key found or invalid parameters found");
						System.exit(0);
					}

					//Decrypt the challenge from alice
					byte[] decryptedAliceNonceBytes = Utility.decrypt(sharedKeyDecrypt,
							firstMessageFromAlice.getEncryptedNonce());
					Nonce aliceNonce = (Nonce) Utility.getObject(decryptedAliceNonceBytes);

					long aliceNonceValue = aliceNonce.getNonce();
					aliceNonceValue -= 1;

					//Generates the message to send Alice's nonce, minus one, and then Bob sends his own nonce
					long nonceValue = System.nanoTime();
					TwoNonce responseToAlice = new TwoNonce(aliceNonceValue, nonceValue);
					byte[] responseToAliceBytes = Utility.prepBytes(3, responseToAlice);
					byte[] encryptedResponseToAliceBytes = Utility.encrypt(sharedKeyEncrypt, responseToAliceBytes);
					
					System.out.println("7) Bob begins communication with Alice, sends back her nonce, minus one, and his own");

					oStream.writeInt(encryptedResponseToAliceBytes.length);
					oStream.flush();
					oStream.write(encryptedResponseToAliceBytes);

					//Decrypt and read Alice's final nonce value
					byte[] finalResponseFromAliceBytes = new byte[iStream.readInt()];
					iStream.readFully(finalResponseFromAliceBytes);
					byte[] decryptedFinalResponseFromAliceBytes = Utility.decrypt(sharedKeyDecrypt,
							finalResponseFromAliceBytes);
					Nonce finalResponseFromAlice = (Nonce) Utility.getObject(decryptedFinalResponseFromAliceBytes);
					
					System.out.println("9) Bob verifies the nonce Alice sent back");

					//Check to see if the nonce values match
					if (finalResponseFromAlice.getNonce() != nonceValue - 1) {
						System.out.println("Incorrect nonce sent back!");
						System.exit(0);
					}

					System.out.println("Authenticaiton complete!");

				}
			}
		} catch (Exception e) {
			iStream.close();
			oStream.close();
		}
	}
}
