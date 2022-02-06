package HW2;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class Client {
	
	private Socket socket;
	private DataOutputStream oStream;
	private DataInputStream iStream;
	
	private BigInteger aSecret = new BigInteger("160031");
	private BigInteger g = new BigInteger("1907");
	private BigInteger p = new BigInteger("784313");
	private BigInteger T_a;
	private BigInteger T_b;
	private BigInteger DFValue;
	
	public Client(String ipAddress, int port) throws UnknownHostException, IOException {
		socket = new Socket(ipAddress, port);
		System.out.println("Connected to " + ipAddress);
		oStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		iStream = new DataInputStream(System.in);
		
		String inputBuf = "";
		T_a = DiffieHellman.calculateDiffieHellmanValue(g, aSecret, p);
		
		System.out.println("Alice's Calculation: " + T_a.toString());
		
		while(!inputBuf.equals("QUIT")) {
			inputBuf = iStream.readUTF();
			T_b = new BigInteger(inputBuf);
			DFValue = efficientExponentiation.calculate(T_b, aSecret, p);
			oStream.writeChars(T_a.toString());
		}
		
		System.out.println("Calculated Diffie Hellman Shared Key: " + DFValue.toString());
		
		try {
			socket.close();
			iStream.close();
			oStream.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Client client = new Client("127.0.0.1", 8000);
	}
}
