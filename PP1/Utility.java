package PP1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class Utility {
	
	/**
	 * Converts a long to a byte array
	 * @param x
	 * @return
	 */
	public static byte[] longToBytes(long x) {
		
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	/**
	 * Converts a byte array into a long
	 * @param bytes
	 * @return
	 */
	public static long bytesToLong(byte[] bytes) {
		
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}
	
	/**
	 * Combine two byte arrays into a single byte array
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static byte[] combineTwoArrays(byte[] b1,  byte[] b2) {
		
		int totalLength = b1.length + b2.length;
		byte[] newArr = new byte[totalLength];
		int index = 0;
		
		for(int i = 0; i < b1.length; i++) {
			newArr[i] = b1[i];
			index++;
		}
		
		int j = 0;
		for(int i = index; i < b2.length + index; i++) {
			newArr[i] = b2[j];
			j++;
		}
		
		return newArr;
		
		
	}

	/**
	 * Takes a byte[] message and encrypts it using 3DES
	 * @param c
	 * @param message
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] encrypt(Cipher c, byte[] message) throws UnsupportedEncodingException {

		byte[] encryptedBytes = null;
		try {
			encryptedBytes = c.doFinal(message);

		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Illegal block size or bad padding found");
			System.exit(0);
		}
		if (encryptedBytes != null)
			return encryptedBytes;

		System.out.println("Error in encryption");
		System.exit(0);
		return null;
	}

	/**
	 * Takes a byte[] message and decryptes it using 3DES
	 * @param c
	 * @param message
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] decrypt(Cipher c, byte[] message) throws UnsupportedEncodingException {
		byte[] decryptedBytes = null;
		try {
			decryptedBytes = c.doFinal(message);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			System.out.println("Illegal block size or bad padding found");
			System.exit(0);
		}

		if (decryptedBytes != null)
			return decryptedBytes;

		System.out.println("Error in decryption");
		System.exit(0);
		return null;
	}

	/**
	 * Takes a byte[] and converts it back into an object that represents some message in the protocol
	 * @param message
	 * @return
	 */
	public static Object getObject(byte[] message) {
		ByteArrayInputStream bis = new ByteArrayInputStream(message);
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(bis);
			Object o = in.readObject();
			return o;
		} catch (Exception e) {
			System.out.println("Error occured extracting object, please restart");
			System.exit(0);
		}
		
		return null;
	}

	/**
	 * Serialize and object that is used to represent some message in the protocol into a byte[] for transmission
	 * @param mode
	 * @param o
	 * @return
	 */
	public static byte[] prepBytes(int mode, Object o) {
		switch (mode) {

		case 0: {
			SerializedString message = (SerializedString) o;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(message);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return yourBytes;
			} catch (Exception e) {
				System.out.println("Error occured, please restart");
				System.exit(0);
			}
		}

		case 1: {
			Nonce message = (Nonce) o;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(message);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return yourBytes;
			} catch (Exception e) {
				System.out.println("Error occured, please restart");
				System.exit(0);
			}
		}

		case 2: {
			KDCResponse message = (KDCResponse) o;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(message);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return yourBytes;
			} catch (Exception e) {
				System.out.println("Error occured, please restart");
				System.exit(0);
			}
		}

		case 3: {
			TwoNonce message = (TwoNonce) o;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(message);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return yourBytes;
			} catch (Exception e) {
				System.out.println("Error occured, please restart");
				System.exit(0);
			}
		}
		
		case 4: {
			AliceKDCRequest message = (AliceKDCRequest) o;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(message);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return yourBytes;
			} catch (Exception e) {
				System.out.println("Error occured, please restart");
				System.exit(0);
			}
		}
		
		case 5: {
			Ticket message = (Ticket) o;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(message);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return yourBytes;
			} catch (Exception e) {
				System.out.println("Error occured, please restart");
				System.exit(0);
			}
		}
		
		case 6:
			AliceToBobFirstMessage message = (AliceToBobFirstMessage) o;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = null;
			try {
				out = new ObjectOutputStream(bos);
				out.writeObject(message);
				out.flush();
				byte[] yourBytes = bos.toByteArray();
				return yourBytes;
			} catch (Exception e) {
				System.out.println("Error occured, please restart");
				System.exit(0);
			}

		default:
			return null;
		}
	}
}
