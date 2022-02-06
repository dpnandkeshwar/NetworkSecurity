package HW2;

import java.math.BigInteger;

public class efficientExponentiation {

	public static BigInteger calculate(BigInteger base, BigInteger exponent, BigInteger mod) {
		BigInteger value = new BigInteger("1");
		char[] bitString = exponent.toString(2).toCharArray();
		for(char i : bitString) {
			value = value.multiply(value);
			if(i == '1')
				value = value.multiply(base);
			value = value.mod(mod);
		}
		
		return value;
		
	}
}
