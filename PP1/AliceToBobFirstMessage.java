package PP1;

import java.io.Serializable;

public class AliceToBobFirstMessage implements Serializable {
	
	private byte[] ticket;
	private byte[] encryptedNonce;
	
	public AliceToBobFirstMessage(byte[] ticket, byte[] encryptedNonce) {
		this.ticket = ticket;
		this.encryptedNonce = encryptedNonce;
	}
	
	public byte[] getTicket() { return ticket; }
	public byte[] getEncryptedNonce() { return encryptedNonce; }
}
