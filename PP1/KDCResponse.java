package PP1;

import java.io.Serializable;

public class KDCResponse implements Serializable {
	
	private long nonce;
	private String identifier;
	private String K_ab;
	private byte[] ticket;
	
	public KDCResponse(long nonce, String identifier, String K_ab, byte[] ticket) {
		this.nonce = nonce;
		this.identifier = identifier;
		this.K_ab = K_ab;
		this.ticket = ticket;
	}
	
	public long getNonce() { return nonce; }
	public String getID() { return identifier; }
	public String getKey() { return K_ab; }
	public byte[] getTicket() { return ticket; }
}
