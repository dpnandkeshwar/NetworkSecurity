package PP1;

import java.io.Serializable;

public class SerializedString implements Serializable {

	private String str;
	
	public SerializedString(String str) { this.str = str; }
	public String getString() { return str; }
}
