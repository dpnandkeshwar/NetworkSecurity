package PP1;
import java.io.*;

public class Nonce implements Serializable{

	private long nonce;
	
	public Nonce(long nonce) { this.nonce = nonce; }
	
	public long getNonce() { return nonce; }
}
