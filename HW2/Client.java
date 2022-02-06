package HW2;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class Client {

	private Socket socket;
	private PrintWriter oStream;
	private InputStreamReader iStream;
	private InputStreamReader socketReader;

	private BigInteger aSecret = new BigInteger("160031");
	private BigInteger g = new BigInteger("1907");
	private BigInteger p = new BigInteger("784313");
	private BigInteger T_a;
	private BigInteger T_b;
	private BigInteger DFValue;

	public Client(String ipAddress, int port) throws UnknownHostException, IOException {
		socket = new Socket(ipAddress, port);
		System.out.println("Connected to " + ipAddress);
		oStream = new PrintWriter(socket.getOutputStream());
		iStream = new InputStreamReader(new BufferedInputStream(System.in));
		socketReader = new InputStreamReader(new BufferedInputStream(socket.getInputStream()));
		
		BufferedReader inReader = new BufferedReader(iStream);
		BufferedReader sReader = new BufferedReader(socketReader);

		String systemInBuf = "";
		String socketInBuf = "";
		T_a = DiffieHellman.calculateDiffieHellmanValue(g, aSecret, p);

		System.out.println("Alice's Calculation: " + T_a.toString());

		oStream.println(T_a.toString());
		oStream.flush();
		
		System.out.println("Waiting for Bob to respond with his value...");
		
		socketInBuf = sReader.readLine();
		T_b = new BigInteger(socketInBuf);
		DFValue = efficientExponentiation.calculate(T_b, aSecret, p);
		
		System.out.println("Diffie Hellman value calculated. Type QUIT to exit this connection");
		
		while(!systemInBuf.equals("QUIT"))
			systemInBuf = inReader.readLine();
		
		System.out.println("Calculated Diffie Hellman Shared Key: " + DFValue.toString());
		
		oStream.println("QUIT");
		oStream.flush();
		
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
