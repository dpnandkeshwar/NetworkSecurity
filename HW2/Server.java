package HW2;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class Server {

	private Socket socket;
	private ServerSocket serverSocket;
	private DataInputStream iStream;
	private DataOutputStream oStream;
	
	private BigInteger bSecret = new BigInteger("12077");
	private BigInteger g = new BigInteger("1907");
	private BigInteger p = new BigInteger("784313");
	private BigInteger T_a;
	private BigInteger T_b;
	private BigInteger DFValue;
	
	public Server(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		System.out.println("Server started");
		System.out.println("Waiting for client connection...");
		
		socket = serverSocket.accept();
		System.out.println("Client accepted!");
		
		iStream = new DataInputStream(
                new BufferedInputStream(socket.getInputStream()));
		
		oStream = new DataOutputStream(
				new BufferedOutputStream(socket.getOutputStream()));
		
		T_b = DiffieHellman.calculateDiffieHellmanValue(g, bSecret, p);
		
		System.out.println("Bob's Calculation: " + T_b.toString());
		
		String inputBuf = "";
		
		while(!inputBuf.equals("QUIT")) {
			String inputFromClient = iStream.readUTF();
			T_a = new BigInteger(inputFromClient);
			DFValue = efficientExponentiation.calculate(T_a, bSecret, p);
			oStream.writeChars(T_b.toString());
		}
		
		try {
			socket.close();
			serverSocket.close();
			iStream.close();
			oStream.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
	}
	
	public static void main(String[] args) throws IOException {
		Server server = new Server(8000);
	}
}