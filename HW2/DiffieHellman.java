package HW2;

import java.math.BigInteger;

public class DiffieHellman {
	
	public static BigInteger calculateDiffieHellmanValue(BigInteger g, BigInteger secret, BigInteger p) {
		return efficientExponentiation.calculate(g, secret, p);
	}
}
