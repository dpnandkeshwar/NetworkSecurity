package PP1;

import java.io.Serializable;

public class Ticket implements Serializable {
	
	private String K_ab;
	private String identifier;
	private long nonce;
	
	public Ticket(String K_ab, String identifier, long nonce) {
		this.K_ab = K_ab;
		this.identifier = identifier;
		this.nonce = nonce;
	}
	
	public String getKey() { return K_ab; }
	public String getID() { return identifier; }
	public long getNonce() { return nonce; }
}
