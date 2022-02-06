package HW2;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class Server {

	private Socket socket;
	private ServerSocket serverSocket;
	private InputStreamReader iStream;
	private PrintWriter oStream;

	private BigInteger bSecret = new BigInteger("12077");
	private BigInteger g = new BigInteger("1907");
	private BigInteger p = new BigInteger("784313");
	private BigInteger T_a;
	private BigInteger T_b;
	private BigInteger DFValue;

	public Server(int port) throws IOException, InterruptedException {
		serverSocket = new ServerSocket(port);
		System.out.println("Server started");
		System.out.println("Waiting for client connection...");

		socket = serverSocket.accept();
		System.out.println("Client accepted!");

		iStream = new InputStreamReader(new BufferedInputStream(socket.getInputStream()));
		
		BufferedReader bf = new BufferedReader(iStream);

		oStream = new PrintWriter(socket.getOutputStream());

		T_b = DiffieHellman.calculateDiffieHellmanValue(g, bSecret, p);

		System.out.println("Bob's Calculation: " + T_b.toString());

		String inputBuf = "";
		
		System.out.println("Waiting for Alice to respond with her value...");
		
		inputBuf = bf.readLine();
		T_a = new BigInteger(inputBuf);
		DFValue = efficientExponentiation.calculate(T_a, bSecret, p);
		System.out.println("Diffie Hellman value calculated for Bob");

		oStream.println(T_b.toString());
		oStream.flush();
		
		System.out.println("Awaiting client to close connection...");
		
		while(!bf.readLine().equals("QUIT"))
			
		System.out.println("Calculated Diffie Hellman Shared Key: " + DFValue.toString());

		try {
			socket.close();
			serverSocket.close();
			iStream.close();
			oStream.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = new Server(8000);
	}
}
