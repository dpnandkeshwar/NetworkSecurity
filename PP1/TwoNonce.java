package PP1;

import java.io.Serializable;

public class TwoNonce implements Serializable {
	
	private long nonce1;
	private long nonce2;
	
	public TwoNonce(long nonce1, long nonce2) {
		this.nonce1 = nonce1;
		this.nonce2 = nonce2;
	}
	
	public long getNonceOne() { return nonce1; }
	public long getNonceTwo() { return nonce2; }
}
