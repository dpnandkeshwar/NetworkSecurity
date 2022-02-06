package HW2;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class Client {

	private Socket socket;
	private DataOutputStream oStream;
	private DataInputStream iStream;
	private DataInputStream socketReader;

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
		iStream = new DataInputStream(new BufferedInputStream(System.in));
		socketReader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

		String systemInBuf = "";
		String socketInBuf = "";
		T_a = DiffieHellman.calculateDiffieHellmanValue(g, aSecret, p);

		System.out.println("Alice's Calculation: " + T_a.toString());

		oStream.writeChars(T_a.toString());
		
		System.out.println("Waiting for Bob to respond with his value...");

		socketInBuf = iStream.readUTF();
		System.out.println("?");
		T_b = new BigInteger(socketInBuf);
		DFValue = efficientExponentiation.calculate(T_b, aSecret, p);
		
		System.out.println("Diffie Hellman value calculated. Type QUIT to exit this connection");
		
		while(!systemInBuf.equals("QUIT"))
			systemInBuf = iStream.readUTF();
		
		System.out.println("Calculated Diffie Hellman Shared Key: " + DFValue.toString());
		
		oStream.writeChars("QUIT");
		
		try {
			socket.close();
			iStream.close();
			oStream.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		Client client = new Client("127.0.0.1", 8000);
	}
}
