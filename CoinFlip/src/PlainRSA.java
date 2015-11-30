

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.util.encoders.Hex;

public class PlainRSA {

	public static void main(String[] args) {
		// data
		String input = "Test";
		byte[] data = input.getBytes();
		byte[] encrData = null;
		byte[] decrData = null;

		System.out.println("Text: " + input + " Länge: " + data.length);

		// Create Keys
		RSAKeyPairGenerator keygen = new RSAKeyPairGenerator();
		keygen.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), // publicExponent
				new SecureRandom(), // prng
				16384, // strength
				10// certainty
		));

		AsymmetricCipherKeyPair keypair = keygen.generateKeyPair();

		// Encryption
		//AsymmetricBlockCipher engEn = new RSAEngine();
		AsymmetricBlockCipher engEn = new OAEPEncoding(new RSAEngine(), new SHA512Digest());
		
		
		engEn.init(true, keypair.getPublic());

		 System.out.println(engEn.getInputBlockSize());
		 System.out.println(engEn.getOutputBlockSize());

		try {
			encrData = engEn.processBlock(data, 0, data.length);
		} catch (InvalidCipherTextException error) {
			error.printStackTrace();
		}

		// Decryption
		//AsymmetricBlockCipher engDe = new RSAEngine();
		AsymmetricBlockCipher engDe = new OAEPEncoding(new RSAEngine(), new SHA512Digest());
		
		engDe.init(false, keypair.getPrivate());

		try {
			decrData = engDe.processBlock(encrData, 0, encrData.length);
		} catch (InvalidCipherTextException error) {
			error.printStackTrace();
		}

		String text0 = Hex.toHexString(data);
		String text1 = Hex.toHexString(encrData);
		String text2 = Hex.toHexString(decrData);

		System.out.println("Input: " + text0 + "\n" + "Encr:  " + text1 + "\n" + "Decr: " + text2);

		if (text0.equals(text2)) {
			System.out.println("Success");
		} else {
			System.out.println("Fail");
		}

	}

}
