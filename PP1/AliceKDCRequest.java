package PP1;

import java.io.*;

public class AliceKDCRequest implements Serializable{
	
	private long nonce;
	private String requestString;
	private byte[] encryptedBobChallenge;
	
	public AliceKDCRequest(long nonce, String requestString, byte[] encryptedBobChallenge) {
		this.nonce = nonce;
		this.requestString = requestString;
		this.encryptedBobChallenge = encryptedBobChallenge;
	}
	
	public long getNonce() { return this.nonce; }
	public String getString() { return this.requestString; }
	public byte[] getEncryptedChallenge() { return this.encryptedBobChallenge; }
}
